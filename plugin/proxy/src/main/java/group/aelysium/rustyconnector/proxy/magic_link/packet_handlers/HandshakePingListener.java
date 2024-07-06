package group.aelysium.rustyconnector.proxy.magic_link.packet_handlers;

import group.aelysium.rustyconnector.common.magic_link.Packet;
import group.aelysium.rustyconnector.common.packets.MagicLink;
import group.aelysium.rustyconnector.common.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.toolkit.RC;
import group.aelysium.rustyconnector.toolkit.RustyConnector;
import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;
import group.aelysium.rustyconnector.toolkit.common.magic_link.IMagicLink;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.PacketParameter;
import group.aelysium.rustyconnector.toolkit.proxy.events.mc_loader.MCLoaderRegisterEvent;
import group.aelysium.rustyconnector.toolkit.proxy.family.IFamily;
import group.aelysium.rustyconnector.toolkit.proxy.family.mcloader.IMCLoader;
import group.aelysium.rustyconnector.toolkit.proxy.util.AddressUtil;
import net.kyori.adventure.text.format.NamedTextColor;

import java.security.InvalidAlgorithmParameterException;
import java.util.concurrent.TimeUnit;

public class HandshakePingListener extends PacketListener<MagicLink.Handshake.Ping> {

    public HandshakePingListener() {
        super(
                BuiltInIdentifications.MAGICLINK_HANDSHAKE_PING,
                new Wrapper<>() {
                    @Override
                    public MagicLink.Handshake.Ping wrap(IPacket packet) {
                        return new MagicLink.Handshake.Ping(packet);
                    }
                }
        );
    }

    @Override
    public void execute(MagicLink.Handshake.Ping packet) throws Exception {
        try {
            IMCLoader mcloader = RC.P.MCLoader(packet.sender().uuid()).orElseThrow();

            mcloader.setTimeout(15);
            mcloader.setPlayerCount(packet.playerCount());
        } catch (Exception e) {
            IMagicLink.Proxy magicLink = RC.P.MagicLink();
            IMagicLink.Proxy.MagicLinkMCLoaderSettings config = magicLink.magicConfig(packet.magicConfigName()).orElseThrow(
                    () -> new NullPointerException("No Magic Config exists with the name "+packet.magicConfigName()+"!")
            );

            try {
                Particle.Flux<IFamily> familyFlux = RustyConnector.Toolkit.Proxy().orElseThrow().orElseThrow().Families().orElseThrow().find(config.family()).orElseThrow(() ->
                        new InvalidAlgorithmParameterException("A family with the id `"+config.family()+"` doesn't exist!")
                );
                IFamily family = familyFlux.access().get(10, TimeUnit.SECONDS);

                RC.P.MCLoader(packet.sender().uuid()).ifPresent(m -> {
                    throw new RuntimeException("MCLoader " + packet.sender().uuid() + " can't be registered twice!");
                });

                IMCLoader.Unregistered unregisteredMCLoader = new IMCLoader.Unregistered(
                        packet.sender().uuid(),
                        AddressUtil.parseAddress(packet.address()),
                        packet.podName().orElse(null),
                        packet.displayName().orElse(null),
                        config.soft_cap(),
                        config.hard_cap(),
                        config.weight(),
                        15
                );

                IMCLoader mcloader = family.connector().register(unregisteredMCLoader);

                RC.P.Adapter().registerMCLoader(mcloader);

                RC.P.EventManager().fireEvent(new MCLoaderRegisterEvent(familyFlux, mcloader));

                Packet.New()
                        .identification(BuiltInIdentifications.MAGICLINK_HANDSHAKE_SUCCESS)
                        .parameter(MagicLink.Handshake.Success.Parameters.MESSAGE, "Connected to the proxy! Registered as `"+mcloader.uuidOrDisplayName()+"` into the family `"+family.id()+"`. Loaded using the magic config `"+packet.magicConfigName()+"`.")
                        .parameter(MagicLink.Handshake.Success.Parameters.COLOR, NamedTextColor.GREEN.toString())
                        .parameter(MagicLink.Handshake.Success.Parameters.INTERVAL, new PacketParameter(10))
                        .addressedTo(packet)
                        .send();
            } catch(Exception e2) {
                Packet.New()
                        .identification(BuiltInIdentifications.MAGICLINK_HANDSHAKE_FAIL)
                        .parameter(MagicLink.Handshake.Failure.Parameters.REASON, "Attempt to connect to proxy failed! " + e2.getMessage())
                        .addressedTo(packet)
                        .send();
            }
        }
    }
}
