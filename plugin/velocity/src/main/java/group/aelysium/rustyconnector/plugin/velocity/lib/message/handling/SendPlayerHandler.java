package group.aelysium.rustyconnector.plugin.velocity.lib.message.handling;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyReference;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.RustyPlayer;
import group.aelysium.rustyconnector.toolkit.core.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketHandler;
import group.aelysium.rustyconnector.core.lib.packets.variants.SendPlayerPacket;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedFamily;
import net.kyori.adventure.text.Component;

import java.security.InvalidAlgorithmParameterException;
import java.util.UUID;

public class SendPlayerHandler implements PacketHandler {
    protected Tinder api;

    public SendPlayerHandler(Tinder api) {
        this.api = api;
    }
    @Override
    public <TPacket extends IPacket> void execute(TPacket genericPacket) throws Exception {
        SendPlayerPacket packet = (SendPlayerPacket) genericPacket;

        Player player = api.velocityServer().getPlayer(UUID.fromString(packet.uuid())).stream().findFirst().orElse(null);
        if(player == null) return;

        try {
            PlayerFocusedFamily family = (PlayerFocusedFamily) new FamilyReference(packet.targetFamilyName()).get();
            if (family == null) throw new InvalidAlgorithmParameterException("A family with the name `"+packet.targetFamilyName()+"` doesn't exist!");

            family.connect(RustyPlayer.from(player));
        } catch (Exception e) {
            player.sendMessage(Component.text(e.getMessage()));
        }
    }
}
