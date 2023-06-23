CREATE TABLE IF NOT EXISTS home_server_mappings (
    player_uuid VARCHAR(36) NOT NULL,
    family_name VARCHAR(32) NOT NULL,
    server_address VARCHAR(128) NOT NULL,
    server_name VARCHAR(128) NOT NULL,
    expiration TIMESTAMP NULL DEFAULT NULL,
    CONSTRAINT uc_Mappings UNIQUE (player_uuid, family_name)
);

CREATE TABLE IF NOT EXISTS friends (
    player_uuid VARCHAR(36) NOT NULL,
    friend_uuid VARCHAR(36) NOT NULL,
    CONSTRAINT uc_Mappings UNIQUE (player_uuid, friend_uuid)
);