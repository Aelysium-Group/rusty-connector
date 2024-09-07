package group.aelysium.rustyconnector.plugin.paper;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.cache.CacheableMessage;
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
        RC.S.Adapter().log(Component.text(sender.getName() + " tried using a RustyConnector command. RC commands can only be executed from the console!"));
        error(sender, "This command can only be executed from the server console.");
        return false;
    }

    private void reply(CommandSender source, Component response) {
        source.sendMessage(response);
    }

    private void reply(CommandSender source, String response) {
        source.sendMessage(Component.text(response));
    }

    private void error(CommandSender source, String error) {
        source.sendMessage(Component.text(error, NamedTextColor.RED));
    }


    @Command("rc message")
    public void yckarhhyoblbmbdl(CommandSender sender) {
        if(!isValid(sender)) return;
        reply(sender, RC.S.Lang().lang().messageUsage());
    }

    @Command("rc message list")
    public void pfnommtocuemordk(CommandSender sender) {
        if(!isValid(sender)) return;
        try {
            List<CacheableMessage> messages = RC.S.MagicLink().messageCache().messages();
            reply(sender, RC.S.Lang().lang().messagePage(messages, 1, (int) Math.floor((double) messages.size() / 10)));
        } catch (Exception e) {
            error(sender, "There was an issue getting those messages!\n" + e.getMessage());
        }
    }

    @Command("rc message list <page>")
    public void evyfrpitotgxtbmf(CommandSender sender, @Argument(value = "page") int page) {
        if(!isValid(sender)) return;
        try {
            List<CacheableMessage> messages = RC.S.MagicLink().messageCache().messages();
            reply(sender, RC.S.Lang().lang().messagePage(messages, page, (int) Math.floor((double) messages.size() / 10)));
        } catch (Exception e) {
            error(sender, "There was an issue getting those messages!\n" + e.getMessage());
        }
    }

    @Command("rc message get")
    public void scfjnwbsynzbksyh(CommandSender sender) {
        if(!isValid(sender)) return;
        reply(sender, RC.S.Lang().lang().messageGetUsage());
    }

    @Command("rc message get <snowflake>")
    public void nidbtmkngikxlzyo(CommandSender sender, @Argument(value = "snowflake") long snowflake) {
        if(!isValid(sender)) return;
        try {
            reply(sender, RC.S.MagicLink().messageCache().findMessage(snowflake).toString());
        } catch (Exception e) {
            error(sender, "There was an issue getting that message!\n" + e.getMessage());
        }
    }

    @Command("send")
    @Command("send <username>")
    public void acmednrmiufxxviz(CommandSender sender) {
        if(!isValid(sender)) return;
        reply(sender, RC.S.Lang().lang().sendUsage());
    }

    @Command("send <username> <target>")
    private  void sertgsdbfdfxxviz(CommandSender sender, @Argument(value = "username") String username, @Argument(value = "target") String target) {
        if(!isValid(sender)) return;
        UUID uuid = RC.S.Adapter().playerUUID(username).orElse(null);
        if(uuid == null) {
            error(sender, "A player with that username doesn't exist.");
            return;
        }

        UUID server = null;
        try {
            server = UUID.fromString(target);
        } catch (Exception ignore) {}

        Packet.Local packet;
        if(server != null) packet = RC.S.Kernel().send(uuid, server);
        else packet = RC.S.Kernel().send(uuid, target);

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