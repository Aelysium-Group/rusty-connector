CREATE TABLE home_server_mappings (
    player_uuid UUID NOT NULL,
    family_name VARCHAR(32) NOT NULL,
    server_address VARCHAR(128) NOT NULL,
    server_name VARCHAR(128) NOT NULL,
    expiration TIMESTAMP DEFAULT null;
    CONSTRAINT uc_Mappings UNIQUE (uuid,family)
);