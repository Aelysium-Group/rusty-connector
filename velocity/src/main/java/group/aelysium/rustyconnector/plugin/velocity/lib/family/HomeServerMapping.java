package group.aelysium.rustyconnector.plugin.velocity.lib.family;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PlayerServer;

public record HomeServerMapping(Player player, PlayerServer server, StaticServerFamily family) {}
