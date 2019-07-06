CREATE TABLE IF NOT EXISTS "location"
(
    id     SERIAL PRIMARY KEY,
    "name" TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS chef
(
    id     SERIAL PRIMARY KEY,
    "name" TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS recipe
(
    id      SERIAL PRIMARY KEY,
    "name"  TEXT NOT NULL,
    chef_id INT  NOT NULL,
    CONSTRAINT fk_recipe_chef_id_id FOREIGN KEY (chef_id) REFERENCES chef (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE TABLE IF NOT EXISTS "source"
(
    id     SERIAL PRIMARY KEY,
    "name" TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS ingredient
(
    id        SERIAL PRIMARY KEY,
    chef_id   INT NOT NULL,
    recipe_id INT NULL,
    source_id INT NOT NULL,
    CONSTRAINT fk_ingredient_recipe_id_id FOREIGN KEY (recipe_id) REFERENCES recipe (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_ingredient_source_id_id FOREIGN KEY (source_id) REFERENCES "source" (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_ingredient_chef_id_id FOREIGN KEY (chef_id) REFERENCES chef (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE TABLE IF NOT EXISTS trip
(
    id      SERIAL PRIMARY KEY,
    "name"  TEXT NOT NULL,
    chef_id INT  NOT NULL,
    CONSTRAINT fk_trip_chef_id_id FOREIGN KEY (chef_id) REFERENCES chef (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE TABLE IF NOT EXISTS leg
(
    id                SERIAL PRIMARY KEY,
    start_location_id INT NOT NULL,
    end_location_id   INT NOT NULL,
    trip_id           INT NOT NULL,
    CONSTRAINT fk_leg_start_location_id_id FOREIGN KEY (start_location_id) REFERENCES location (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_leg_end_location_id_id FOREIGN KEY (end_location_id) REFERENCES location (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_leg_trip_id_id FOREIGN KEY (trip_id) REFERENCES trip (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
