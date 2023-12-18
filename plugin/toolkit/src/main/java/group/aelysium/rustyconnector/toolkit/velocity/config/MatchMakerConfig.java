package group.aelysium.rustyconnector.toolkit.velocity.config;

import group.aelysium.rustyconnector.toolkit.core.config.IYAML;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.gameplay.ITeam;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IScoreCard;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

import java.util.List;

public interface MatchMakerConfig {
    IScoreCard.IRankSchema.Type<?> getAlgorithm();
    List<ITeam.Settings> getTeams();
    LiquidTimestamp getMatchmakingInterval();
    double getVariance();
}
