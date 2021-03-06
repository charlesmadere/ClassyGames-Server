CREATE TABLE IF NOT EXISTS games
(
	id VARCHAR(255) NOT NULL,
	user_creator BIGINT NOT NULL,
	user_challenged BIGINT NOT NULL,
	board TEXT NOT NULL,
	turn TINYINT NOT NULL,
	game_type TINYINT NOT NULL,
	last_move TIMESTAMP NOT NULL DEFAULT NOW(),
	finished TINYINT NOT NULL,
	PRIMARY KEY (id),
	FOREIGN KEY (user_creator) REFERENCES users(id),
	FOREIGN KEY (user_challenged) REFERENCES users(id)
);
