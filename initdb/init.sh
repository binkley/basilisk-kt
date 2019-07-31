#!/bin/bash

set -e

psql -b -v ON_ERROR_STOP=1 <<-'EOSQL'
CREATE DATABASE basilisk;
CREATE USER basilisk;
GRANT ALL PRIVILEGES ON DATABASE basilisk TO basilisk;
\connect basilisk basilisk;
CREATE SCHEMA basil;
CREATE SCHEMA chefs;
EOSQL
