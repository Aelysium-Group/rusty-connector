package group.aelysium.rustyconnector.plugin.velocity.lib;

import group.aelysium.rustyconnector.RC;
import group.aelysium.ara.Particle;
import group.aelysium.rustyconnector.common.crypt.NanoID;
import group.aelysium.rustyconnector.proxy.events.ServerUnregisterEvent;
import group.aelysium.rustyconnector.proxy.family.Family;
import group.aelysium.rustyconnector.proxy.family.FamilyRegistry;
import group.aelysium.rustyconnector.proxy.family.Server;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides adaptive resources for servers on Velocity networks.
 * UUIDs are not user/plugin friendly, so to provide better experiences for other plugins not integrated with RC, "server registrations" are used instead.
 */
public class ServerRegistry {
    private final Map<UUID, String> servers = new ConcurrentHashMap<>();

    public ServerRegistry() {}

    /**
     * Finds a server registration based on it's uuid.
     * @param uuid The uuid to search for.
     * @return An optional containing the server's registration if there is one, empty if there isn't.
     */
    public Optional<String> find(@NotNull UUID uuid) {
        return Optional.ofNullable(this.servers.get(uuid));
    }

    /**
     * Registers a new server to the registry.
     * This registry exists to track servers inside the RC environment.
     * This method handles a very specific task, you should only use it if you know what you're doing.
     * If you're just trying to create and register a new server to RustyConnector, you should use {@link group.aelysium.rustyconnector.proxy.ProxyKernel#registerServer(Particle.Flux, Server.Configuration)}
     * @param server The server to register.
     * @return The newly generated registration for that server.
     * @throws NoSuchElementException If the server doesn't have a family or if the family isn't available.
     */
    public void register(@NotNull Server server) throws NoSuchElementException {
        Family family = server.family().orElseThrow().orElseThrow();
        String registration = family.id() + "-" + NanoID.randomNanoID(12);
        this.servers.put(server.uuid(), registration);
        server.property("velocity_registration_name", registration);
    }

    /**
     * Registers a server from the proxy.
     * @param server The server to unregister.
     */
    public void unregister(@NotNull Server server) {
        RC.P.EventManager().fireEvent(new ServerUnregisterEvent(server));

        RC.P.Adapter().unregisterServer(server);
        this.servers.remove(server.uuid());
    }

    /**
     * Unregisters the registration mapping.
     * @param uuid The uuid of the server.
     */
    public void unregister(@NotNull UUID uuid) {
        this.servers.remove(uuid);
    }

    public void close() {
        this.servers.clear();
    }
}
