package group.aelysium.rustyconnector.toolkit.velocity.server;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.toolkit.velocity.family.IResolvableFamily;

import java.util.Optional;
import java.util.UUID;

public interface IResolvableServer {
    UUID uuid();

    ServerInfo serverInfo();

    IResolvableFamily family();

    Optional<? extends IPlayerServer> resolve();

    boolean equals(Object object);
}
