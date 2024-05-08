package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.packet_handlers;

import group.aelysium.rustyconnector.core.lib.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.core.lib.packets.RankedGame;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.RankedFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.RankedMCLoader;
import group.aelysium.rustyconnector.toolkit.core.packet.Packet;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.ISession;
import group.aelysium.rustyconnector.toolkit.velocity.server.IRankedMCLoader;

public class RankedGameEndTiedListener extends PacketListener<RankedGame.EndTied> {
    protected Tinder api;

    public RankedGameEndTiedListener(Tinder api) {
        this.api = api;
    }

    @Override
    public PacketIdentification target() {
        return BuiltInIdentifications.RANKED_GAME_END_TIE;
    }

    @Override
    public RankedGame.EndTied wrap(Packet packet) {
        return new RankedGame.EndTied(packet);
    }

    @Override
    public void execute(RankedGame.EndTied packet) {
        RankedMCLoader mcloader = new IRankedMCLoader.Reference(packet.sender().uuid()).get();

        ISession session = mcloader.currentSession().orElseGet(() -> {
            RankedFamily family = (RankedFamily) mcloader.family();
            return family.matchmaker().fetch(packet.session().uuid()).orElseThrow(()->
                    new RuntimeException("No session with the uuid: "+packet.session().uuid()+" exists on MCLoader: "+mcloader.uuid())
            );
        });

        session.endTied(packet.unlock());
    }
}