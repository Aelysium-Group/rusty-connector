package group.aelysium.rustyconnector.toolkit.velocity.config;

import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchmaker;

public interface MatchMakerConfig {
    IMatchmaker.Settings settings();
}
