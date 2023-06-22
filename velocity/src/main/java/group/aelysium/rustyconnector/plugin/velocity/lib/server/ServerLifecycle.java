package group.aelysium.rustyconnector.plugin.velocity.lib.server;

import group.aelysium.rustyconnector.core.lib.model.ClockService;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;

import java.util.Objects;

public class ServerLifecycle extends ClockService {
    protected final long interval;

    public ServerLifecycle(int threads, long interval) {
        super(true, threads);
        this.interval = interval;
    }

    public void startHeartbeat() {
        this.throwIfDisabled();

        VelocityAPI api = VelocityRustyConnector.getAPI();
        ServerService serverService = api.getService(ServerService.class);

        this.scheduleRecurring(() -> {
            try {
                serverService.getServers().forEach(serverReference -> {
                    PlayerServer server = serverReference.get();
                    if (server == null) return;

                    server.decreaseTimeout();

                    try {
                        if (server.isStale()) {
                            serverService.unregisterServer(server.getServerInfo(), server.getFamilyName(), true);
                            serverService.getServers().remove(serverReference);
                        }
                    } catch (NullPointerException ignore) {}
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                serverService.getServers().removeIf(server -> {
                    if(server.get() == null) return true;
                    try {
                        if (Objects.requireNonNull(server.get()).isStale()) return true;
                    } catch (Exception ignore) {}

                    return false;
                });
            } catch (Exception ignore) {}
        }, 1);
    }
}