#!/usr/bin/env bash

set -e
set -u
set -o pipefail

read -r verb path protocol

function not-found() {
  cat <<EOM | sed 's/$/\r/'
HTTP/1.1 404 Not Found
Content-Length: 0
Connection: close
Date: $(TZ=GMT date +'%a, %d %b %Y %T %Z')
X-B3-TraceId: abcdef0123456789
X-B3-SpanId: abcdef0123456789
X-B3-Sampled: 1

EOM
}

case $path in
*) not-found ;;
esac
