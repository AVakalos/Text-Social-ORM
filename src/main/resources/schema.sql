BEGIN;

CREATE TABLE IF NOT EXISTS users(
	user_id SERIAL PRIMARY KEY,
	username VARCHAR(200) UNIQUE NOT NULL,
	password VARCHAR(200) NOT NULL,
	role VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS posts(
	post_id SERIAL PRIMARY KEY,
	user_id integer NOT NULL,
	text varchar(5000) NOT NULL,
    isshared boolean NOT NULL,
	createdat TIMESTAMP NOT NULL,
	FOREIGN KEY(user_id) 
		REFERENCES users(user_id) 
		ON DELETE CASCADE
		ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS comments(
	comment_id SERIAL PRIMARY KEY,
	post_id integer NOT NULL,
	user_id integer NOT NULL ,
	text varchar(1000) NOT NULL,
	createdat TIMESTAMP NOT NULL ,
	FOREIGN KEY(post_id)
		REFERENCES posts(post_id)
		ON DELETE CASCADE
		ON UPDATE CASCADE,
	FOREIGN KEY(user_id)
		REFERENCES users(user_id)
		ON DELETE CASCADE
		ON UPDATE CASCADE		
);

CREATE TABLE IF NOT EXISTS followers(
	user_id BIGINT NOT NULL,
	following_id BIGINT NOT NULL,
	PRIMARY KEY(user_id,following_id),
	FOREIGN KEY(user_id)
		REFERENCES users(user_id)
		ON DELETE CASCADE
		ON UPDATE CASCADE,
	FOREIGN KEY(following_id)
		REFERENCES users(user_id)
		ON DELETE CASCADE
		ON UPDATE CASCADE
);

COMMIT;