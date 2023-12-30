package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.packet_handlers;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.toolkit.core.packet.variants.RankedGameEndPacket;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.RankedFamily;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;

public class RankedUpdateListener extends PacketListener<RankedGameEndPacket> {
    protected Tinder api;

    public RankedUpdateListener(Tinder api) {
        this.api = api;
    }

    @Override
    public PacketIdentification target() {
        return null; // TODO fix this
    }

    @Override
    public void execute(RankedGameEndPacket packet) throws Exception {
        Family family = new Family.Reference(packet.familyName()).get();
        if(!(family instanceof RankedFamily)) return;

        if(!family.containsServer(packet.uuid())) return;

        //((RankedFamily) family).gameManager().end(packet.uuid());
    }
}