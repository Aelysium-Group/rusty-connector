package group.aelysium.rustyconnector.plugin.velocity.lib.message.handling;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.core.lib.packets.PacketHandler;
import group.aelysium.rustyconnector.core.lib.packets.variants.UnlockServerPacket;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;

public class UnlockServerHandler extends PacketHandler {
    @Override
    public void execute(GenericPacket genericPacket) throws Exception {
        UnlockServerPacket packet = (UnlockServerPacket) genericPacket;
        Tinder api = Tinder.get();

        ServerInfo serverInfo = new ServerInfo(packet.serverName(), packet.address());
        PlayerServer server = api.services().serverService().search(serverInfo);

        if (server != null) {
            PlayerFocusedServerFamily family = (PlayerFocusedServerFamily) server.family();
            family.unlockServer(server);
        }
    }
}
