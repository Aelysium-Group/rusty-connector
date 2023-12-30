package group.aelysium.rustyconnector.core.mcloader.lib.ranked_game_interface.handlers;

import group.aelysium.rustyconnector.toolkit.core.packet.variants.RankedGameAssociatePacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.IMCLoaderTinder;

public class RankedGameAssociateListener extends PacketListener<RankedGameAssociatePacket> {
    protected IMCLoaderTinder api;

    public RankedGameAssociateListener(IMCLoaderTinder api) {
        this.api = api;
    }

    @Override
    public PacketIdentification target() {
        return PacketIdentification.Predefined.REQUEST_TO_START_RANKED_GAME;
    }

    @Override
    public void execute(RankedGameAssociatePacket packet) {

        api.services().rankedGameInterface().associateGame(packet.uuid());
    }
}
