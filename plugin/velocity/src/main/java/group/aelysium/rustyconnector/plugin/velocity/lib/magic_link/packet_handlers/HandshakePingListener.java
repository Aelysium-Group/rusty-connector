package group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.packet_handlers;

import group.aelysium.rustyconnector.core.lib.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.configs.MagicMCLoaderConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.RankedFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.MagicLinkService;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.RankedMCLoader;
import group.aelysium.rustyconnector.toolkit.core.messenger.IMessengerConnection;
import group.aelysium.rustyconnector.toolkit.core.packet.Packet;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.core.lib.packets.MagicLink;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketParameter;
import group.aelysium.rustyconnector.toolkit.core.server.ServerAssignment;
import group.aelysium.rustyconnector.toolkit.velocity.magic_link.IMagicLink;
import group.aelysium.rustyconnector.toolkit.velocity.util.AddressUtil;
import net.kyori.adventure.text.format.NamedTextColor;

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

        ServerService serverService = api.services().server();

        try {
            MCLoader server = new MCLoader.Reference(packet.sender().uuid()).get();

            server.setTimeout(serverService.serverTimeout());
            server.setPlayerCount(packet.playerCount());
        } catch (Exception e) {
            RegisterServer.register(api, packet);
        }
    }

    private static class RegisterServer {
        public static void register(Tinder api, MagicLink.Handshake.Ping packet) {
            ServerService serverService = api.services().server();
            MagicLinkService magicLink = api.services().magicLink();
            IMessengerConnection backboneMessenger = api.services().magicLink().connection().orElseThrow();

            IMagicLink.MagicLinkMCLoaderSettings config = magicLink.magicConfig(packet.magicConfigName()).orElseThrow(
                    () -> new NullPointerException("No Magic Config exists with the name "+packet.magicConfigName()+"!")
            );

            try {
                Family family = new Family.Reference(config.family()).get();

                ServerAssignment assignment = ServerAssignment.GENERIC;
                if(family instanceof RankedFamily) assignment = ServerAssignment.RANKED_GAME_SERVER;

                MCLoader server;
                if(assignment == ServerAssignment.RANKED_GAME_SERVER) server = rankedMCLoader(config, packet);
                else server = genericMCLoader(config, packet);

                server.register(family.id());

                Packet response = packet.reply()
                        .identification(BuiltInIdentifications.MAGICLINK_HANDSHAKE_SUCCESS)
                        .parameter(MagicLink.Handshake.Success.Parameters.MESSAGE, "Connected to the proxy! Registered as `"+server.serverInfo().getName()+"` into the family `"+server.family().id()+"`. Loaded using the magic config `"+packet.magicConfigName()+"`.")
                        .parameter(MagicLink.Handshake.Success.Parameters.COLOR, NamedTextColor.GREEN.toString())
                        .parameter(MagicLink.Handshake.Success.Parameters.INTERVAL, new PacketParameter(serverService.serverInterval()))
                        .parameter(MagicLink.Handshake.Success.Parameters.ASSIGNMENT, assignment.toString())
                        .build();
                backboneMessenger.publish(response);

            } catch(Exception e) {
                Packet response = packet.reply()
                        .identification(BuiltInIdentifications.MAGICLINK_HANDSHAKE_FAIL)
                        .parameter(MagicLink.Handshake.Failure.Parameters.REASON, "Attempt to connect to proxy failed! " + e.getMessage())
                        .build();
                backboneMessenger.publish(response);
            }
        }

        private static MCLoader genericMCLoader(IMagicLink.MagicLinkMCLoaderSettings config, MagicLink.Handshake.Ping packet) {
            return new MCLoader(
                    packet.sender().uuid(),
                    AddressUtil.parseAddress(packet.address()),
                    packet.displayName().orElse(null),
                    config.soft_cap(),
                    config.hard_cap(),
                    config.weight(),
                    15
            );
        }
        private static RankedMCLoader rankedMCLoader(IMagicLink.MagicLinkMCLoaderSettings config, MagicLink.Handshake.Ping packet) {
            return new RankedMCLoader(
                    packet.sender().uuid(),
                    AddressUtil.parseAddress(packet.address()),
                    packet.displayName().orElse(null),
                    config.soft_cap(),
                    config.hard_cap(),
                    config.weight(),
                    15
            );
        }
    }
}
