CREATE TABLE IF NOT EXISTS recipe
(
    id      SERIAL PRIMARY KEY,
    "name"  TEXT NOT NULL,
    code    TEXT NOT NULL UNIQUE,
    chef_id INT  NOT NULL,
    CONSTRAINT fk_recipe_chef_id_id FOREIGN KEY (chef_id) REFERENCES chef (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE IF NOT EXISTS "source"
(
    id     SERIAL PRIMARY KEY,
    "name" TEXT NOT NULL,
    code   TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS ingredient
(
    id        SERIAL PRIMARY KEY,
    code      TEXT NOT NULL UNIQUE,
    chef_id   INT  NOT NULL,
    recipe_id INT  NULL,
    source_id INT  NOT NULL,
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
    start_location_id INT       NOT NULL,
    start_at          TIMESTAMP NOT NULL,
    end_location_id   INT       NOT NULL,
    end_at            TIMESTAMP NOT NULL,
    trip_id           INT       NOT NULL,
    CONSTRAINT fk_leg_start_location_id_id FOREIGN KEY (start_location_id) REFERENCES location (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_leg_end_location_id_id FOREIGN KEY (end_location_id) REFERENCES location (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_leg_trip_id_id FOREIGN KEY (trip_id) REFERENCES trip (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE IF NOT EXISTS source_location
(
    source_id   INT,
    location_id INT,
    CONSTRAINT pk_SOURCE_LOCATION PRIMARY KEY (source_id, location_id),
    CONSTRAINT fk_source_location_source_id_id FOREIGN KEY (source_id) REFERENCES "source" (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_source_location_location_id_id FOREIGN KEY (location_id) REFERENCES "location" (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE IF NOT EXISTS ingredient_location
(
    ingredient_id INT,
    location_id   INT,
    CONSTRAINT pk_INGREDIENT_LOCATION PRIMARY KEY (ingredient_id, location_id),
    CONSTRAINT fk_ingredient_location_ingredient_id_id FOREIGN KEY (ingredient_id) REFERENCES "ingredient" (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_ingredient_location_location_id_id FOREIGN KEY (location_id) REFERENCES "location" (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE IF NOT EXISTS recipe_location
(
    recipe_id   INT,
    location_id INT,
    CONSTRAINT pk_RECIPE_LOCATION PRIMARY KEY (recipe_id, location_id),
    CONSTRAINT fk_recipe_location_recipe_id_id FOREIGN KEY (recipe_id) REFERENCES "recipe" (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_recipe_location_location_id_id FOREIGN KEY (location_id) REFERENCES "location" (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
