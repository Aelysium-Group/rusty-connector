package group.aelysium.rustyconnector.core.plugin.lib.magic_link;

import group.aelysium.rustyconnector.toolkit.mc_loader.connection_intent.ConnectionIntent;
import group.aelysium.rustyconnector.toolkit.mc_loader.magic_link.IMagicLinkService;
import group.aelysium.rustyconnector.toolkit.mc_loader.magic_link.MagicLinkStatus;
import group.aelysium.rustyconnector.toolkit.core.serviceable.ClockService;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;
import group.aelysium.rustyconnector.core.TinderAdapterForCore;
import group.aelysium.rustyconnector.core.plugin.central.CoreServiceHandler;
import group.aelysium.rustyconnector.core.plugin.lib.packet_builder.PacketBuilderService;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MagicLinkService extends ClockService implements IMagicLinkService<PacketBuilderService> {
    private AtomicInteger upcomingPingDelay = new AtomicInteger(5);
    private MagicLinkStatus status = MagicLinkStatus.SEARCHING;

    public MagicLinkService(int threads) {
        super(threads);
    }

    public void setStatus(MagicLinkStatus status) {
        this.status = status;
    }

    public void setUpcomingPingDelay(int delay) {
        upcomingPingDelay.set(delay);
    }

    private void scheduleNextPing(PacketBuilderService packetBuilderService) {
        this.scheduleDelayed(() -> {
            try {
                packetBuilderService.pingProxy(ConnectionIntent.CONNECT);
            } catch (Exception e) {
                e.printStackTrace();
            }

            MagicLinkService.this.scheduleNextPing(packetBuilderService);
        }, LiquidTimestamp.from(this.upcomingPingDelay.get(), TimeUnit.SECONDS));
    }

    public void startHeartbeat(PacketBuilderService packetBuilderService) {
        this.scheduleNextPing(packetBuilderService);
    }

    public void disconnect() {
        PacketBuilderService service = ((CoreServiceHandler) TinderAdapterForCore.getTinder().services()).packetBuilder();
        service.pingProxy(ConnectionIntent.DISCONNECT);
    }

    @Override
    public void kill() {
        super.kill();
        this.disconnect();
    }
}
