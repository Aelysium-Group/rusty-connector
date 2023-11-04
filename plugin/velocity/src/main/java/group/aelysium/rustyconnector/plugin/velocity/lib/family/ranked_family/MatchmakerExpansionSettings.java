package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family;

import group.aelysium.rustyconnector.api.velocity.util.LiquidTimestamp;

public record MatchmakerExpansionSettings(boolean allowed,
                                          double interval,
                                          LiquidTimestamp delay,
                                          double maxVariance,
                                          boolean kickPlayers) {}
