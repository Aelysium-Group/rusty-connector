CREATE TABLE IF NOT EXISTS home_server_mappings (
    player_uuid VARCHAR(36) NOT NULL,
    family_name VARCHAR(32) NOT NULL,
    server_address VARCHAR(128) NOT NULL,
    server_name VARCHAR(128) NOT NULL,
    expiration TIMESTAMP NULL DEFAULT NULL,
    CONSTRAINT uc_Mappings UNIQUE (player_uuid, family_name)
);