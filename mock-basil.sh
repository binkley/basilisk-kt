#!/usr/bin/env bash

export PS4='+${BASH_SOURCE}:${LINENO}:${FUNCNAME[0]:+${FUNCNAME[0]}():} '

set -e
set -u
set -o pipefail

# shellcheck source=mock-functions.sh
. "${0%/*}/mock-functions.sh"

read -r verb path protocol

case $path in
*) not-found ;;
esac
