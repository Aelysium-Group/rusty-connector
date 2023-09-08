package group.aelysium.rustyconnector.plugin.velocity.lib.message.handling;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.packets.PacketHandler;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.core.lib.packets.variants.SendPlayerPacket;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedServerFamily;
import net.kyori.adventure.text.Component;

import java.security.InvalidAlgorithmParameterException;
import java.util.UUID;

public class SendPlayerHandler implements PacketHandler {
    private final SendPlayerPacket message;

    public SendPlayerHandler(GenericPacket message) {
        this.message = (SendPlayerPacket) message;
    }

    @Override
    public void execute() throws Exception {
        VelocityAPI api = VelocityAPI.get();

        Player player = api.velocityServer().getPlayer(UUID.fromString(message.uuid())).stream().findFirst().orElse(null);
        if(player == null) return;

        try {
            FamilyService familyService = api.services().familyService();
            PlayerFocusedServerFamily family = (PlayerFocusedServerFamily) familyService.find(message.targetFamilyName());
            if (family == null) throw new InvalidAlgorithmParameterException("A family with the name `"+message.targetFamilyName()+"` doesn't exist!");

            family.connect(player);
        } catch (Exception e) {
            player.sendMessage(Component.text(e.getMessage()));
        }
    }
}
