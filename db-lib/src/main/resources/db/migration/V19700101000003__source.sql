CREATE TABLE IF NOT EXISTS "source"
(
    code   TEXT NOT NULL PRIMARY KEY,
    "name" TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS source_location
(
    source_code   TEXT,
    location_code TEXT,
    CONSTRAINT pk_SOURCE_LOCATION PRIMARY KEY (source_code, location_code),
    CONSTRAINT fk_source_location_source_code_id FOREIGN KEY (source_code) REFERENCES "source" (code) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_source_location_location_code_id FOREIGN KEY (location_code) REFERENCES "location" (code) ON DELETE RESTRICT ON UPDATE RESTRICT
);
