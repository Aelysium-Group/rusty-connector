package group.aelysium.rustyconnector.plugin.velocity.lib.message.handling;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.core.lib.packets.PacketHandler;
import group.aelysium.rustyconnector.core.lib.packets.variants.OpenServerPacket;
import group.aelysium.rustyconnector.core.lib.packets.variants.SendPlayerPacket;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import net.kyori.adventure.text.Component;

import java.lang.ref.WeakReference;
import java.security.InvalidAlgorithmParameterException;
import java.util.Objects;
import java.util.UUID;
import java.util.Vector;

public class OpenServerHandler extends PacketHandler {
    @Override
    public void execute(GenericPacket genericPacket) throws Exception {
        OpenServerPacket packet = (OpenServerPacket) genericPacket;
        Tinder api = Tinder.get();

        api.logger().log("Executing OpenServer " + packet.serverName());

        ServerInfo serverInfo = new ServerInfo(packet.serverName(), packet.address());
        PlayerServer server = api.services().serverService().search(serverInfo);

        if (server != null) {
            PlayerFocusedServerFamily family = (PlayerFocusedServerFamily) server.family();
            family.openServer(server);
        }
    }
}
