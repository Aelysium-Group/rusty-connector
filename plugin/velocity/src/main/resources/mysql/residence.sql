-- ################################################ --
--  MIGRATE TABLE TO v0.7.0 FROM PREVIOUS VERSIONS  --
RENAME TABLE home_server_mappings TO residence;
-- ################################################ --

CREATE TABLE IF NOT EXISTS residence (
    player_uuid VARCHAR(36) NOT NULL,
    family_name VARCHAR(32) NOT NULL,
    server_address VARCHAR(128) NOT NULL,
    server_name VARCHAR(128) NOT NULL,
    expiration TIMESTAMP NULL DEFAULT NULL,
    CONSTRAINT uc_Mappings UNIQUE (player_uuid, family_name)
);