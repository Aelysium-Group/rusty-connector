package group.aelysium.rustyconnector.core.mcloader.lib.ranked_game_interface.handlers;

import group.aelysium.rustyconnector.core.TinderAdapterForCore;
import group.aelysium.rustyconnector.core.lib.packets.variants.RankedGameAssociatePacket;
import group.aelysium.rustyconnector.core.lib.packets.variants.ServerPingResponsePacket;
import group.aelysium.rustyconnector.core.mcloader.lib.magic_link.MagicLinkService;
import group.aelysium.rustyconnector.toolkit.core.logger.PluginLogger;
import group.aelysium.rustyconnector.toolkit.core.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketHandler;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.MCLoaderTinder;
import group.aelysium.rustyconnector.toolkit.mc_loader.magic_link.MagicLinkStatus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class RankedGameAssociateHandler implements PacketHandler {
    protected MCLoaderTinder api;

    public RankedGameAssociateHandler(MCLoaderTinder api) {
        this.api = api;
    }

    @Override
    public <TPacket extends IPacket> void execute(TPacket genericPacket) {
        RankedGameAssociatePacket packet = (RankedGameAssociatePacket) genericPacket;

        api.services().rankedGameInterface().associateGame(packet.uuid());
    }
}
