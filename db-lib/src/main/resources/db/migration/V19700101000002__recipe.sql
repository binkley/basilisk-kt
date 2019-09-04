CREATE TABLE IF NOT EXISTS recipe
(
    code      TEXT NOT NULL PRIMARY KEY,
    "name"    TEXT NOT NULL,
    chef_code TEXT NOT NULL,
    status    TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS recipe_location
(
    recipe_code   TEXT,
    location_code TEXT,
    CONSTRAINT pk_RECIPE_LOCATION PRIMARY KEY (recipe_code, location_code),
    CONSTRAINT fk_recipe_location_recipe_code_id FOREIGN KEY (recipe_code) REFERENCES "recipe" (code) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_recipe_location_location_code_id FOREIGN KEY (location_code) REFERENCES "location" (code) ON DELETE RESTRICT ON UPDATE RESTRICT
);
