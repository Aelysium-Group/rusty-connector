package group.aelysium.rustyconnector.plugin.paper;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.crypt.NanoID;
import group.aelysium.rustyconnector.common.magic_link.MagicLinkCore;
import group.aelysium.rustyconnector.common.magic_link.packet.Packet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;

import java.util.List;
import java.util.UUID;

public final class CommandRusty {
    private boolean isValid(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) return true;
        reply(sender, RC.Lang("rustyconnector-consoleOnly").generate());
        return false;
    }

    private void reply(CommandSender source, Component response) {
        source.sendMessage(response);
    }

    @Command("rc message")
    public void yckarhhyoblbmbdl(CommandSender sender) {
        if(!isValid(sender)) return;
        reply(sender, RC.Lang("rustyconnector-messageUsage").generate());
    }

    @Command("rc message list")
    public void pfnommtocuemordk(CommandSender sender) {
        if(!isValid(sender)) return;
        try {
            List<Packet.Remote> messages = RC.S.MagicLink().messageCache().messages();
            reply(sender, RC.Lang("rustyconnector-messagePage").generate(messages, 1, (int) Math.floor((double) messages.size() / 10)));
        } catch (Exception e) {
            reply(sender, RC.Lang("rustyconnector-error").generate(e.getMessage()));
        }
    }

    @Command("rc message list <page>")
    public void evyfrpitotgxtbmf(CommandSender sender, @Argument("page") int page) {
        if(!isValid(sender)) return;
        try {
            List<Packet.Remote> messages = RC.S.MagicLink().messageCache().messages();
            reply(sender, RC.Lang("rustyconnector-messagePage").generate(messages, page, (int) Math.floor((double) messages.size() / 10)));
        } catch (Exception e) {
            reply(sender, RC.Lang("rustyconnector-error").generate(e.getMessage()));
        }
    }

    @Command("rc message get")
    public void scfjnwbsynzbksyh(CommandSender sender) {
        if(!isValid(sender)) return;
        reply(sender, RC.Lang("rustyconnector-messageGetUsage").generate());
    }

    @Command("rc message get <id>")
    public void nidbtmkngikxlzyo(CommandSender sender, @Argument("id") String id) {
        if(!isValid(sender)) return;
        try {
            reply(sender, RC.Lang("rustyconnector-message").generate(RC.S.MagicLink().messageCache().findMessage(NanoID.fromString(id))));
        } catch (Exception e) {
            reply(sender, RC.Lang("rustyconnector-error").generate(e.getMessage()));
        }
    }

    @Command("send")
    public void acmednrmiufxxviz(CommandSender sender) {
        if(!isValid(sender)) return;
        reply(sender, RC.Lang("rustyconnector-sendUsage").generate());
    }
    @Command("send <username>")
    public void acmednrmiusgxviz(CommandSender sender, @Argument("username") String username) {
        acmednrmiufxxviz(sender);
    }

    @Command("send <username> <family_name>")
    private  void sertgsdbfdfxxviz(CommandSender sender, @Argument("username") String username, @Argument("family_name") String family_name) {
        if(!isValid(sender)) return;

        UUID uuid = RC.S.Adapter().playerUUID(username).orElse(null);
        if(uuid == null) {
            reply(sender, RC.Lang("rustyconnector-missing2").generate("player", username));
            return;
        }

        Packet.Local packet = RC.S.Kernel().send(uuid, family_name);

        packet.onReply(p -> {
            MagicLinkCore.Packets.ResponsePacket response = new MagicLinkCore.Packets.ResponsePacket(p);
            RC.S.Adapter().log(Component.text(response.message()));
        });
    }
    @Command("send <username> server <server_uuid>")
    private  void sertgsdbgrfxxviz(CommandSender sender, @Argument("username") String username, @Argument("server_uuid") String server_uuid) {
        if(!isValid(sender)) return;

        UUID uuid = RC.S.Adapter().playerUUID(username).orElse(null);
        if(uuid == null) {
            reply(sender, RC.Lang("rustyconnector-missing2").generate("player", username));
            return;
        }

        UUID server = null;
        try {
            server = UUID.fromString(server_uuid);
        } catch (Exception ignore) {
            reply(sender, RC.Lang("rustyconnector-error").generate("The provided server uuid was invalid!"));
        }

        Packet.Local packet = RC.S.Kernel().send(uuid, server);

        packet.onReply(p -> {
            MagicLinkCore.Packets.ResponsePacket response = new MagicLinkCore.Packets.ResponsePacket(p);
            RC.S.Adapter().log(Component.text(response.message()));
        });
    }

    @Command("lock")
    private void zxcvssdbfdfxxviz(CommandSender sender) {
        if(!isValid(sender)) return;
        RC.S.Kernel().lock();
    }

    @Command("unlock")
    private void sertgssdfvsscefd(CommandSender sender) {
        if(!isValid(sender)) return;
        RC.S.Kernel().unlock();
    }
}