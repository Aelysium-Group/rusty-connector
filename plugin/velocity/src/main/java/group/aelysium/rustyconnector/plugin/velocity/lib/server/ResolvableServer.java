package group.aelysium.rustyconnector.plugin.velocity.lib.server;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.api.velocity.server.IResolvableServer;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ResolvableFamily;

import java.util.Optional;
import java.util.UUID;

public class ResolvableServer implements IResolvableServer {
    protected UUID uuid;
    protected ServerInfo serverInfo;
    protected ResolvableFamily family;

    public ResolvableServer(UUID uuid, ServerInfo serverInfo, ResolvableFamily family) {
        this.uuid = uuid;
        this.serverInfo = serverInfo;
        this.family = family;
    }

    public UUID uuid() {
        return uuid;
    }

    public ServerInfo serverInfo() {
        return serverInfo;
    }

    public ResolvableFamily family() {
        return family;
    }

    public Optional<PlayerServer> resolve() {
        PlayerServer potentialPlayerServer = Tinder.get().services().server().search(this.serverInfo);
        if(potentialPlayerServer == null) return Optional.empty();
        return Optional.of(potentialPlayerServer);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        ResolvableServer that = (ResolvableServer) object;
        return this.serverInfo.equals(that.serverInfo());
    }

    public static ResolvableServer from(PlayerServer server) {
        return new ResolvableServer(server.id(), server.serverInfo(), ResolvableFamily.from(server.family()));
    }
}
