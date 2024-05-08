package group.aelysium.rustyconnector.plugin.velocity.lib.magic_link;

import group.aelysium.rustyconnector.core.lib.data_transit.DataTransitService;
import group.aelysium.rustyconnector.core.lib.messenger.MessengerConnection;
import group.aelysium.rustyconnector.core.lib.messenger.MessengerConnector;
import group.aelysium.rustyconnector.core.lib.messenger.implementors.redis.RedisConnection;
import group.aelysium.rustyconnector.core.lib.messenger.implementors.redis.RedisConnector;
import group.aelysium.rustyconnector.toolkit.core.messenger.IMessengerConnection;
import group.aelysium.rustyconnector.toolkit.core.messenger.IMessengerConnector;
import group.aelysium.rustyconnector.toolkit.core.serviceable.ClockService;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import group.aelysium.rustyconnector.toolkit.velocity.magic_link.IMagicLink;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MagicLinkService extends ClockService implements IMagicLink {
    protected final long interval;
    protected IMessengerConnector redisConnector;
    protected Map<String, MagicLinkMCLoaderSettings> settingsMap;

    public MagicLinkService(long interval, IMessengerConnector redisConnector, Map<String, MagicLinkMCLoaderSettings> magicLinkMCLoaderSettingsMap) {
        super(2);
        this.interval = interval;
        this.redisConnector = redisConnector;
        this.settingsMap = magicLinkMCLoaderSettingsMap;
    }

    public void startHeartbeat(ServerService serverService) {
        this.scheduleRecurring(() -> {
            try {
                // Unregister any stale servers
                // The removing feature of server#unregister is valid because serverService.servers() creates a new list which isn't bound to the underlying list.
                serverService.servers().forEach(server -> {
                    server.decreaseTimeout(3);

                    try {
                        if (server.stale()) server.unregister(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception ignore) {}
        }, 3, 5); // Period of `3` lets us not loop over the servers as many times with a small hit to how quickly stale servers will be unregistered.
    }

    public Optional<MagicLinkMCLoaderSettings> magicConfig(String name) {
        MagicLinkMCLoaderSettings settings = this.settingsMap.get(name);
        if(settings == null) return Optional.empty();
        return Optional.of(settings);
    }

    /**
     * Get the {@link MessengerConnection} created from this {@link MessengerConnector}.
     * @return An {@link Optional} possibly containing a {@link MessengerConnection}.
     */
    public Optional<IMessengerConnection> connection() {
        return this.redisConnector.connection();
    }

    /**
     * Connect to the remote resource.
     * @return A {@link MessengerConnection}.
     * @throws ConnectException If there was an issue connecting to the remote resource.
     */
    public IMessengerConnection connect() throws ConnectException {
        return this.redisConnector.connect();
    }

    @Override
    public void kill() {
        this.redisConnector.kill();
        super.kill();
    }
}
