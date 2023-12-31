package group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.packet_handlers;

import group.aelysium.rustyconnector.plugin.velocity.lib.config.configs.MagicMCLoaderConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.RankedFamily;
import group.aelysium.rustyconnector.toolkit.core.messenger.IMessengerConnection;
import group.aelysium.rustyconnector.toolkit.core.packet.variants.magic_link.HandshakeFailurePacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.core.packet.GenericPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.core.packet.variants.magic_link.HandshakePacket;
import group.aelysium.rustyconnector.toolkit.core.packet.variants.magic_link.HandshakeSuccessPacket;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import group.aelysium.rustyconnector.toolkit.core.server.ServerAssignment;
import group.aelysium.rustyconnector.toolkit.velocity.util.AddressUtil;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.IOException;

public class HandshakeListener extends PacketListener<HandshakePacket> {
    protected Tinder api;

    public HandshakeListener(Tinder api) {
        this.api = api;
    }

    @Override
    public PacketIdentification target() {
        return PacketIdentification.Predefined.MAGICLINK_HANDSHAKE;
    }

    @Override
    public void execute(HandshakePacket packet) throws Exception {
        //if(api.logger().loggerGate().check(GateKey.PING))
        //    api.logger().send(ProxyLang.PING.build(serverInfo));

        reviveOrConnectServer(api, packet);
    }

    private static void connectServer(Tinder api, HandshakePacket packet) {
        ServerService serverService = api.services().server();
        IMessengerConnection backboneMessenger = api.services().magicLink().connection().orElseThrow();

        MagicMCLoaderConfig magicMCLoaderConfig = MagicMCLoaderConfig.construct(api.dataFolder(), packet.magicConfigName(), api.lang(), api.services().config());

        try {
            Family family = new Family.Reference(magicMCLoaderConfig.family()).get();

            MCLoader server = new MCLoader.Builder()
                    .uuid(packet.sender())
                    .address(AddressUtil.parseAndResolveAddress(packet.address()))
                    .softPlayerCap(magicMCLoaderConfig.playerCap_soft())
                    .hardPlayerCap(magicMCLoaderConfig.playerCap_hard())
                    .weight(magicMCLoaderConfig.weight())
                    .podName(packet.podName())
                    .build();
            server.register(family.id());

            ServerAssignment assignment = ServerAssignment.GENERIC;
            if(family instanceof RankedFamily) assignment = ServerAssignment.RANKED_GAME_SERVER;

            HandshakeSuccessPacket message = new GenericPacket.MCLoaderPacketBuilder()
                    .identification(PacketIdentification.Predefined.MAGICLINK_HANDSHAKE_SUCCESS)
                    .sendingToAnotherMCLoader(packet.sender())
                    .parameter(HandshakeSuccessPacket.Parameters.MESSAGE, "Connected to the proxy! Registered as `"+server.serverInfo().getName()+"` into the family `"+server.family().id()+"`. Loaded using the magic config `"+packet.magicConfigName()+"`.")
                    .parameter(HandshakeSuccessPacket.Parameters.COLOR, NamedTextColor.GREEN.toString())
                    .parameter(HandshakeSuccessPacket.Parameters.INTERVAL, String.valueOf(serverService.serverInterval()))
                    .parameter(HandshakeSuccessPacket.Parameters.ASSIGNMENT, assignment.toString())
                    .build();
            backboneMessenger.publish(message);

        } catch(Exception e) {
            backboneMessenger.publish(HandshakeFailurePacket.create(packet.sender(), "Attempt to connect to proxy failed! " + e.getMessage()));
        }
    }

    private static void reviveOrConnectServer(Tinder api, HandshakePacket packet) throws IOException {
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
