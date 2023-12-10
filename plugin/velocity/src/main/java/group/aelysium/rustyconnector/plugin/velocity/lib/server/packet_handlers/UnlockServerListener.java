package group.aelysium.rustyconnector.plugin.velocity.lib.server.packet_handlers;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.toolkit.core.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.core.lib.packets.variants.UnlockServerPacket;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketType;

public class UnlockServerListener implements PacketListener {
    protected Tinder api;

    public UnlockServerListener(Tinder api) {
        this.api = api;
    }

    @Override
    public PacketType.Mapping identifier() {
        return PacketType.UNLOCK_SERVER;
    }

    @Override
    public <TPacket extends IPacket> void execute(TPacket genericPacket) throws Exception {
        UnlockServerPacket packet = (UnlockServerPacket) genericPacket;

        ServerInfo serverInfo = new ServerInfo(packet.serverName(), packet.address());
        MCLoader server = new MCLoader.Reference(serverInfo).get();

        if (server != null) {
            Family family = server.family();
            family.loadBalancer().unlock(server);
        }
    }
}
