CREATE TABLE IF NOT EXISTS ingredient
(
    id        SERIAL PRIMARY KEY,
    code      TEXT NOT NULL UNIQUE,
    chef_code TEXT NOT NULL,
    recipe_id INT  NULL,
    source_id INT  NOT NULL,
    CONSTRAINT fk_ingredient_recipe_id_id FOREIGN KEY (recipe_id) REFERENCES recipe (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_ingredient_source_id_id FOREIGN KEY (source_id) REFERENCES "source" (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE IF NOT EXISTS ingredient_location
(
    ingredient_id INT,
    location_code TEXT,
    CONSTRAINT pk_INGREDIENT_LOCATION PRIMARY KEY (ingredient_id, location_code),
    CONSTRAINT fk_ingredient_location_ingredient_id_id FOREIGN KEY (ingredient_id) REFERENCES "ingredient" (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_ingredient_location_location_code_id FOREIGN KEY (location_code) REFERENCES "location" (code) ON DELETE RESTRICT ON UPDATE RESTRICT
);
