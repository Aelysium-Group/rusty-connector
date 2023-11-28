package group.aelysium.rustyconnector.toolkit.velocity.family.static_family;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.util.Reference;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

public interface IServerResidence {
    IPlayer player();
    Reference<? extends IPlayer, ?> rawPlayer();

    IMCLoader server();
    Reference<? extends IMCLoader, ServerInfo> rawServer();

    IFamily family();

    Long expiration();

    void expiration(LiquidTimestamp expiration);
}