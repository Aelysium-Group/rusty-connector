package group.aelysium.rustyconnector.plugin.velocity.lib.magic_link;

import group.aelysium.rustyconnector.core.lib.model.ClockService;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;

import java.util.Objects;

public class MagicLinkService extends ClockService {
    protected final long interval;

    public MagicLinkService(int threads, long interval) {
        super(threads);
        this.interval = interval;
    }

    public void startHeartbeat() {
        VelocityAPI api = VelocityAPI.get();
        ServerService serverService = api.services().serverService();

        this.scheduleRecurring(() -> {
            try {
                serverService.servers().forEach(serverReference -> {
                    PlayerServer server = serverReference.get();
                    if (server == null) return;

                    server.decreaseTimeout();

                    try {
                        if (server.stale()) {
                            serverService.unregisterServer(server.serverInfo(), server.family().name(), true);
                            serverService.servers().remove(serverReference);
                        }
                    } catch (NullPointerException ignore) {}
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                serverService.servers().removeIf(server -> {
                    if(server.get() == null) return true;
                    try {
                        if (Objects.requireNonNull(server.get()).stale()) return true;
                    } catch (Exception ignore) {}

                    return false;
                });
            } catch (Exception ignore) {}
        }, 1);
    }
}
