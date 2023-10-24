package group.aelysium.rustyconnector.api.velocity.dynamic_teleport.tpa;

import group.aelysium.rustyconnector.api.velocity.util.LiquidTimestamp;

import java.util.List;

public record TPAServiceSettings(boolean friendsOnly, boolean ignorePlayerCap, LiquidTimestamp expiration, List<String> enabledFamilies) {}
