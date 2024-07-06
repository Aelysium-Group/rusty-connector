package group.aelysium.rustyconnector.mcloader.magic_link;

import group.aelysium.rustyconnector.common.magic_link.Packet;
import group.aelysium.rustyconnector.common.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.toolkit.RC;
import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;
import group.aelysium.rustyconnector.toolkit.common.crypt.AESCryptor;
import group.aelysium.rustyconnector.toolkit.common.magic_link.IMagicLink;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.PacketParameter;
import group.aelysium.rustyconnector.toolkit.mc_loader.events.magic_link.DisconnectedEvent;
import group.aelysium.rustyconnector.toolkit.proxy.util.LiquidTimestamp;
import org.jetbrains.annotations.NotNull;

import java.net.ConnectException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MagicLink implements IMagicLink.MCLoader {
    private final IMessengerConnector messenger;
    private final ClockService heartbeat = new ClockService(2);
    private final AtomicInteger delay = new AtomicInteger(5);
    private final String podName = System.getenv("POD_NAME");
    private final AtomicBoolean stopPinging = new AtomicBoolean(false);

    public MagicLink(IMessengerConnector messenger) {
        this.messenger = messenger;
    }

    @Override
    public String magicConfig() {
        return null;
    }

    public void setDelay(int delay) {
        this.delay.set(delay);
    }

    private void scheduleNextPing(IMCLoaderFlame<? extends ICoreServiceHandler> api) {
        IServerInfoService serverInfoService = api.services().serverInfo();
        this.heartbeat.scheduleDelayed(() -> {
            if(stopPinging.get()) return;

            try {
                Packet.MCLoaderPacketBuilder.ReadyForParameters packet = RC.P.MagicLink().packetBuilder().newBuilder()
                        .identification(BuiltInIdentifications.MAGICLINK_HANDSHAKE_PING)
                        .sendingToProxy()
                        .parameter(group.aelysium.rustyconnector.common.packets.MagicLink.Handshake.Ping.Parameters.ADDRESS, serverInfoService.address())
                        .parameter(group.aelysium.rustyconnector.common.packets.MagicLink.Handshake.Ping.Parameters.DISPLAY_NAME, serverInfoService.displayName())
                        .parameter(group.aelysium.rustyconnector.common.packets.MagicLink.Handshake.Ping.Parameters.MAGIC_CONFIG_NAME, serverInfoService.magicConfig())
                        .parameter(group.aelysium.rustyconnector.common.packets.MagicLink.Handshake.Ping.Parameters.PLAYER_COUNT, new PacketParameter(serverInfoService.playerCount()));

                if(podName != null)
                    packet.parameter(group.aelysium.rustyconnector.common.packets.MagicLink.Handshake.Ping.Parameters.POD_NAME, this.podName);

                RC.P.MagicLink().connection().orElseThrow().publish(packet.build());
            } catch (Exception e) {
                e.printStackTrace();
            }

            MagicLink.this.scheduleNextPing(api);
        }, LiquidTimestamp.from(this.delay.get(), TimeUnit.SECONDS));
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
    public void close() throws Exception {
        stopPinging.set(true);
        try {
            this.heartbeat.kill();
        } catch (Exception ignore) {}

        try {
            group.aelysium.rustyconnector.mcloader.MCLoaderFlame api = TinderAdapterForCore.getTinder().flame();

            Packet.New()
                    .identification(BuiltInIdentifications.MAGICLINK_HANDSHAKE_DISCONNECT)
                    .addressedTo(IPacket.Target.allAvailableProxies())
                    .send();
            this.connection().orElseThrow().publish(packet);

            api.services().events().fireEvent(new DisconnectedEvent());
        } catch (Exception ignore) {}

        this.messenger.kill();
    }

    public static class Tinder extends Particle.Tinder<IMagicLink.MCLoader> {
        private AESCryptor cryptor;

        public Tinder() {}

        public void cryptor(AESCryptor cryptor) {
            this.cryptor = cryptor;
        }


        @Override
        public @NotNull IMagicLink.Proxy ignite() throws Exception {
        }

    }
}
