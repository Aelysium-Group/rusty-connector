package group.aelysium.rustyconnector.core.mcloader.lib.magic_link;

import group.aelysium.rustyconnector.core.mcloader.central.MCLoaderFlame;
import group.aelysium.rustyconnector.core.mcloader.lib.server_info.ServerInfoService;
import group.aelysium.rustyconnector.toolkit.core.messenger.IMessengerConnection;
import group.aelysium.rustyconnector.toolkit.core.messenger.IMessengerConnector;
import group.aelysium.rustyconnector.toolkit.core.packet.GenericPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketParameter;
import group.aelysium.rustyconnector.toolkit.core.packet.variants.magic_link.HandshakeKillPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.variants.magic_link.HandshakePacket;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.ICoreServiceHandler;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.IMCLoaderFlame;
import group.aelysium.rustyconnector.toolkit.mc_loader.magic_link.IMagicLinkService;
import group.aelysium.rustyconnector.toolkit.mc_loader.magic_link.MagicLinkStatus;
import group.aelysium.rustyconnector.toolkit.core.serviceable.ClockService;
import group.aelysium.rustyconnector.toolkit.mc_loader.server_info.IServerInfoService;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;
import group.aelysium.rustyconnector.core.TinderAdapterForCore;

import java.net.ConnectException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MagicLinkService implements IMagicLinkService {
    private final IMessengerConnector messenger;
    private final ClockService heartbeat = new ClockService(2);
    private final AtomicInteger upcomingPingDelay = new AtomicInteger(5);
    private MagicLinkStatus status = MagicLinkStatus.SEARCHING;

    public MagicLinkService(IMessengerConnector messenger) {
        this.messenger = messenger;
    }

    public void setStatus(MagicLinkStatus status) {
        this.status = status;
    }

    public void setUpcomingPingDelay(int delay) {
        upcomingPingDelay.set(delay);
    }

    private void scheduleNextPing(IMCLoaderFlame<? extends ICoreServiceHandler> api) {
        IServerInfoService serverInfoService = api.services().serverInfo();
        this.heartbeat.scheduleDelayed(() -> {
            try {
                HandshakePacket packet = api.services().packetBuilder().startNew()
                        .identification(PacketIdentification.Predefined.MAGICLINK_HANDSHAKE)
                        .sendingToProxy()
                        .parameter(HandshakePacket.Parameters.ADDRESS, serverInfoService.address())
                        .parameter(HandshakePacket.Parameters.DISPLAY_NAME, serverInfoService.displayName())
                        .parameter(HandshakePacket.Parameters.MAGIC_CONFIG_NAME, serverInfoService.magicConfig())
                        .parameter(HandshakePacket.Parameters.PLAYER_COUNT, new PacketParameter(serverInfoService.playerCount()))
                        .build(HandshakePacket.class);

                api.services().magicLink().connection().orElseThrow().publish(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }

            MagicLinkService.this.scheduleNextPing(api);
        }, LiquidTimestamp.from(this.upcomingPingDelay.get(), TimeUnit.SECONDS));
    }

    public void startHeartbeat(IMCLoaderFlame<? extends ICoreServiceHandler> api) {
        this.scheduleNextPing(api);
    }

    public Optional<IMessengerConnection> connection() {
        return this.messenger.connection();
    }

    public IMessengerConnection connect() throws ConnectException {
        return this.messenger.connect();
    }

    @Override
    public void kill() {
        MCLoaderFlame api = TinderAdapterForCore.getTinder().flame();

        GenericPacket packet = api.services().packetBuilder().startNew()
                .identification(PacketIdentification.Predefined.MAGICLINK_HANDSHAKE_KILL)
                .sendingToProxy()
                .build();
        api.services().magicLink().connection().orElseThrow().publish(packet);

        this.heartbeat.kill();
        this.messenger.kill();
    }
}
