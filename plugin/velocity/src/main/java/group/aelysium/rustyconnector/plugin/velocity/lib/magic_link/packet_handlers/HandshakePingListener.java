package group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.packet_handlers;

import group.aelysium.rustyconnector.core.lib.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.configs.MagicMCLoaderConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.RankedFamily;
import group.aelysium.rustyconnector.toolkit.core.messenger.IMessengerConnection;
import group.aelysium.rustyconnector.toolkit.core.packet.Packet;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.core.lib.packets.MagicLink;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import group.aelysium.rustyconnector.toolkit.core.server.ServerAssignment;
import group.aelysium.rustyconnector.toolkit.velocity.util.AddressUtil;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.IOException;

public class HandshakePingListener extends PacketListener<MagicLink.Handshake.Ping> {
    protected Tinder api;

    public HandshakePingListener(Tinder api) {
        this.api = api;
    }

    @Override
    public PacketIdentification target() {
        return BuiltInIdentifications.MAGICLINK_HANDSHAKE_PING;
    }

    @Override
    public MagicLink.Handshake.Ping wrap(Packet packet) {
        return new MagicLink.Handshake.Ping(packet);
    }

    @Override
    public void execute(MagicLink.Handshake.Ping packet) throws Exception {
        //if(api.logger().loggerGate().check(GateKey.PING))
        //    api.logger().send(ProxyLang.PING.build(serverInfo));

        reviveOrConnectServer(api, packet);
    }

    private static void connectServer(Tinder api, MagicLink.Handshake.Ping packet) {
        ServerService serverService = api.services().server();
        IMessengerConnection backboneMessenger = api.services().magicLink().connection().orElseThrow();

        MagicMCLoaderConfig magicMCLoaderConfig = MagicMCLoaderConfig.construct(api.dataFolder(), packet.magicConfigName(), api.lang(), api.services().config());

        try {
            Family family = new Family.Reference(magicMCLoaderConfig.family()).get();

            MCLoader server = new MCLoader.Builder()
                    .uuid(packet.sender())
                    .address(AddressUtil.parseAddress(packet.address()))
                    .softPlayerCap(magicMCLoaderConfig.playerCap_soft())
                    .hardPlayerCap(magicMCLoaderConfig.playerCap_hard())
                    .weight(magicMCLoaderConfig.weight())
                    .podName("")
                    .build();
            server.register(family.id());

            ServerAssignment assignment = ServerAssignment.GENERIC;
            if(family instanceof RankedFamily) assignment = ServerAssignment.RANKED_GAME_SERVER;

            Packet response = api.services().packetBuilder().newBuilder()
                    .identification(BuiltInIdentifications.MAGICLINK_HANDSHAKE_SUCCESS)
                    .sendingToMCLoader(packet.sender())
                    .parameter(MagicLink.Handshake.Success.Parameters.MESSAGE, "Connected to the proxy! Registered as `"+server.serverInfo().getName()+"` into the family `"+server.family().id()+"`. Loaded using the magic config `"+packet.magicConfigName()+"`.")
                    .parameter(MagicLink.Handshake.Success.Parameters.COLOR, NamedTextColor.GREEN.toString())
                    .parameter(MagicLink.Handshake.Success.Parameters.INTERVAL, String.valueOf(serverService.serverInterval()))
                    .parameter(MagicLink.Handshake.Success.Parameters.ASSIGNMENT, assignment.toString())
                    .build();
            backboneMessenger.publish(response);

        } catch(Exception e) {
            e.printStackTrace();
            Packet response = api.services().packetBuilder().newBuilder()
                .identification(BuiltInIdentifications.MAGICLINK_HANDSHAKE_FAIL)
                .sendingToMCLoader(packet.sender())
                .parameter(MagicLink.Handshake.Failure.Parameters.REASON, "Attempt to connect to proxy failed! " + e.getMessage())
                .build();
            backboneMessenger.publish(response);
        }
    }

    private static void reviveOrConnectServer(Tinder api, MagicLink.Handshake.Ping packet) {
        ServerService serverService = api.services().server();

        try {
            MCLoader server = new MCLoader.Reference(packet.sender()).get();

            server.setTimeout(serverService.serverTimeout());
            server.setPlayerCount(packet.playerCount());
        } catch (Exception ignore) {
            connectServer(api, packet);
        }
    }
}
