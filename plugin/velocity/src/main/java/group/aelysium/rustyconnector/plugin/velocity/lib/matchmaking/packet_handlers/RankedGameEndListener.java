package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.packet_handlers;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.toolkit.core.packet.variants.RankedGameEndPacket;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.RankedFamily;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;

public class RankedGameEndListener extends PacketListener<RankedGameEndPacket> {
    protected Tinder api;

    public RankedGameEndListener(Tinder api) {
        this.api = api;
    }

    @Override
    public PacketIdentification target() {
        return PacketIdentification.Predefined.END_RANKED_GAME;
    }

    @Override
    public void execute(RankedGameEndPacket packet) throws Exception {
        //((RankedFamily) family).gameManager().end(packet.uuid());
    }
}