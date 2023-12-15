package group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.packet_handlers;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.config.MagicMCLoaderConfig;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.toolkit.core.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.mc_loader.connection_intent.ConnectionIntent;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.core.lib.messenger.implementors.redis.RedisConnection;
import group.aelysium.rustyconnector.toolkit.core.packet.variants.GenericPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketOrigin;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketType;
import group.aelysium.rustyconnector.toolkit.core.packet.variants.ServerPingPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.variants.ServerPingResponsePacket;
import group.aelysium.rustyconnector.toolkit.core.log_gate.GateKey;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;

public class MagicLinkPingListener implements PacketListener {
    protected Tinder api;

    public MagicLinkPingListener(Tinder api) {
        this.api = api;
    }

    @Override
    public PacketType.Mapping identifier() {
        return PacketType.PING;
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
            api.logger().send(ProxyLang.PING.build(serverInfo));

        if(packet.intent() == ConnectionIntent.CONNECT)
            reviveOrConnectServer(api, serverInfo, packet);
        if(packet.intent() == ConnectionIntent.DISCONNECT)
            disconnectServer(api, serverInfo, packet);
    }

    private static void connectServer(Tinder api, ServerInfo serverInfo, ServerPingPacket packet) throws IOException {
        ServerService serverService = api.services().server();
        RedisConnection backboneMessenger = api.flame().backbone().connection().orElseThrow();

        MagicMCLoaderConfig magicMCLoaderConfig = MagicMCLoaderConfig.construct(api.dataFolder(), packet.magicConfigName(), api.lang());

        try {
            MCLoader server = new ServerService.ServerBuilder()
                    .setServerInfo(serverInfo)
                    .setSoftPlayerCap(magicMCLoaderConfig.playerCap_soft())
                    .setHardPlayerCap(magicMCLoaderConfig.playerCap_hard())
                    .setWeight(magicMCLoaderConfig.weight())
                    .setPodName(packet.podName())
                    .build();

            server.register(magicMCLoaderConfig.family());

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
        MagicMCLoaderConfig magicMCLoaderConfig = MagicMCLoaderConfig.construct(api.dataFolder(), packet.magicConfigName(), api.lang());

        api.services().server().unregisterServer(serverInfo, magicMCLoaderConfig.family(), true);
    }

    private static void reviveOrConnectServer(Tinder api, ServerInfo serverInfo, ServerPingPacket packet) throws IOException {
        ServerService serverService = api.services().server();

        MCLoader server = (MCLoader) new MCLoader.Reference(serverInfo).get();
        if (server == null) {
            connectServer(api, serverInfo, packet);
            return;
        }

        server.setTimeout(serverService.serverTimeout());
        server.setPlayerCount(packet.playerCount());
    }
}
