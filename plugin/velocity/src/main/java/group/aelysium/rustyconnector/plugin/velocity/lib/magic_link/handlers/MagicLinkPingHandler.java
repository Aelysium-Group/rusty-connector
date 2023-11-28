package group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.handlers;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.config.MagicLinkConfig;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
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
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;

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

    private static void connectServer(Tinder api, ServerInfo serverInfo, ServerPingPacket packet) throws IOException {
        ServerService serverService = api.services().server();
        RedisConnection backboneMessenger = api.flame().backbone().connection().orElseThrow();

        MagicLinkConfig magicLinkConfig = new MagicLinkConfig(api.dataFolder(), packet.magicConfigName());
        if(!magicLinkConfig.generate(new ArrayList<>(), Tinder.get().lang(), LangFileMappings.VELOCITY_MAGIC_CONFIG_TEMPLATE))
            throw new IllegalStateException("Unable to fetch config!");
        magicLinkConfig.register();

        try {
            MCLoader server = new ServerService.ServerBuilder()
                    .setServerInfo(serverInfo)
                    .setFamilyName(magicLinkConfig.family())
                    .setSoftPlayerCap(magicLinkConfig.playerCap_soft())
                    .setHardPlayerCap(magicLinkConfig.playerCap_hard())
                    .setWeight(magicLinkConfig.weight())
                    .build();

            server.register(magicLinkConfig.family());

            ServerPingResponsePacket message = (ServerPingResponsePacket) new GenericPacket.Builder()
                    .setType(PacketType.PING_RESPONSE)
                    .setAddress(serverInfo.getAddress())
                    .setOrigin(PacketOrigin.PROXY)
                    .setParameter(ServerPingResponsePacket.ValidParameters.STATUS, String.valueOf(ServerPingResponsePacket.PingResponseStatus.ACCEPTED))
                    .setParameter(ServerPingResponsePacket.ValidParameters.MESSAGE, "Connected to the proxy! Registered as `"+server.serverInfo().getName()+"` into the family `"+server.family().id()+"`. Loaded using the magic config `"+packet.magicConfigName()+"`.")
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
        MagicLinkConfig magicLinkConfig = new MagicLinkConfig(api.dataFolder(), packet.magicConfigName());
        if(!magicLinkConfig.generate(new ArrayList<>(), Tinder.get().lang(), LangFileMappings.VELOCITY_MAGIC_CONFIG_TEMPLATE))
            throw new IllegalStateException("Unable to fetch config!");
        magicLinkConfig.register();

        api.services().server().unregisterServer(serverInfo, magicLinkConfig.family(), true);
    }

    private static void reviveOrConnectServer(Tinder api, ServerInfo serverInfo, ServerPingPacket packet) throws IOException {
        ServerService serverService = api.services().server();

        MCLoader server = new MCLoader.Reference(serverInfo).get();
        if (server == null) {
            connectServer(api, serverInfo, packet);
            return;
        }

        server.setTimeout(serverService.serverTimeout());
        server.setPlayerCount(packet.playerCount());
    }
}
