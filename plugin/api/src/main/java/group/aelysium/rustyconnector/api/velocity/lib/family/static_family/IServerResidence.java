package group.aelysium.rustyconnector.api.velocity.lib.family.static_family;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.api.velocity.lib.family.IResolvableFamily;
import group.aelysium.rustyconnector.api.velocity.lib.family.bases.IBaseFamily;
import group.aelysium.rustyconnector.api.velocity.lib.players.IResolvablePlayer;
import group.aelysium.rustyconnector.api.velocity.lib.server.IPlayerServer;
import group.aelysium.rustyconnector.api.velocity.lib.server.IResolvableServer;
import group.aelysium.rustyconnector.api.velocity.lib.util.LiquidTimestamp;

import java.util.Optional;

public interface IServerResidence {
    Optional<Player> player();
    IResolvablePlayer rawPlayer();

    Optional<? extends IPlayerServer> server();
    IResolvableServer rawServer();

    Optional<? extends IBaseFamily> family();
    IResolvableFamily rawFamily();

    Long expiration();

    void expiration(LiquidTimestamp expiration);
}