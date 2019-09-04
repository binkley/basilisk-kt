# DB Migrations

Given agreements between teams, the migrations could be sliced by library, so
a binary only pulls in migrations needed for its domain objects.

However, the performance improvement in testing is premised on using a single
JDBC connection to a Dockerized Postgres, shared across all tests in the JVM.
Splicing migrations would mean conflicts from Flyway, or necessitate separate
Docker containers for each library. 

## Migration versioning

Flyway documentation suggests simple integers for versioning migrations.
However, this does not comport well with slicing, and implies all migrations
are in a common source location.

A clever alternative is _timestamps_, following advice in
[_Best Practices using Flyway for Database Migrations_](http://dbabullet.com/index.php/2018/03/29/best-practices-using-flyway-for-database-migrations/).
