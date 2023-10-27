CREATE TABLE IF NOT EXISTS friends (
    player1_uuid VARCHAR(36) NOT NULL,
    player2_uuid VARCHAR(36) NOT NULL,
    CONSTRAINT uc_Mappings UNIQUE (player1_uuid, player2_uuid)
);