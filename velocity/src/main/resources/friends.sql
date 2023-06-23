CREATE TABLE IF NOT EXISTS friends (
    player1 VARCHAR(36) NOT NULL,
    player2 VARCHAR(36) NOT NULL,
    CONSTRAINT uc_Mappings UNIQUE (player1, player2)
);