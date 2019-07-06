#!/bin/bash

set -e

psql -U basilisk -v ON_ERROR_STOP=1 <<-EOSQL
EOSQL
