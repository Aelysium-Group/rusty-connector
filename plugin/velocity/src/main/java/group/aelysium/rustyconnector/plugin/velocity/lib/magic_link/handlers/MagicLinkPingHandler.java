package group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.handlers;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.plugin.velocity.central.config.MagicDefaultConfig;
import group.aelysium.rustyconnector.toolkit.core.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.mc_loader.connection_intent.ConnectionIntent;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketHandler;
import group.aelysium.rustyconnector.core.lib.messenger.implementors.redis.RedisConnection;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketOrigin;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketType;
import group.aelysium.rustyconnector.core.lib.packets.variants.ServerPingPacket;
import group.aelysium.rustyconnector.core.lib.packets.variants.ServerPingResponsePacket;
import group.aelysium.rustyconnector.toolkit.core.log_gate.GateKey;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import net.kyori.adventure.text.format.NamedTextColor;

import java.net.InetSocketAddress;

public class MagicLinkPingHandler implements PacketHandler {
    protected Tinder api;

    public MagicLinkPingHandler(Tinder api) {
        this.api = api;
    }

    @Override
    public <TPacket extends IPacket> void execute(TPacket genericPacket) throws Exception {
        ServerPingPacket packet = (ServerPingPacket) genericPacket;

        InetSocketAddress address = packet.address();

        ServerInfo serverInfo = new ServerInfo(
                packet.serverName(),
                address
        );

        if(api.logger().loggerGate().check(GateKey.PING))
            api.logger().send(VelocityLang.PING.build(serverInfo));

        if(packet.intent() == ConnectionIntent.CONNECT)
            reviveOrConnectServer(api, serverInfo, packet);
        if(packet.intent() == ConnectionIntent.DISCONNECT)
            disconnectServer(api, serverInfo, packet);
    }

    private static void connectServer(Tinder api, ServerInfo serverInfo, ServerPingPacket packet) {
        ServerService serverService = api.services().server();
        RedisConnection backboneMessenger = api.flame().backbone().connection().orElseThrow();
        MagicDefaultConfig magicDefaultConfig = new MagicDefaultConfig(api.dataFolder(), packet.magicConfigName());
        magicDefaultConfig.register();

        try {
            PlayerServer server = new ServerService.ServerBuilder()
                    .setServerInfo(serverInfo)
                    .setFamilyName(magicDefaultConfig.family())
                    .setSoftPlayerCap(magicDefaultConfig.playerCap_soft())
                    .setHardPlayerCap(magicDefaultConfig.playerCap_hard())
                    .setWeight(magicDefaultConfig.weight())
                    .build();

            server.register(magicDefaultConfig.family());

            ServerPingResponsePacket message = (ServerPingResponsePacket) new GenericPacket.Builder()
                    .setType(PacketType.PING_RESPONSE)
                    .setAddress(serverInfo.getAddress())
                    .setOrigin(PacketOrigin.PROXY)
                    .setParameter(ServerPingResponsePacket.ValidParameters.STATUS, String.valueOf(ServerPingResponsePacket.PingResponseStatus.ACCEPTED))
                    .setParameter(ServerPingResponsePacket.ValidParameters.MESSAGE, "Connected to the proxy! Registered as `"+server.serverInfo().getName()+"` into the family `"+server.family().name()+"`.")
                    .setParameter(ServerPingResponsePacket.ValidParameters.COLOR, NamedTextColor.GREEN.toString())
                    .setParameter(ServerPingResponsePacket.ValidParameters.INTERVAL_OPTIONAL, String.valueOf(serverService.serverInterval()))
                    .buildSendable();
            backboneMessenger.publish(message);

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
    }

    private static void disconnectServer(Tinder api, ServerInfo serverInfo, ServerPingPacket packet) throws Exception {
        MagicDefaultConfig magicDefaultConfig = new MagicDefaultConfig(api.dataFolder(), packet.magicConfigName());
        magicDefaultConfig.register();

        api.services().server().unregisterServer(serverInfo, magicDefaultConfig.family(), true);

    }

    private static void reviveOrConnectServer(Tinder api, ServerInfo serverInfo, ServerPingPacket packet) {
        ServerService serverService = api.services().server();

        PlayerServer server = serverService.search(serverInfo);
        if (server == null) {
            connectServer(api, serverInfo, packet);
            return;
        }

        server.setTimeout(serverService.serverTimeout());
        server.setPlayerCount(packet.playerCount());
    }
}
