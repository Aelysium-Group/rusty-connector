package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.packet_handlers;

import group.aelysium.rustyconnector.core.lib.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.core.lib.packets.MagicLink;
import group.aelysium.rustyconnector.core.lib.packets.RankedGame;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.RankedMCLoader;
import group.aelysium.rustyconnector.toolkit.core.packet.Packet;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.velocity.server.IRankedMCLoader;

public class RankedGameEndListener extends PacketListener<RankedGame.End> {
    protected Tinder api;

    public RankedGameEndListener(Tinder api) {
        this.api = api;
    }

    @Override
    public PacketIdentification target() {
        return BuiltInIdentifications.RANKED_GAME_END;
    }

    @Override
    public RankedGame.End wrap(Packet packet) {
        return new RankedGame.End(packet);
    }

    @Override
    public void execute(RankedGame.End packet) {
        RankedMCLoader mcloader = new IRankedMCLoader.Reference(packet.sender()).get();
    }
}