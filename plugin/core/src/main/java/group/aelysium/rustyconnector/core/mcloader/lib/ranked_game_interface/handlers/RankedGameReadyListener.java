package group.aelysium.rustyconnector.core.mcloader.lib.ranked_game_interface.handlers;

import group.aelysium.rustyconnector.core.TinderAdapterForCore;
import group.aelysium.rustyconnector.core.lib.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.core.lib.packets.RankedGame;
import group.aelysium.rustyconnector.toolkit.core.packet.Packet;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.IMCLoaderTinder;
import group.aelysium.rustyconnector.toolkit.mc_loader.events.ranked_game.RankedGameReadyEvent;

public class RankedGameReadyListener extends PacketListener<RankedGame.Ready> {
    protected IMCLoaderTinder api;

    public RankedGameReadyListener(IMCLoaderTinder api) {
        this.api = api;
    }

    @Override
    public PacketIdentification target() {
        return BuiltInIdentifications.RANKED_GAME_READY;
    }

    @Override
    public RankedGame.Ready wrap(Packet packet) {
        return new RankedGame.Ready(packet);
    }

    @Override
    public void execute(RankedGame.Ready packet) {
        RankedGame.Session session = packet.session();

        TinderAdapterForCore.getTinder().services().events().fireEvent(new RankedGameReadyEvent(session.uuid(), session.players()));
        TinderAdapterForCore.getTinder().services().rankedGameInterface().orElseThrow().session(session.uuid(), session.players());
    }
}
