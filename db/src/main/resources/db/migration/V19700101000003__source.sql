CREATE TABLE IF NOT EXISTS "source"
(
    id     SERIAL PRIMARY KEY,
    code   TEXT NOT NULL UNIQUE,
    "name" TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS source_location
(
    source_id     INT,
    location_code TEXT,
    CONSTRAINT pk_SOURCE_LOCATION PRIMARY KEY (source_id, location_code),
    CONSTRAINT fk_source_location_source_id_id FOREIGN KEY (source_id) REFERENCES "source" (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_source_location_location_code_id FOREIGN KEY (location_code) REFERENCES "location" (code) ON DELETE RESTRICT ON UPDATE RESTRICT
);
