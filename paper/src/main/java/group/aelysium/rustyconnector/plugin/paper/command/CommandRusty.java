package group.aelysium.rustyconnector.plugin.paper.command;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.magic_link.MagicLinkCore;
import group.aelysium.rustyconnector.common.magic_link.packet.Packet;
import group.aelysium.rustyconnector.plugin.common.command.Client;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;

import java.util.NoSuchElementException;
import java.util.UUID;

public final class CommandRusty {
    @Command("send")
    public void acmednrmiufxxviz(Client<?> client) {
        client.send(RC.Lang("rustyconnector-sendUsage").generate());
    }
    @Command("send <username>")
    public void acmednrmiusgxviz(Client<?> client, @Argument("username") String username) {
        acmednrmiufxxviz(client);
    }

    @Command("send <username> <family_name>")
    private  void sertgsdbfdfxxviz(Client<?> client, @Argument("username") String username, @Argument("family_name") String family_name) {
        try {
            UUID uuid = RC.S.Adapter().playerUUID(username)
                    .orElseThrow(()->new NoSuchElementException("Unable to get the uuid for the username ["+username+"]."));

            Packet.Local packet = RC.S.Kernel().send(uuid, family_name);

            packet.onReply(p -> {
                MagicLinkCore.Packets.ResponsePacket response = new MagicLinkCore.Packets.ResponsePacket(p);
                RC.S.Adapter().log(Component.text(response.message()));
            });
        } catch (Exception e) {
            client.send(Error.from(e).urgent(true));
        }
    }
    @Command("send <username> server <server_uuid>")
    private  void sertgsdbgrfxxviz(Client<?> client, @Argument("username") String username, @Argument("server_uuid") String server_uuid) {
        try {
            UUID uuid = RC.S.Adapter().playerUUID(username)
                    .orElseThrow(()->new NoSuchElementException("Unable to get the uuid for the username ["+username+"]."));

            UUID server = UUID.fromString(server_uuid);

            Packet.Local packet = RC.S.Kernel().send(uuid, server);

            packet.onReply(p -> {
                MagicLinkCore.Packets.ResponsePacket response = new MagicLinkCore.Packets.ResponsePacket(p);
                RC.S.Adapter().log(Component.text(response.message()));
            });
        } catch (Exception e) {
            client.send(Error.from(e).urgent(true));
        }
    }

    @Command("lock")
    private void zxcvssdbfdfxxviz(Client<?> client) {
        RC.S.Kernel().lock();
    }

    @Command("unlock")
    private void sertgssdfvsscefd(Client<?> client) {
        RC.S.Kernel().unlock();
    }
}