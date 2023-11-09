package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family;

import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

public record MatchmakerExpansionSettings(boolean enabled,
                                          double interval,
                                          LiquidTimestamp delay,
                                          double maxVariance,
                                          boolean kickPlayers) {}
