package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family;

import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.RankedGameRankerType;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.RankedGameScoringType;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.solo.RankedSoloGame;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.teams.RankedTeamGame;

public record RankedMatchmakerSettings(
        RankedGameRankerType type,
        RankedSoloGame.Settings soloSettings,
        RankedTeamGame.Settings teamSettings,
        RankedGameScoringType scoringType,
        double variance,
        LiquidTimestamp interval
) {}