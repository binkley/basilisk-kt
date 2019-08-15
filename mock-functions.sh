function standard-headers() {
  cat <<EOH | sed 's/$/\r/'
Connection: close
Date: $(TZ=GMT date +'%a, %d %b %Y %T %Z')
X-B3-TraceId: abcdef0123456789
X-B3-SpanId: abcdef0123456789
X-B3-Sampled: 1

EOH
}

function ok-with-json() {
  local json="$1"
  cat <<EOH | sed 's/$/\r/'
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: ${#json}
EOH
  standard-headers
  printf %s "$json"
}

function ok() {
  cat <<EOH | sed 's/$/\r/'
HTTP/1.1 200 OK
Content-Length: 0
EOH
  standard-headers
}

function not-found() {
  cat <<EOH | sed 's/$/\r/'
HTTP/1.1 404 Not Found
Content-Length: 0
EOH
  standard-headers
}

function method-no-allowed() {
  cat <<EOH | sed 's/$/\r/'
HTTP/1.1 405 Method Not Allowed
Allow: GET
Content-Length: 0
EOH
  standard-headers
}
