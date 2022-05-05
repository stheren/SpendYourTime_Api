CREATE DATABASE SpendYourTime;

-- table for users
CREATE TABLE SpendYourTime.Users (user_id int NOT NULL,
                    email varchar(255) NOT NULL UNIQUE,
                    pseudo varchar(255) NOT NULL UNIQUE,
                    password varchar(255) NOT NULL,
                    CONSTRAINT pk_user_id PRIMARY KEY CLUSTERED (user_id));


-- table for player (user who play the game)
CREATE TABLE SpendYourTime.Player (user_id int NOT NULL,
                     Guild int,
                     position_x int NOT NULL,
                     position_y int NOT NULL,
                     outfit int NOT NULL DEFAULT '0',
                     eyes int NOT NULL DEFAULT '0',
                     accessories int NOT NULL DEFAULT '0',
                     hairstyle int NOT NULL DEFAULT '0',
                     CONSTRAINT pk_player_id PRIMARY KEY CLUSTERED (user_id));

-- table for the map
CREATE TABLE SpendYourTime.Maps (map_id int NOT NULL,
                  map_width int NOT NULL,
                  map_height int NOT NULL,
                  CONSTRAINT pk_map_id PRIMARY KEY CLUSTERED (map_id));

-- table for the guild with a guild_id auto increment, guild_name, owner link at user_id and map_id
CREATE TABLE SpendYourTime.Guilds (guild_id int NOT NULL,
                    guild_name varchar(255) NOT NULL UNIQUE,
                    owner int NOT NULL UNIQUE,
                    map_id int NOT NULL,
                    work_name varchar(255) NOT NULL,
                    CONSTRAINT pk_guild_id PRIMARY KEY CLUSTERED (guild_id));

-- table for the message with a message_id auto increment, message_content, message_date, message_author link at user_id
CREATE TABLE SpendYourTime.Messages (message_id int NOT NULL,
                      message_content varchar(255) NOT NULL,
                      message_date datetime NOT NULL,
                      message_author int NOT NULL,
                      CONSTRAINT pk_message_id PRIMARY KEY CLUSTERED (message_id));

ALTER TABLE SpendYourTime.Player ADD CONSTRAINT fk_player_user_id FOREIGN KEY (user_id) REFERENCES Users(user_id);
ALTER TABLE SpendYourTime.Guilds ADD CONSTRAINT fk_guild_owner FOREIGN KEY (owner) REFERENCES Users(user_id);
ALTER TABLE SpendYourTime.Guilds ADD CONSTRAINT fk_guild_map FOREIGN KEY (map_id) REFERENCES Maps(map_id);
ALTER TABLE SpendYourTime.Messages ADD CONSTRAINT fk_message_author FOREIGN KEY (message_author) REFERENCES Users(user_id);
