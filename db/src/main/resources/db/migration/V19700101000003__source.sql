CREATE TABLE IF NOT EXISTS "source"
(
    id     SERIAL PRIMARY KEY,
    code   TEXT NOT NULL UNIQUE,
    "name" TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS source_location
(
    source_id   INT,
    location_id INT,
    CONSTRAINT pk_SOURCE_LOCATION PRIMARY KEY (source_id, location_id),
    CONSTRAINT fk_source_location_source_id_id FOREIGN KEY (source_id) REFERENCES "source" (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_source_location_location_id_id FOREIGN KEY (location_id) REFERENCES "location" (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
