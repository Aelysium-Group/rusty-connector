package group.aelysium.rustyconnector.plugin.velocity.lib.family;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.static_family.StaticServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;

public record HomeServerMapping(Player player, PlayerServer server, StaticServerFamily family) {}
