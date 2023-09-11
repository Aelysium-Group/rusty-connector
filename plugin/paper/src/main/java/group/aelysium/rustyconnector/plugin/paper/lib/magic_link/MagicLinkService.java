package group.aelysium.rustyconnector.plugin.paper.lib.magic_link;

import group.aelysium.rustyconnector.core.lib.packets.variants.ServerPingPacket;
import group.aelysium.rustyconnector.core.lib.model.ClockService;
import group.aelysium.rustyconnector.plugin.paper.central.Tinder;
import group.aelysium.rustyconnector.plugin.paper.lib.services.PacketBuilderService;

import java.util.concurrent.atomic.AtomicInteger;

public class MagicLinkService extends ClockService {
    private AtomicInteger upcomingPingDelay = new AtomicInteger(5);
    private Status status = Status.SEARCHING;

    public MagicLinkService(int threads) {
        super(threads);
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

    private void scheduleNextPing(PacketBuilderService packetBuilderService) {
        this.scheduleDelayed(() -> {
            try {
                packetBuilderService.pingProxy(ServerPingPacket.ConnectionIntent.CONNECT);
            } catch (Exception e) {
                e.printStackTrace();
            }

            MagicLinkService.this.scheduleNextPing(packetBuilderService);
        }, this.upcomingPingDelay.get());
    }

    public void startHeartbeat(PacketBuilderService packetBuilderService) {
        this.scheduleNextPing(packetBuilderService);
    }

    public enum Status {
        CONNECTED,
        SEARCHING,
        DENIED
    }

    public void disconnect() {
        PacketBuilderService service = Tinder.get().services().packetBuilder();
        service.pingProxy(ServerPingPacket.ConnectionIntent.DISCONNECT);
    }

    @Override
    public void kill() {
        super.kill();
        this.disconnect();
    }
}
