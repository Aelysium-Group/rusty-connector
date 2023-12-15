package group.aelysium.rustyconnector.plugin.velocity.lib.server.packet_handlers;

import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.toolkit.core.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.core.packet.variants.SendPlayerPacket;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketType;
import net.kyori.adventure.text.Component;

import java.security.InvalidAlgorithmParameterException;
import java.util.UUID;

public class SendPlayerListener implements PacketListener {
    protected Tinder api;

    public SendPlayerListener(Tinder api) {
        this.api = api;
    }

    @Override
    public PacketType.Mapping identifier() {
        return PacketType.SEND_PLAYER;
    }
    @Override
    public <TPacket extends IPacket> void execute(TPacket genericPacket) throws Exception {
        SendPlayerPacket packet = (SendPlayerPacket) genericPacket;

        com.velocitypowered.api.proxy.Player player = api.velocityServer().getPlayer(UUID.fromString(packet.uuid())).stream().findFirst().orElse(null);
        if(player == null) return;

        try {
            Family family = (Family) new Family.Reference(packet.targetFamilyName()).get();
            if (family == null) throw new InvalidAlgorithmParameterException("A family with the id `"+packet.targetFamilyName()+"` doesn't exist!");

            family.connect(Player.from(player));
        } catch (Exception e) {
            player.sendMessage(Component.text(e.getMessage()));
        }
    }
}
