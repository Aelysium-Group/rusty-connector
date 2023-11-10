package group.aelysium.rustyconnector.plugin.velocity.lib.message.handling;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.toolkit.core.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketHandler;
import group.aelysium.rustyconnector.core.lib.packets.variants.UnlockServerPacket;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;

public class UnlockServerHandler implements PacketHandler {
    protected Tinder api;

    public UnlockServerHandler(Tinder api) {
        this.api = api;
    }

    @Override
    public <TPacket extends IPacket> void execute(TPacket genericPacket) throws Exception {
        UnlockServerPacket packet = (UnlockServerPacket) genericPacket;

        ServerInfo serverInfo = new ServerInfo(packet.serverName(), packet.address());
        PlayerServer server = api.services().server().search(serverInfo);

        if (server != null) {
            PlayerFocusedFamily family = (PlayerFocusedFamily) server.family();
            family.unlockServer(server);
        }
    }
}
