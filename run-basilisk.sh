#!/usr/bin/env bash

# shellcheck disable=SC2214

export PS4='+${BASH_SOURCE}:${LINENO}:${FUNCNAME[0]:+${FUNCNAME[0]}():} '

set -e
set -u
set -o pipefail

# Colors!
printf -v preset "\e[0m"
printf -v pred "\e[31m"
printf -v pgreen "\e[32m"
printf -v pyellow "\e[33m"

function _print-usage() {
  echo "Usage: $0 [-E|--environments=ENVIRONMENT(s)][-h|--help][-n|--dry-run] [TARGETS(s)]"
}

function _print-help() {
  _print-usage
  cat <<EOH

Options:
  -E, --environments=ENVIRONMENT(s)
                  Run with specific Micronaut ENVIRONMENT(s), comma-separated
                    if multiple; default is "app db"
  -h, --help      Print this help and exit
  -n, --dry-run   Prints what would execute, but does not execute

Arguments:
  TARGETS(s)  Run specific TARGET(s); default is "basil chefs".  All targets
    include tailing logs.  Top-level targets:
      - chefs   Runs chefs service
      - basil   Runs basil service
      - db      Runs just database
EOH
}

# We always need these to have a functioning program
basil_environments=("app" "db")
chefs_environments=("app" "db")

run=
while getopts :E:e:hn-: opt; do
  [[ $opt == - ]] && opt=${OPTARG%%=*} OPTARG=${OPTARG#*=}
  case $opt in
  E | environments)
    IFS=, read -r -a all_environments <<<"$OPTARG"
    basil_environments=("${basil_environments[@]}" "${all_environments[@]}")
    chefs_environments=("${chefs_environments[@]}" "${all_environments[@]}")
    ;;
  h | help)
    _print-help
    exit 0
    ;;
  n | dry-run)
    run="echo"
    ;;
  *)
    _print-usage >&2
    exit 2
    ;;
  esac
done
shift $((OPTIND - 1))

targets=() # Avoid clobbering cmd line flags, esp `-x`
case $# in
0) targets=(basil chefs) ;;
*) targets=("$@") ;;
esac
targets=("${targets[@]}" logs) # Assume tailing logs

# See https://stackoverflow.com/a/22644006
rc=0
trap 'exit $rc' INT TERM
trap 'rm -rf "$tmpdir" ; kill 0 ; exit $rc' EXIT
tmpdir="$(mktemp -d 2>/dev/null || mktemp -d -t basilisk)"

# TODO: How to exit from ERR with original failed exit code?
function stack-trace-and-exit() {
  local err=$?
  set +o xtrace # Do not trace this code
  # TODO: Fix the exit code
  # shellcheck disable=SC2034
  local code="${1:-1}"
  echo "Error in ${BASH_SOURCE[1]}:${BASH_LINENO[0]}. '${BASH_COMMAND}' exited with status $err"
  # Print out the stack trace described by $function_stack
  if [ ${#FUNCNAME[@]} -gt 2 ]; then
    echo "Stack:"
    for ((i = 1; i < ${#FUNCNAME[@]} - 1; i++)); do
      echo " $i: ${BASH_SOURCE[$i + 1]}:${BASH_LINENO[$i]} ${FUNCNAME[$i]}(...)"
    done
  fi
  echo "Exiting with status $err"
  exit "$err"
}
trap stack-trace-and-exit ERR
set -o errtrace

function _join() {
  local IFS=,
  echo "$*"
}

logs_to_tail=()
function _tail-log() {
  logs_to_tail=("${logs_to_tail[@]}" "$1")
}

function _ready-or-die() {
  local bgpid=$1
  local out_file=$2
  local ready_text=$3

  # This is convoluted, but gets the script to use the exit code (`$?`) of the job on error
  while ! grep "$ready_text" "$out_file" >/dev/null; do
    # `kill -0 <pid>` is neat: Check that the process is running, but do not
    # actually delivery a signal
    kill -0 "$bgpid" 2>/dev/null || {
      # Meaning ... wait for fake signal to be delivered
      wait "$bgpid" || {
        rc=$?
        cat "$out_file"
        exit $rc
      }
    }
    sleep 1
  done
}

function check-docker() {
  if ! docker ps >/dev/null 2>&1; then
    echo "$0: ${pred}Docker not running${preset}" >&2
    exit 2
  fi
}

function reset-docker() {
  echo "Resetting docker ..."
  docker-compose down -v >"$tmpdir/docker" 2>&1
}

function run-postgres() {
  echo "Waiting for postgres to be ready ..."
  docker-compose up basilisk-db >>"$tmpdir/postgres" 2>&1 &
  _ready-or-die $! "$tmpdir/postgres" 'database system is ready to accept connections'
  _tail-log "$tmpdir/postgres"
}

function install-schemas() {
  echo "Installing schemas ..."
  ./gradlew flywayMigrate >"$tmpdir/schemas" || {
    rc=$?
    cat "$tmpdir/schemas" >&2
    return $rc
  }
}

function install-seed-data() {
  echo "${pyellow}No seed data${preset}"
}

function build-all() {
  echo "Building applications ..."
  # Use this instead of `if ... then ... fi` to capture $?
  ./gradlew shadowJar --rerun-tasks >"$tmpdir/jars" || {
    rc=$?
    cat "$tmpdir/jars"
    exit $rc
  }
}

function run-chefs() {
  echo "Waiting for Chefs ..."
  local chefs_vm_args=("${chefs_vm_args[@]}"
    "-Dmicronaut.environments=$(_join "${chefs_environments[@]}" "$@")")
  echo "Running java ${chefs_vm_args[*]} -jar chefs-bin/build/libs/*-all.jar" >"$tmpdir/chefs"
  java "${chefs_vm_args[@]}" -jar chefs-bin/build/libs/*-all.jar >>"$tmpdir/chefs" 2>&1 &
  _ready-or-die $! "$tmpdir/chefs" "Startup completed in"
  _tail-log "$tmpdir/chefs"
}

function _mock-service() {
  command -v socat >/dev/null || {
    echo "${pred}$0: ERROR: 'socat' not installed${preset}" >&2
    exit 1
  }

  local port="$1"
  local exec="$2"
  local log="$3"

  socat -v tcp-l:"$port",fork exec:"$exec" >"$log" 2>&1 &
}

function mock-chefs() {
  echo "Mocking Chefs ..."
  _mock-service 7372 ./mock-chefs.sh "$tmpdir/chefs"
  _tail-log "$tmpdir/chefs"
}

function run-basil() {
  echo "Waiting for Basil ..."
  local basil_vm_args=("${basil_vm_args[@]}"
    "-Dmicronaut.environments=$(_join "${basil_environments[@]}" "$@")")
  echo "Running java ${basil_vm_args[*]} -jar basil-bin/build/libs/*-all.jar" >"$tmpdir/basil"
  java "${basil_vm_args[@]}" -jar basil-bin/build/libs/*-all.jar >>"$tmpdir/basil" 2>&1 &
  _ready-or-die $! "$tmpdir/basil" "Startup completed in"
  _tail-log "$tmpdir/basil"
}

function mock-basil() {
  echo "Mocking Basil ..."
  _mock-service 7371 ./mock-basil.sh "$tmpdir/basil"
  _tail-log "$tmpdir/basil"
}

function tail-logs() {
  [[ 0 == "${#logs_to_tail[@]}" ]] && return
  echo "${pgreen}Ready${preset}.  Following application logs ..."
  echo # Blank line to help spot the start of logs
  tail -F "${logs_to_tail[@]}"
}

make -rs -f - "${targets[@]}" >"$tmpdir/make" <<'EOM'
all:
	echo BUG: No targets
	exit 1

ifneq ($(filter-out basil,$(MAKECMDGOALS)),$(MAKECMDGOALS))
basil: need-basil
else
basil: mock-basil
endif

ifneq ($(filter-out chefs,$(MAKECMDGOALS)),$(MAKECMDGOALS))
chefs: need-chefs
else
chefs: mock-chefs
endif

db: need-schemas
logs: need-logs

need-docker:
	echo check-docker
	echo reset-docker

need-postgres: need-docker
	echo run-postgres

need-schemas: need-postgres
	echo install-schemas

need-seed-data: need-schemas
	echo install-seed-data

need-basil: need-seed-data chefs need-build
	echo run-basil

mock-basil:
	echo mock-basil

need-chefs: need-seed-data need-build
	echo run-chefs

mock-chefs:
	echo mock-chefs

need-build:
	echo build-all

need-logs:
	echo tail-logs
EOM

# Read in here; avoid calls in functions to programs that drain STDIN
# I'm looking at you, Gradle
IFS=$'\n' read -d '' -r -a commands <"$tmpdir/make" || true
for command in "${commands[@]}"; do
  $run "$command" || {
    rc=$?
    echo "${pred}$0: Function failed: $command${preset}" >&2
    exit $rc
  }
done
