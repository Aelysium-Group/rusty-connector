package group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa;

import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

import java.util.List;

public record TPAServiceSettings(boolean friendsOnly, boolean ignorePlayerCap, LiquidTimestamp expiration, List<String> enabledFamilies) {}
