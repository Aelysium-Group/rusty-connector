package group.aelysium.rustyconnector.plugin.paper.lib.services;

import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageServerPing;
import group.aelysium.rustyconnector.core.lib.model.ClockService;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;

import java.util.concurrent.atomic.AtomicInteger;

public class ProxyConnectorService extends ClockService {
    private AtomicInteger upcomingPingDelay = new AtomicInteger(0);
    private AtomicInteger nextcomingPingDelay = new AtomicInteger(5);
    private Status status = Status.SEARCHING;

    public ProxyConnectorService(int threads) {
        super(true, threads);
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Set the ping delay for this upcoming ping.
     * @param delay The delay to set.
     */
    public void setUpcomingPingDelay(int delay) {
        upcomingPingDelay.set(delay);
    }

    /**
     * Set the ping delay for all pings after this upcoming one.
     * @param delay The delay to set.
     */
    public void setNextcomingPingDelay(int delay) {
        nextcomingPingDelay.set(delay);
    }

    private void scheduleNextPing() {
        PluginLogger logger = PaperRustyConnector.getAPI().getLogger();
        RedisMessagerService service = PaperRustyConnector.getAPI().getService(RedisMessagerService.class);

        this.scheduleDelayed(() -> {
            try {
                if(this.status == Status.SEARCHING)
                    logger.log("Searching for proxy...");
                service.pingProxy(RedisMessageServerPing.ConnectionIntent.CONNECT);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ProxyConnectorService.this.scheduleNextPing();
        }, this.upcomingPingDelay.get());

        if(this.upcomingPingDelay.get() != this.nextcomingPingDelay.get()) this.upcomingPingDelay = this.nextcomingPingDelay;
    }

    public void startHeartbeat() {
        this.throwIfDisabled();

        this.scheduleNextPing();
    }

    public enum Status {
        CONNECTED,
        SEARCHING,
        DENIED
    }

    public void disconnect() {
        RedisMessagerService service = PaperRustyConnector.getAPI().getService(RedisMessagerService.class);
        service.pingProxy(RedisMessageServerPing.ConnectionIntent.DISCONNECT);
    }

    @Override
    public void kill() {
        super.kill();
    }
}
