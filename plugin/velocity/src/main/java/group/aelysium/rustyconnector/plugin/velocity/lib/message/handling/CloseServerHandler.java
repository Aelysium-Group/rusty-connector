package group.aelysium.rustyconnector.plugin.velocity.lib.message.handling;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.core.lib.packets.PacketHandler;
import group.aelysium.rustyconnector.core.lib.packets.variants.CloseServerPacket;
import group.aelysium.rustyconnector.core.lib.packets.variants.OpenServerPacket;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Vector;

public class CloseServerHandler extends PacketHandler {
    @Override
    public void execute(GenericPacket genericPacket) throws Exception {
        CloseServerPacket packet = (CloseServerPacket) genericPacket;
        Tinder api = Tinder.get();

        ServerInfo serverInfo = new ServerInfo(packet.serverName(), packet.address());
        PlayerServer server = api.services().serverService().search(serverInfo);

        if (server != null) {
            PlayerFocusedServerFamily family = (PlayerFocusedServerFamily) server.family();
            family.closeServer(server);
        }
    }
}
