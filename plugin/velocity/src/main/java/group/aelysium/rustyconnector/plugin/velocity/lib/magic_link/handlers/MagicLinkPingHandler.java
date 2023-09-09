package group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.handlers;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnection;
import group.aelysium.rustyconnector.core.lib.packets.PacketHandler;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.core.lib.packets.PacketOrigin;
import group.aelysium.rustyconnector.core.lib.packets.PacketType;
import group.aelysium.rustyconnector.core.lib.packets.variants.ServerPingPacket;
import group.aelysium.rustyconnector.core.lib.packets.variants.ServerPingResponsePacket;
import group.aelysium.rustyconnector.core.lib.lang_messaging.GateKey;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import net.kyori.adventure.text.format.NamedTextColor;

import java.net.InetSocketAddress;

public class MagicLinkPingHandler implements PacketHandler {
    private final ServerPingPacket message;

    public MagicLinkPingHandler(GenericPacket message) {
        this.message = (ServerPingPacket) message;
    }

    @Override
    public void execute() throws Exception {
        InetSocketAddress address = message.address();
        VelocityAPI api = VelocityAPI.get();

        ServerInfo serverInfo = new ServerInfo(
                message.serverName(),
                address
        );

        if(api.logger().loggerGate().check(GateKey.PING))
            api.logger().send(VelocityLang.PING.build(serverInfo));

        if(message.intent() == ServerPingPacket.ConnectionIntent.CONNECT)
            this.reviveOrConnectServer(serverInfo);
        if(message.intent() == ServerPingPacket.ConnectionIntent.DISCONNECT)
            this.disconnectServer(serverInfo);
    }

    private boolean connectServer(ServerInfo serverInfo) {
        VelocityAPI api = VelocityAPI.get();
        ServerService serverService = api.services().serverService();
        MessengerConnection<?> backboneMessenger = api.core().backbone().connection().orElseThrow();

        try {
            PlayerServer server = new ServerService.ServerBuilder()
                    .setServerInfo(serverInfo)
                    .setFamilyName(message.familyName())
                    .setSoftPlayerCap(message.softCap())
                    .setHardPlayerCap(message.hardCap())
                    .setWeight(message.weight())
                    .build();

            server.register(message.familyName());

            ServerPingResponsePacket message = (ServerPingResponsePacket) new GenericPacket.Builder()
                    .setType(PacketType.PING_RESPONSE)
                    .setAddress(serverInfo.getAddress())
                    .setOrigin(PacketOrigin.PROXY)
                    .setParameter(ServerPingResponsePacket.ValidParameters.STATUS, String.valueOf(ServerPingResponsePacket.PingResponseStatus.ACCEPTED))
                    .setParameter(ServerPingResponsePacket.ValidParameters.MESSAGE, "Connected to the proxy!")
                    .setParameter(ServerPingResponsePacket.ValidParameters.COLOR, NamedTextColor.GREEN.toString())
                    .setParameter(ServerPingResponsePacket.ValidParameters.INTERVAL_OPTIONAL, String.valueOf(serverService.serverInterval()))
                    .buildSendable();
            backboneMessenger.publish(message);

            return true;
        } catch(Exception e) {
            ServerPingResponsePacket message = (ServerPingResponsePacket) new GenericPacket.Builder()
                    .setType(PacketType.PING_RESPONSE)
                    .setAddress(serverInfo.getAddress())
                    .setOrigin(PacketOrigin.PROXY)
                    .setParameter(ServerPingResponsePacket.ValidParameters.STATUS, String.valueOf(ServerPingResponsePacket.PingResponseStatus.DENIED))
                    .setParameter(ServerPingResponsePacket.ValidParameters.MESSAGE, "Attempt to connect to proxy failed! " + e.getMessage())
                    .setParameter(ServerPingResponsePacket.ValidParameters.COLOR, NamedTextColor.RED.toString())
                    .buildSendable();
            backboneMessenger.publish(message);
        }
        return false;
    }

    private boolean disconnectServer(ServerInfo serverInfo) throws Exception {
        VelocityAPI api = VelocityAPI.get();
        api.services().serverService().unregisterServer(serverInfo, message.familyName(), true);

        return true;
    }

    private boolean reviveOrConnectServer(ServerInfo serverInfo) {
        VelocityAPI api = VelocityAPI.get();
        ServerService serverService = api.services().serverService();

        PlayerServer server = serverService.search(serverInfo);
        if (server == null) {
            return this.connectServer(serverInfo);
        }

        server.setTimeout(serverService.serverTimeout());
        server.setPlayerCount(this.message.playerCount());
        return true;
    }
}
