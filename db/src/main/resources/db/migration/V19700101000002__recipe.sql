CREATE TABLE IF NOT EXISTS recipe
(
    id        SERIAL PRIMARY KEY,
    code      TEXT NOT NULL UNIQUE,
    "name"    TEXT NOT NULL,
    chef_code TEXT NOT NULL,
    status    TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS recipe_location
(
    recipe_id     INT,
    location_code TEXT,
    CONSTRAINT pk_RECIPE_LOCATION PRIMARY KEY (recipe_id, location_code),
    CONSTRAINT fk_recipe_location_recipe_id_id FOREIGN KEY (recipe_id) REFERENCES "recipe" (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_recipe_location_location_code_id FOREIGN KEY (location_code) REFERENCES "location" (code) ON DELETE RESTRICT ON UPDATE RESTRICT
);
