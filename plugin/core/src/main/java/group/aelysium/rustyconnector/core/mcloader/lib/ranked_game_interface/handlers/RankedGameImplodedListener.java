package group.aelysium.rustyconnector.core.mcloader.lib.ranked_game_interface.handlers;

import group.aelysium.rustyconnector.core.TinderAdapterForCore;
import group.aelysium.rustyconnector.core.lib.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.core.lib.packets.RankedGame;
import group.aelysium.rustyconnector.toolkit.core.packet.Packet;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.IMCLoaderTinder;
import group.aelysium.rustyconnector.toolkit.mc_loader.events.ranked_game.RankedGameImplodeEvent;

public class RankedGameImplodedListener extends PacketListener<RankedGame.Imploded> {
    protected IMCLoaderTinder api;

    public RankedGameImplodedListener(IMCLoaderTinder api) {
        this.api = api;
    }

    @Override
    public PacketIdentification target() {
        return BuiltInIdentifications.RANKED_GAME_IMPLODE;
    }

    @Override
    public RankedGame.Imploded wrap(Packet packet) {
        return new RankedGame.Imploded(packet);
    }

    @Override
    public void execute(RankedGame.Imploded packet) {
        TinderAdapterForCore.getTinder().services().rankedGameInterface().orElseThrow().session(null, null);
        TinderAdapterForCore.getTinder().services().events().fireEvent(new RankedGameImplodeEvent(packet.sessionUUID(), packet.reason()));
    }
}
