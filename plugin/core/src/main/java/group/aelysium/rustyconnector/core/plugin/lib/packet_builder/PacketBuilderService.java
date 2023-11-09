package group.aelysium.rustyconnector.core.plugin.lib.packet_builder;

import group.aelysium.rustyconnector.toolkit.mc_loader.central.MCLoaderTinder;
import group.aelysium.rustyconnector.toolkit.mc_loader.connection_intent.ConnectionIntent;
import group.aelysium.rustyconnector.toolkit.mc_loader.packet_builder.IPacketBuilderService;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketOrigin;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketType;
import group.aelysium.rustyconnector.core.lib.packets.variants.LockServerPacket;
import group.aelysium.rustyconnector.core.lib.packets.variants.UnlockServerPacket;
import group.aelysium.rustyconnector.core.lib.packets.variants.SendPlayerPacket;
import group.aelysium.rustyconnector.core.lib.packets.variants.ServerPingPacket;
import group.aelysium.rustyconnector.core.TinderAdapterForCore;
import group.aelysium.rustyconnector.core.plugin.lib.lang.PluginLang;
import group.aelysium.rustyconnector.core.plugin.lib.server_info.ServerInfoService;
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
                    .setParameter(ServerPingPacket.ValidParameters.FAMILY_NAME, serverInfoService.family())
                    .setParameter(ServerPingPacket.ValidParameters.SERVER_NAME, serverInfoService.name())
                    .setParameter(ServerPingPacket.ValidParameters.SOFT_CAP, String.valueOf(serverInfoService.softPlayerCap()))
                    .setParameter(ServerPingPacket.ValidParameters.HARD_CAP, String.valueOf(serverInfoService.hardPlayerCap()))
                    .setParameter(ServerPingPacket.ValidParameters.WEIGHT, String.valueOf(serverInfoService.weight()))
                    .setParameter(ServerPingPacket.ValidParameters.PLAYER_COUNT, String.valueOf(serverInfoService.playerCount()))
                    .buildSendable();
            api.flame().backbone().connection().orElseThrow().publish(message);
        } catch (Exception e) {
            PluginLang.BOXED_MESSAGE_COLORED.send(api.logger(), e.toString(), NamedTextColor.RED);
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
                .setParameter(UnlockServerPacket.ValidParameters.SERVER_NAME, serverInfoService.name())
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
                .setParameter(UnlockServerPacket.ValidParameters.SERVER_NAME, serverInfoService.name())
                .buildSendable();

        api.flame().backbone().connection().orElseThrow().publish(message);
    }

    @Override
    public void kill() {}
}
