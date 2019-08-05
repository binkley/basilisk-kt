# DB Migrations

Given agreements between teams, the migrations could be sliced by library, so
a binary only pulls in migrations needed for its domain objects.

However, the performance improvement in testing is premised on using a single
JDBC connection to a Dockerized Postgres, shared across all tests in the JVM.
Splicing migrations would mean conflicts from Flyway, or necessitate separate
Docker containers for each library. 
