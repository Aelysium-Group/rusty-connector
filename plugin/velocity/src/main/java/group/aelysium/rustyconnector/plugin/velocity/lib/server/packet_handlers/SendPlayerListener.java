package group.aelysium.rustyconnector.plugin.velocity.lib.server.packet_handlers;

import group.aelysium.rustyconnector.core.lib.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.toolkit.core.packet.Packet;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.core.lib.packets.SendPlayerPacket;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import net.kyori.adventure.text.Component;

import java.security.InvalidAlgorithmParameterException;
import java.util.UUID;

public class SendPlayerListener extends PacketListener<SendPlayerPacket> {
    protected Tinder api;

    public SendPlayerListener(Tinder api) {
        this.api = api;
    }

    @Override
    public PacketIdentification target() {
        return BuiltInIdentifications.SEND_PLAYER;
    }

    @Override
    public SendPlayerPacket wrap(Packet packet) {
        return new SendPlayerPacket(packet);
    }

    @Override
    public void execute(SendPlayerPacket packet) throws Exception {
        com.velocitypowered.api.proxy.Player player = api.velocityServer().getPlayer(packet.uuid()).orElseThrow();

        try {
            Family family = new Family.Reference(packet.targetFamilyName()).get();
            if (family == null) throw new InvalidAlgorithmParameterException("A family with the id `"+packet.targetFamilyName()+"` doesn't exist!");

            IMCLoader server;
            try {
                server = new IMCLoader.Reference(UUID.fromString(player.getCurrentServer().orElseThrow().getServerInfo().getName())).get();
            } catch (Exception e) {
                throw new RuntimeException("You don't seem to be connected to a server at this moment!");
            }

            if(family.equals(server.family())) throw new RuntimeException("You're already connected to this server!");

            family.connect(new Player(player));
        } catch (Exception e) {
            player.sendMessage(Component.text(e.getMessage()));
        }
    }
}
