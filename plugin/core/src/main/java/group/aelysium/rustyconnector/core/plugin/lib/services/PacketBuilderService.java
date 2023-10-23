package group.aelysium.rustyconnector.core.plugin.lib.services;

import group.aelysium.rustyconnector.core.central.Tinder;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.core.lib.packets.PacketOrigin;
import group.aelysium.rustyconnector.core.lib.packets.PacketType;
import group.aelysium.rustyconnector.core.lib.packets.variants.LockServerPacket;
import group.aelysium.rustyconnector.core.lib.packets.variants.UnlockServerPacket;
import group.aelysium.rustyconnector.core.lib.packets.variants.SendPlayerPacket;
import group.aelysium.rustyconnector.core.lib.packets.variants.ServerPingPacket;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.core.plugin.Plugin;
import group.aelysium.rustyconnector.core.plugin.lib.lang.PluginLang;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.UUID;

public class PacketBuilderService extends Service {
    public void pingProxy(ServerPingPacket.ConnectionIntent intent) {
        Tinder api = Plugin.getAPI();

        try {
            ServerInfoService serverInfoService = api.services().serverInfo();
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
        Tinder api = Plugin.getAPI();
        ServerInfoService serverInfoService = api.services().serverInfo();

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
        Tinder api = Plugin.getAPI();
        ServerInfoService serverInfoService = api.services().serverInfo();

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
        Tinder api = Plugin.getAPI();
        ServerInfoService serverInfoService = api.services().serverInfo();

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
