CREATE TABLE IF NOT EXISTS leg
(
    id                SERIAL PRIMARY KEY,
    start_location_id INT       NOT NULL,
    start_at          TIMESTAMP NOT NULL,
    end_location_id   INT       NOT NULL,
    end_at            TIMESTAMP NOT NULL,
    trip_id           INT       NOT NULL,
    CONSTRAINT fk_leg_start_location_id_id FOREIGN KEY (start_location_id) REFERENCES location (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_leg_end_location_id_id FOREIGN KEY (end_location_id) REFERENCES location (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_leg_trip_id_id FOREIGN KEY (trip_id) REFERENCES trip (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
