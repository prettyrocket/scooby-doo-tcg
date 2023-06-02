BEGIN TRANSACTION;

DROP TABLE IF EXISTS users;

CREATE TABLE users (
	user_id SERIAL,
	username varchar(50) NOT NULL UNIQUE,
	password_hash varchar(200) NOT NULL,
	role varchar(50) NOT NULL,
	CONSTRAINT PK_user PRIMARY KEY (user_id)
);

CREATE TABLE character_card (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    species VARCHAR(50),
    occupation VARCHAR(100),
    catchphrase VARCHAR(200),
    image VARCHAR(200)
);

CREATE TABLE production_run (
    id SERIAL PRIMARY KEY,
    product_code INTEGER REFERENCES character_card (id),
    production_date DATE NOT NULL,
    quantity INTEGER,
    status VARCHAR(20),
    notes TEXT
);

COMMIT TRANSACTION;