CREATE TABLE IF NOT EXISTS "location"
(
    id     SERIAL PRIMARY KEY,
    code   TEXT NOT NULL UNIQUE,
    "name" TEXT NOT NULL
);
