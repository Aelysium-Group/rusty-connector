package group.aelysium.rustyconnector.plugin.velocity.lib.message.handling;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.api.core.packet.IPacket;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.api.core.packet.PacketHandler;
import group.aelysium.rustyconnector.core.lib.packets.variants.LockServerPacket;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;

public class LockServerHandler implements PacketHandler {
    @Override
    public <TPacket extends IPacket> void execute(TPacket genericPacket) throws Exception {
        LockServerPacket packet = (LockServerPacket) genericPacket;
        Tinder api = Tinder.get();

        ServerInfo serverInfo = new ServerInfo(packet.serverName(), packet.address());
        PlayerServer server = api.services().server().search(serverInfo);

        if (server != null) {
            PlayerFocusedFamily family = (PlayerFocusedFamily) server.family();
            family.lockServer(server);
        }
    }
}
