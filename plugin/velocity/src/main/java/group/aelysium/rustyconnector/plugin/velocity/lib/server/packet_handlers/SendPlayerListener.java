package group.aelysium.rustyconnector.plugin.velocity.lib.server.packet_handlers;

import group.aelysium.rustyconnector.core.lib.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.core.lib.packets.MagicLink;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.toolkit.core.packet.Packet;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.core.lib.packets.SendPlayerPacket;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import net.kyori.adventure.text.Component;

import java.security.InvalidAlgorithmParameterException;

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

        com.velocitypowered.api.proxy.Player player = api.velocityServer().getPlayer(packet.uuid()).stream().findFirst().orElse(null);
        if(player == null) return;

        try {
            Family family = new Family.Reference(packet.targetFamilyName()).get();
            if (family == null) throw new InvalidAlgorithmParameterException("A family with the id `"+packet.targetFamilyName()+"` doesn't exist!");

            family.connect(Player.from(player));
        } catch (Exception e) {
            player.sendMessage(Component.text(e.getMessage()));
        }
    }
}
