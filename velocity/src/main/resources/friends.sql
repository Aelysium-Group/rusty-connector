CREATE TABLE IF NOT EXISTS friends (
    player1_uuid VARCHAR(36) NOT NULL,
    player2_uuid VARCHAR(36) NOT NULL,
    CONSTRAINT uc_Mappings UNIQUE (player1_uuid, player2_uuid)
);

CREATE TABLE IF NOT EXISTS requests (
    sender_UUID VARCHAR(36) NOT NULL,
    target_UUID VARCHAR(36) NOT NULL,
    CONSTRAINT uc_Mappings UNIQUE (sender_UUID, target_UUID)
);