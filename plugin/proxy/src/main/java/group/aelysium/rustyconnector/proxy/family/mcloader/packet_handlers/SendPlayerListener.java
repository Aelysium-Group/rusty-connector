package group.aelysium.rustyconnector.proxy.family.mcloader.packet_handlers;

import group.aelysium.rustyconnector.common.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.toolkit.RC;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.PacketListener;
import group.aelysium.rustyconnector.common.packets.SendPlayerPacket;
import group.aelysium.rustyconnector.toolkit.proxy.family.IFamily;
import group.aelysium.rustyconnector.toolkit.proxy.player.IPlayer;
import net.kyori.adventure.text.Component;

public class SendPlayerListener extends PacketListener<SendPlayerPacket> {
    public SendPlayerListener() {
        super(
                BuiltInIdentifications.SEND_PLAYER,
                new Wrapper<>() {
                    @Override
                    public SendPlayerPacket wrap(IPacket packet) {
                        return new SendPlayerPacket(packet);
                    }
                }
        );
    }

    @Override
    public void execute(SendPlayerPacket packet) throws Exception {
        IPlayer player = RC.P.Player(packet.uuid()).orElseThrow();

        try {
            IFamily family;
            try {
                family = player.server().orElseThrow().family().orElseThrow();
            } catch (Exception e) {
                throw new RuntimeException("You don't seem to be connected to a server at this moment!");
            }



            if(family.id().equals(packet.targetFamilyName()))
                throw new RuntimeException("You're already connected to this server!");

            family.connect(player);
        } catch (Exception e) {
            player.sendMessage(Component.text(e.getMessage()));
        }
    }
}
