package group.aelysium.rustyconnector.core.mcloader.lib.packet_builder;

import group.aelysium.rustyconnector.core.lib.packets.variants.*;
import group.aelysium.rustyconnector.core.mcloader.central.MCLoaderTinder;
import group.aelysium.rustyconnector.toolkit.core.packet.variants.*;
import group.aelysium.rustyconnector.toolkit.mc_loader.connection_intent.ConnectionIntent;
import group.aelysium.rustyconnector.toolkit.mc_loader.packet_builder.IPacketBuilderService;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketOrigin;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketType;
import group.aelysium.rustyconnector.core.TinderAdapterForCore;
import group.aelysium.rustyconnector.core.mcloader.lib.lang.MCLoaderLang;
import group.aelysium.rustyconnector.core.mcloader.lib.server_info.ServerInfoService;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.UUID;

public class PacketBuilderService implements IPacketBuilderService {
    public void pingProxy(ConnectionIntent intent) {
        MCLoaderTinder api = TinderAdapterForCore.getTinder();

        try {
            ServerInfoService serverInfoService = (ServerInfoService) api.services().serverInfo();
            ServerPingPacket message = (ServerPingPacket) new GenericPacket.Builder()
                    .setType(PacketType.PING)
                    .setOrigin(PacketOrigin.SERVER)
                    .setAddress(serverInfoService.address())
                    .setParameter(ServerPingPacket.ValidParameters.INTENT, intent.toString())
                    .setParameter(ServerPingPacket.ValidParameters.MAGIC_CONFIG_NAME, serverInfoService.magicConfig())
                    .setParameter(ServerPingPacket.ValidParameters.SERVER_NAME, serverInfoService.sessionUUID().toString())
                    .setParameter(ServerPingPacket.ValidParameters.PLAYER_COUNT, String.valueOf(serverInfoService.playerCount()))
                    .buildSendable();
            api.flame().backbone().connection().orElseThrow().publish(message);
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
        ServerInfoService serverInfoService = (ServerInfoService) api.services().serverInfo();

        SendPlayerPacket message = (SendPlayerPacket) new GenericPacket.Builder()
                .setType(PacketType.SEND_PLAYER)
                .setOrigin(PacketOrigin.SERVER)
                .setAddress(serverInfoService.address())
                .setParameter(SendPlayerPacket.ValidParameters.TARGET_FAMILY_NAME, familyName)
                .setParameter(SendPlayerPacket.ValidParameters.PLAYER_UUID, player.toString())
                .buildSendable();

        api.flame().backbone().connection().orElseThrow().publish(message);
    }

    /**
     * Tells the proxy to open the server running the command.
     */
    public void unlockServer() {
        MCLoaderTinder api = TinderAdapterForCore.getTinder();
        ServerInfoService serverInfoService = (ServerInfoService) api.services().serverInfo();

        UnlockServerPacket message = (UnlockServerPacket) new GenericPacket.Builder()
                .setType(PacketType.UNLOCK_SERVER)
                .setOrigin(PacketOrigin.SERVER)
                .setAddress(serverInfoService.address())
                .setParameter(UnlockServerPacket.ValidParameters.SERVER_NAME, serverInfoService.sessionUUID().toString())
                .buildSendable();

        api.flame().backbone().connection().orElseThrow().publish(message);
    }

    /**
     * Tells the proxy to close the server running the command.
     */
    public void lockServer() {
        MCLoaderTinder api = TinderAdapterForCore.getTinder();
        ServerInfoService serverInfoService = (ServerInfoService) api.services().serverInfo();

        LockServerPacket message = (LockServerPacket) new GenericPacket.Builder()
                .setType(PacketType.LOCK_SERVER)
                .setOrigin(PacketOrigin.SERVER)
                .setAddress(serverInfoService.address())
                .setParameter(UnlockServerPacket.ValidParameters.SERVER_NAME, serverInfoService.sessionUUID().toString())
                .buildSendable();

        api.flame().backbone().connection().orElseThrow().publish(message);
    }

    /**
     * Tells the proxy to end the game currently active on this server.
     */
    public void endRankedGame(UUID uuid) {
        if(uuid == null) return;

        MCLoaderTinder api = TinderAdapterForCore.getTinder();
        ServerInfoService serverInfoService = (ServerInfoService) api.services().serverInfo();

        RankedGameEndPacket message = (RankedGameEndPacket) new GenericPacket.Builder()
                .setType(PacketType.END_RANKED_GAME)
                .setOrigin(PacketOrigin.SERVER)
                .setAddress(serverInfoService.address())
                .setParameter(RankedGameEndPacket.ValidParameters.FAMILY_NAME, serverInfoService.sessionUUID().toString())
                .setParameter(RankedGameEndPacket.ValidParameters.GAME_UUID, String.valueOf(uuid))
                .buildSendable();

        api.flame().backbone().connection().orElseThrow().publish(message);
    }

    @Override
    public void kill() {}
}
