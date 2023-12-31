package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.packet_handlers;

import group.aelysium.rustyconnector.plugin.velocity.lib.server.RankedMCLoader;
import group.aelysium.rustyconnector.toolkit.core.packet.GenericPacket;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.velocity.server.IRankedMCLoader;

public class RankedGameEndListener extends PacketListener<GenericPacket> {
    protected Tinder api;

    public RankedGameEndListener(Tinder api) {
        this.api = api;
    }

    @Override
    public PacketIdentification target() {
        return PacketIdentification.Predefined.END_RANKED_GAME;
    }

    @Override
    public void execute(GenericPacket packet) {
        RankedMCLoader mcloader = new IRankedMCLoader.Reference(packet.sender()).get();
    }
}