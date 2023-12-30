package group.aelysium.rustyconnector.core.mcloader.lib.packet_builder;

import group.aelysium.rustyconnector.core.mcloader.central.MCLoaderTinder;
import group.aelysium.rustyconnector.toolkit.core.packet.GenericPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.variants.*;
import group.aelysium.rustyconnector.toolkit.core.packet.variants.magic_link.HandshakeKillPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.variants.magic_link.HandshakePacket;
import group.aelysium.rustyconnector.toolkit.mc_loader.packet_builder.IPacketBuilderService;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.core.TinderAdapterForCore;
import group.aelysium.rustyconnector.core.mcloader.lib.lang.MCLoaderLang;
import group.aelysium.rustyconnector.core.mcloader.lib.server_info.ServerInfoService;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.UUID;

public class PacketBuilderService implements IPacketBuilderService {
    public void magicLinkHandshake() {
        MCLoaderTinder api = TinderAdapterForCore.getTinder();

        try {
            ServerInfoService serverInfoService = api.services().serverInfo();
            api.flame().backbone().connection().orElseThrow().publish(HandshakePacket.create(serverInfoService.uuid(), serverInfoService.address(), serverInfoService.displayName(), serverInfoService.magicConfig(), serverInfoService.playerCount()));
        } catch (Exception e) {
            MCLoaderLang.BOXED_MESSAGE_COLORED.send(api.logger(), e.toString(), NamedTextColor.RED);
        }
    }
    public void magicLinkKill() {
        MCLoaderTinder api = TinderAdapterForCore.getTinder();

        try {
            ServerInfoService serverInfoService = api.services().serverInfo();
            api.flame().backbone().connection().orElseThrow().publish(HandshakeKillPacket.create(serverInfoService.uuid()));
        } catch (Exception e) {
            MCLoaderLang.BOXED_MESSAGE_COLORED.send(api.logger(), e.toString(), NamedTextColor.RED);
        }
    }

    /**
     * Requests that the proxy moves this player to another server.
     * @param player The player to send.
     * @param familyName The name of the family to send to.
     */
    public void sendToOtherFamily(UUID player, String familyName) {
        MCLoaderTinder api = TinderAdapterForCore.getTinder();
        ServerInfoService serverInfoService = api.services().serverInfo();

        SendPlayerPacket message = new GenericPacket.Builder()
                .identification(PacketIdentification.Predefined.SEND_PLAYER)
                .toProxy(serverInfoService.uuid())
                .parameter(SendPlayerPacket.ValidParameters.TARGET_FAMILY_NAME, familyName)
                .parameter(SendPlayerPacket.ValidParameters.PLAYER_UUID, player.toString())
                .build();

        api.flame().backbone().connection().orElseThrow().publish(message);
    }

    /**
     * Tells the proxy to open the server running the command.
     */
    public void unlockServer() {
        MCLoaderTinder api = TinderAdapterForCore.getTinder();
        ServerInfoService serverInfoService = api.services().serverInfo();

        UnlockServerPacket message = new GenericPacket.Builder()
                .identification(PacketIdentification.Predefined.UNLOCK_SERVER)
                .toProxy(serverInfoService.uuid())
                .build();

        api.flame().backbone().connection().orElseThrow().publish(message);
    }

    /**
     * Tells the proxy to close the server running the command.
     */
    public void lockServer() {
        MCLoaderTinder api = TinderAdapterForCore.getTinder();
        ServerInfoService serverInfoService = api.services().serverInfo();

        LockServerPacket message = new GenericPacket.Builder()
                .identification(PacketIdentification.Predefined.LOCK_SERVER)
                .toProxy(serverInfoService.uuid())
                .build();

        api.flame().backbone().connection().orElseThrow().publish(message);
    }

    /**
     * Tells the proxy to end the game currently active on this server.
     */
    public void endRankedGame(UUID uuid) {
        if(uuid == null) return;

        MCLoaderTinder api = TinderAdapterForCore.getTinder();
        ServerInfoService serverInfoService = api.services().serverInfo();

        RankedGameEndPacket message = new GenericPacket.Builder()
                .identification(PacketIdentification.Predefined.END_RANKED_GAME)
                .toProxy(serverInfoService.uuid())
                .parameter(RankedGameEndPacket.ValidParameters.FAMILY_NAME, serverInfoService.uuid().toString())
                .parameter(RankedGameEndPacket.ValidParameters.GAME_UUID, String.valueOf(uuid))
                .build();

        api.flame().backbone().connection().orElseThrow().publish(message);
    }

    @Override
    public void kill() {}
}
