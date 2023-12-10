package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.packet_handlers;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.toolkit.core.packet.variants.RankedGameEndPacket;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.RankedFamily;
import group.aelysium.rustyconnector.toolkit.core.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketType;

public class RankedUpdateListener implements PacketListener {
    protected Tinder api;

    public RankedUpdateListener(Tinder api) {
        this.api = api;
    }

    @Override
    public PacketType.Mapping identifier() {
        return null; // TODO fix this
    }

    @Override
    public <TPacket extends IPacket> void execute(TPacket genericPacket) throws Exception {
        RankedGameEndPacket packet = (RankedGameEndPacket) genericPacket;

        Family family = new Family.Reference(packet.familyName()).get();
        if(!(family instanceof RankedFamily)) return;

        ServerInfo serverInfo = new ServerInfo(
                packet.serverName(),
                packet.address()
        );
        if(!family.containsServer(serverInfo)) return;

        //((RankedFamily) family).gameManager().end(packet.uuid());
    }
}