package group.aelysium.rustyconnector.toolkit.velocity.family.static_family;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.toolkit.velocity.family.Family;
import group.aelysium.rustyconnector.toolkit.velocity.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.server.MCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.util.Reference;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

public interface IServerResidence {
    Player player();
    Reference<? extends Player, ?> rawPlayer();

    MCLoader server();
    Reference<? extends MCLoader, ServerInfo> rawServer();

    Family family();

    Long expiration();

    void expiration(LiquidTimestamp expiration);
}