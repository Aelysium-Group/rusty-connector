package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa;

import group.aelysium.rustyconnector.core.lib.model.LiquidTimestamp;

import java.util.List;

public record TPASettings (boolean friendsOnly, boolean ignorePlayerCap, LiquidTimestamp expiration, List<String> enabledFamilies) {}
