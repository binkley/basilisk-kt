#!/bin/bash

set -e

psql -b -v ON_ERROR_STOP=1 <<-'EOSQL'
CREATE USER basilisk;
CREATE DATABASE basilisk;
GRANT ALL PRIVILEGES ON DATABASE basilisk TO basilisk;
CREATE DATABASE basil;
GRANT ALL PRIVILEGES ON DATABASE basil TO basilisk;
CREATE DATABASE chefs;
GRANT ALL PRIVILEGES ON DATABASE chefs TO basilisk;
EOSQL
