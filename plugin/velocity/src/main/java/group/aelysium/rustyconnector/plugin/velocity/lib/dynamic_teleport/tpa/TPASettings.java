package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa;

import group.aelysium.rustyconnector.api.velocity.lib.util.LiquidTimestamp;

import java.util.List;

public record TPASettings (boolean friendsOnly, boolean ignorePlayerCap, LiquidTimestamp expiration, List<String> enabledFamilies) {}
