package group.aelysium.rustyconnector.plugin.velocity.lib.magic_link;

import group.aelysium.rustyconnector.toolkit.core.serviceable.ClockService;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;

import java.util.Objects;

public class MagicLinkService extends ClockService {
    protected final long interval;

    public MagicLinkService(int threads, long interval) {
        super(threads);
        this.interval = interval;
    }

    public void startHeartbeat(ServerService serverService) {
        this.scheduleRecurring(() -> {
            try {
                // Unregister any stale servers
                // The removing feature of server#unregister is valid because serverService.servers() creates a new list which isn't bound to the underlying list.
                serverService.servers().forEach(server -> {
                    server.decreaseTimeout();

                    try {
                        if (server.stale()) server.unregister(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception ignore) {}
        }, 1, 5);
    }
}
