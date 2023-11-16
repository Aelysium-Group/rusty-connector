package group.aelysium.rustyconnector.toolkit.velocity.family.static_family;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.toolkit.velocity.family.IResolvableFamily;
import group.aelysium.rustyconnector.toolkit.velocity.family.bases.IBaseFamily;
import group.aelysium.rustyconnector.toolkit.velocity.players.IRustyPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IPlayerServer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IResolvableServer;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

import java.util.Optional;

public interface IServerResidence {
    Optional<Player> player();
    IRustyPlayer rawPlayer();

    Optional<? extends IPlayerServer> server();
    IResolvableServer rawServer();

    Optional<? extends IBaseFamily> family();
    IResolvableFamily rawFamily();

    Long expiration();

    void expiration(LiquidTimestamp expiration);
}