package group.aelysium.rustyconnector.toolkit.velocity.config;

import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IScoreCard;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

public interface MatchMakerConfig {
    IScoreCard.IRankSchema.Type<?> getAlgorithm();
    int min();
    int max();
    LiquidTimestamp getMatchmakingInterval();
    double getVariance();
}
