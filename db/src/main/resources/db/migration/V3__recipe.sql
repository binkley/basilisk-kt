CREATE TABLE IF NOT EXISTS recipe
(
    id      SERIAL PRIMARY KEY,
    "name"  TEXT NOT NULL,
    code    TEXT NOT NULL UNIQUE,
    chef_id INT  NOT NULL,
    status  TEXT NOT NULL,
    CONSTRAINT fk_recipe_chef_id_id FOREIGN KEY (chef_id) REFERENCES chef (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE IF NOT EXISTS recipe_location
(
    recipe_id   INT,
    location_id INT,
    CONSTRAINT pk_RECIPE_LOCATION PRIMARY KEY (recipe_id, location_id),
    CONSTRAINT fk_recipe_location_recipe_id_id FOREIGN KEY (recipe_id) REFERENCES "recipe" (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_recipe_location_location_id_id FOREIGN KEY (location_id) REFERENCES "location" (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
