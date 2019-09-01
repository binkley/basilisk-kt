CREATE TABLE IF NOT EXISTS trip
(
    id        SERIAL PRIMARY KEY,
    "name"    TEXT NOT NULL,
    chef_code TEXT NOT NULL,
    CONSTRAINT fk_trip_chef_code_id FOREIGN KEY (chef_code) REFERENCES chef (code) ON DELETE RESTRICT ON UPDATE RESTRICT
);
