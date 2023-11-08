CREATE TABLE IF NOT EXISTS users(
	user_id SERIAL PRIMARY KEY,
	username VARCHAR(200) UNIQUE NOT NULL,
	password VARCHAR(200) NOT NULL,
	role VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS posts(
	post_id SERIAL PRIMARY KEY,
	user_id integer,
	text varchar(5000),
	created TIMESTAMP,
	FOREIGN KEY(user_id) 
		REFERENCES users(user_id) 
		ON DELETE CASCADE
		ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS comments(
	comment_id SERIAL PRIMARY KEY,
	post_id integer,
	user_id integer,
	text varchar(1000),
	created TIMESTAMP,
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
	user_id integer,
	following_id integer,
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

CREATE TABLE IF NOT EXISTS links(
	user_id integer,
	post_id integer,
	PRIMARY KEY(user_id, post_id),
	FOREIGN KEY(user_id)
		REFERENCES users(user_id)
		ON DELETE CASCADE
		ON UPDATE CASCADE,
	FOREIGN KEY(post_id)
		REFERENCES posts(post_id)
		ON DELETE CASCADE
		ON UPDATE CASCADE		
);