CREATE TABLE IF NOT EXISTS ingredient
(
    id          SERIAL PRIMARY KEY,
    code        TEXT NOT NULL UNIQUE,
    chef_code   TEXT NOT NULL,
    recipe_code TEXT NULL,
    source_code TEXT NOT NULL,
    CONSTRAINT fk_ingredient_recipe_code_id FOREIGN KEY (recipe_code) REFERENCES recipe (code) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_ingredient_source_code_id FOREIGN KEY (source_code) REFERENCES "source" (code) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE IF NOT EXISTS ingredient_location
(
    ingredient_id INT,
    location_code TEXT,
    CONSTRAINT pk_INGREDIENT_LOCATION PRIMARY KEY (ingredient_id, location_code),
    CONSTRAINT fk_ingredient_location_ingredient_id_id FOREIGN KEY (ingredient_id) REFERENCES "ingredient" (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_ingredient_location_location_code_id FOREIGN KEY (location_code) REFERENCES "location" (code) ON DELETE RESTRICT ON UPDATE RESTRICT
);
