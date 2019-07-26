CREATE TABLE IF NOT EXISTS "location"
(
    id     SERIAL PRIMARY KEY,
    "name" TEXT NOT NULL,
    code   TEXT NOT NULL UNIQUE
);
