package group.aelysium.rustyconnector.plugin.velocity;

import com.velocitypowered.api.proxy.ConsoleCommandSource;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.RustyConnector;
import group.aelysium.rustyconnector.common.crypt.NanoID;
import group.aelysium.rustyconnector.common.magic_link.packet.Packet;
import group.aelysium.rustyconnector.proxy.family.Family;
import group.aelysium.rustyconnector.proxy.family.Server;
import group.aelysium.rustyconnector.proxy.player.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class CommandRusty {
    protected void reply(ConsoleCommandSource source, Component response) {
        source.sendMessage(response);
    }

    protected void reply(ConsoleCommandSource source, String response) {
        source.sendMessage(Component.text(response));
    }

    protected void error(ConsoleCommandSource source, String error) {
        source.sendMessage(Component.text(error, NamedTextColor.RED));
    }

    @Command("rc")
    public void hizfafjjszjivcys(ConsoleCommandSource sender) {
        reply(sender, RC.P.Lang().lang().usage());
    }

    @Command("rc debug")
    public void stiuzzsqhudcamko(ConsoleCommandSource sender) {
        try {
            RC.P.Kernel().bootLog().forEach(sender::sendMessage);
        } catch (Exception e) {
            error(sender, "There was an issue fetching the debug log!");
        }
    }

    @Command("rc reload")
    public void nglbwcmuvchdjaon(ConsoleCommandSource sender) {
        try {
            reply(sender, "Reloading the proxy kernel...");
            RustyConnector.Toolkit.Proxy().orElseThrow().reignite(false);
            RustyConnector.Toolkit.Proxy().get();
            reply(sender, "Done reloading!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Command("rc message")
    public void yckarhhyoblbmbdl(ConsoleCommandSource sender) {
        reply(sender, RC.P.Lang().lang().messageUsage());
    }

    @Command("rc message list")
    public void pfnommtocuemordk(ConsoleCommandSource sender) {
        try {
            List<Packet.Remote> messages = RC.P.MagicLink().messageCache().messages();
            reply(sender, RC.P.Lang().lang().messagePage(messages, 1, (int) Math.floor((double) messages.size() / 10)));
        } catch (Exception e) {
            error(sender, "There was an issue getting those messages!\n" + e.getMessage());
        }
    }

    @Command("rc message list <page>")
    public void evyfrpitotgxtbmf(ConsoleCommandSource sender, @Argument(value = "page") int page) {
        try {
            List<Packet.Remote> messages = RC.P.MagicLink().messageCache().messages();
            reply(sender, RC.P.Lang().lang().messagePage(messages, page, (int) Math.floor((double) messages.size() / 10)));
        } catch (Exception e) {
            error(sender, "There was an issue getting those messages!\n" + e.getMessage());
        }
    }

    @Command("rc message get")
    public void scfjnwbsynzbksyh(ConsoleCommandSource sender) {
        reply(sender, RC.P.Lang().lang().messageGetUsage());
    }

    @Command("rc message get <id>")
    public void nidbtmkngikxlzyo(ConsoleCommandSource sender, @Argument(value = "id") String id) {
        try {
            reply(sender, RC.P.MagicLink().messageCache().findMessage(NanoID.fromString(id)).toString());
        } catch (Exception e) {
            error(sender, "There was an issue getting that message!\n" + e.getMessage());
        }
    }

    @Command("send")
    public void acmednsmiufxxviz(ConsoleCommandSource sender) {
        reply(sender, RC.P.Lang().lang().usage());
    }
    @Command("send <username>")
    @Command("send <username> server")
    public void acmednrmiufxxviz(ConsoleCommandSource sender, @Argument(value = "username") String username) {
        reply(sender, RC.P.Lang().lang().usage());
    }

    @Command("send <username> <family_name>")
    public void qxeafgbinengqytu(ConsoleCommandSource sender, @Argument(value = "username") String username, @Argument(value = "family_name") String family_name) {
        try {
            Player player = RC.P.Player(username).orElseThrow();
            if (!player.online()) {
                reply(sender, RC.P.Lang().lang().noPlayer(username));
                return;
            }

            Family family = RC.P.Family(family_name).orElseThrow();

            Player.Connection.Request request = family.connect(player);
            Player.Connection.Result result = request.result().get(30, TimeUnit.SECONDS);

            if (result.connected()) return;

            reply(sender, result.message());
        } catch (NoSuchElementException e) {
            reply(sender, RC.P.Lang().lang().noFamily(family_name));
        } catch (Exception e) {
            error(sender, "There was an issue using that command! " + e.getMessage());
        }
    }

    @Command("send <username> server <server_uuid>")
    public void mlavtgbdguegwcwi(ConsoleCommandSource sender, @Argument(value = "username") String username, @Argument(value = "server_uuid") String server_uuid) {
        try {
            Player player = RC.P.Player(username).orElseThrow();
            if (!player.online()) {
                reply(sender, RC.P.Lang().lang().noPlayer(username));
                return;
            }

            Server server;
            try {
                server = RC.P.Server(UUID.fromString(server_uuid)).orElseThrow();
            } catch (Exception ignore) {
                reply(sender, RC.P.Lang().lang().noServer(server_uuid));
                return;
            }

            server.connect(player);
        } catch (Exception e) {
            error(sender, "There was an issue using that command! " + e.getMessage());
        }
    }

    @Command("family")
    public void tdrdolhxvcjhaskb(ConsoleCommandSource sender) {
        reply(sender, RC.P.Lang().lang().families());
    }

    @Command("family <name>")
    public void mfndwqqzuiqmesyn(ConsoleCommandSource sender, @Argument(value = "name") String name) {
        Family family = RC.P.Family(name).orElseThrow();

        reply(sender, family.details());
    }
}