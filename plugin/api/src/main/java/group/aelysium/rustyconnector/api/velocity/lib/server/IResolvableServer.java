package group.aelysium.rustyconnector.api.velocity.lib.server;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.api.velocity.lib.family.IResolvableFamily;

import java.util.Optional;
import java.util.UUID;

public interface IResolvableServer {
    UUID uuid();

    ServerInfo serverInfo();

    IResolvableFamily family();

    Optional<? extends IPlayerServer> resolve();

    boolean equals(Object object);
}
