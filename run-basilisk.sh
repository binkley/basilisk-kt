#!/usr/bin/env bash

# shellcheck disable=SC2214,SC2215

export PS4='+${BASH_SOURCE}:${LINENO}:${FUNCNAME[0]:+${FUNCNAME[0]}():} '

set -e
set -u
set -o pipefail

# Colors!
printf -v preset "\e[0m"
printf -v pred "\e[31m"
printf -v pyellow "\e[33m"

function -print-usage() {
  echo "Usage: $0 [-h|--help] [ENVIRONMENTS(s)]"
}

function -print-help() {
  -print-usage
  cat <<EOH

Options:
  -h, --help            Print this help and exit

Arguments:
  ENVIRONMENTS(s)  Run all programs using with Micronaut ENVIRONMENTS(s)
EOH
}

basil_environments=("app" "db")
basil_vm_args=()
chefs_environments=("app" "db")
chefs_vm_args=()

while getopts :h-: opt; do
  [[ $opt == - ]] && opt=${OPTARG%%=*} OPTARG=${OPTARG#*=}
  case $opt in
  h | help)
    -print-help
    exit 0
    ;;
  *)
    -print-usage >&2
    exit 2
    ;;
  esac
done
shift $((OPTIND - 1))

if ! docker ps >/dev/null 2>&1; then
  echo "$0: ${pred}Docker not running${preset}" >&2
  exit 2
fi

function -join() {
  local IFS=,
  echo "$*"
}

basil_vm_args=("${basil_vm_args[@]}"
  "-Dmicronaut.environments=$(-join "${basil_environments[@]}" "$@")")
chefs_vm_args=("${chefs_vm_args[@]}"
  "-Dmicronaut.environments=$(-join "${chefs_environments[@]}" "$@")")

# See https://stackoverflow.com/a/22644006
rc=0
trap 'exit $rc' INT TERM
trap 'rm -rf "$tmpdir" ; kill 0 ; exit $rc' EXIT
tmpdir="$(mktemp -d 2>/dev/null || mktemp -d -t basilisk)"

function -ready-or-die() {
  local bgpid=$1
  local out_file=$2
  local ready_text=$3

  # This is convoluted, but gets the script to use the exit code (`$?`) of the job on error
  while ! grep "$ready_text" "$out_file" >/dev/null; do
    # `kill -0 <pid>` is neat: Check that the process is running, but do not
    # actually delivery a signal
    kill -0 $bgpid 2>/dev/null || {
      # Meaning ... wait for fake signal to be delivered
      wait $bgpid || {
        rc=$?
        cat "$out_file"
        exit $rc
      }
    }
    sleep 1
  done
}

echo "Resetting docker ..."
docker-compose down -v >"$tmpdir/docker" 2>&1

echo "Waiting for postgres to be ready ..."
docker-compose up basilisk-db >>"$tmpdir/postgres" 2>&1 &
-ready-or-die $! "$tmpdir/postgres" 'database system is ready to accept connections'

echo "Installing schemas ..."
./gradlew flywayMigrate >"$tmpdir/schema"

echo "${pyellow}No seed data${preset}"

echo "Building applications ..."
# Use this instead of `if ... then ... fi` to capture $?
./gradlew shadowJar --rerun-tasks >"$tmpdir/jars" || {
  rc=$?
  cat "$tmpdir/jars"
  exit $rc
}

echo "Waiting for Basil ..."
java "${basil_vm_args[@]}" -jar basil-bin/build/libs/*-all.jar >"$tmpdir/basil" 2>&1 &
-ready-or-die $! "$tmpdir/basil" "Startup completed in"

echo "Waiting for Chefs ..."
java "${chefs_vm_args[@]}" -jar chefs-bin/build/libs/*-all.jar >"$tmpdir/chefs" 2>&1 &
-ready-or-die $! "$tmpdir/chefs" "Startup completed in"

echo "Ready.  Following application logs ..."
tail -F "$tmpdir/basil" "$tmpdir/chefs"