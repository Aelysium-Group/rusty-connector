package group.aelysium.rustyconnector.core.mcloader.lib.ranked_game_interface.handlers;

import group.aelysium.rustyconnector.toolkit.core.packet.variants.RankedGameAssociatePacket;
import group.aelysium.rustyconnector.toolkit.core.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketType;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.MCLoaderTinder;

public class RankedGameAssociateListener implements PacketListener {
    protected MCLoaderTinder api;

    public RankedGameAssociateListener(MCLoaderTinder api) {
        this.api = api;
    }

    @Override
    public PacketType.Mapping identifier() {
        return PacketType.ASSOCIATE_RANKED_GAME;
    }

    @Override
    public <TPacket extends IPacket> void execute(TPacket genericPacket) {
        RankedGameAssociatePacket packet = (RankedGameAssociatePacket) genericPacket;

        api.services().rankedGameInterface().associateGame(packet.uuid());
    }
}
