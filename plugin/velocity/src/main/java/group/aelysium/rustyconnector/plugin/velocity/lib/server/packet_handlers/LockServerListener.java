package group.aelysium.rustyconnector.plugin.velocity.lib.server.packet_handlers;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.toolkit.core.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.core.packet.variants.LockServerPacket;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketType;

public class LockServerListener implements PacketListener {
    protected Tinder api;

    public LockServerListener(Tinder api) {
        this.api = api;
    }

    @Override
    public PacketType.Mapping identifier() {
        return PacketType.LOCK_SERVER;
    }
    @Override
    public <TPacket extends IPacket> void execute(TPacket genericPacket) throws Exception {
        LockServerPacket packet = (LockServerPacket) genericPacket;

        ServerInfo serverInfo = new ServerInfo(packet.serverName(), packet.address());
        MCLoader server = (MCLoader) new MCLoader.Reference(serverInfo).get();

        if (server != null) {
            Family family = server.family();
            family.loadBalancer().lock(server);
        }
    }
}
