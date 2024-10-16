package group.aelysium.rustyconnector.plugin.velocity.commands;

import com.velocitypowered.api.proxy.ConsoleCommandSource;
import group.aelysium.ara.Particle;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.RustyConnector;
import group.aelysium.rustyconnector.common.crypt.NanoID;
import group.aelysium.rustyconnector.common.magic_link.packet.Packet;
import group.aelysium.rustyconnector.proxy.family.Family;
import group.aelysium.rustyconnector.proxy.family.Server;
import group.aelysium.rustyconnector.proxy.player.Player;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public final class CommandRusty {
    private static final Map<String, Callable<Particle.Flux<?>>> modules = Map.of(
        "ALL_MODULES", ()->RustyConnector.Toolkit.Proxy().orElseThrow(),
        "LANG", ()->RC.P.Kernel().Lang(),
        "ALL_FAMILIES", ()->RC.P.Kernel().FamilyRegistry(),
        "MAGIC_LINK", ()->RC.P.Kernel().MagicLink(),
        "PLAYER_REGISTRY", ()->RC.P.Kernel().PlayerRegistry(),
        "EVENT_MANAGER", ()->RC.P.Kernel().EventManager()
    );

    private static void reply(ConsoleCommandSource source, Component response) {
        source.sendMessage(response);
    }

    @Command("rc")
    public static void hizfafjjszjivcys(ConsoleCommandSource sender) {
        reply(sender, RC.Lang("rustyconnector-rootUsage").generate());
    }

    @Command("rc reload")
    public static void nglbwcmuvchdjaon(ConsoleCommandSource sender) {
        reply(sender, RC.Lang("rustyconnector-moduleReloadList").generate(modules.keySet().stream().toList()));
    }
    @Command("rc reload <module>")
    public static void nglbwcmuvchdjaon(ConsoleCommandSource sender, @Argument(value = "module") String module) {
        try {
            if(!modules.containsKey(module.toUpperCase()))
                reply(sender, RC.Lang("rustyconnector-moduleReloadList").generate(modules.keySet().stream().toList()));

            Particle.Flux<?> particle = modules.get(module.toUpperCase()).call();
            reply(sender, RC.Lang("rustyconnector-waiting").generate());
            particle.reignite();
            particle.observe(1, TimeUnit.MINUTES);
            reply(sender, RC.Lang("rustyconnector-finished").generate());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Command("rc message")
    public static void yckarhhyoblbmbdl(ConsoleCommandSource sender) {
        reply(sender, RC.Lang("rustyconnector-messageUsage").generate());
    }

    @Command("rc message list")
    public static void pfnommtocuemordk(ConsoleCommandSource sender) {
        try {
            List<Packet.Remote> messages = RC.P.MagicLink().messageCache().messages();
            reply(sender, RC.P.Lang().lang("rustyconnector-messagePage").generate(messages, 1, (int) Math.floor((double) messages.size() / 10)));
        } catch (Exception e) {
            reply(sender, RC.Lang("rustyconnector-error").generate(e.getMessage()));
        }
    }

    @Command("rc message list <page>")
    public static void evyfrpitotgxtbmf(ConsoleCommandSource sender, @Argument(value = "page") int page) {
        try {
            List<Packet.Remote> messages = RC.P.MagicLink().messageCache().messages();
            reply(sender, RC.P.Lang().lang("rustyconnector-messagePage").generate(messages, page, (int) Math.floor((double) messages.size() / 10)));
        } catch (Exception e) {
            reply(sender, RC.Lang("rustyconnector-error").generate(e.getMessage()));
        }
    }

    @Command("rc message get")
    public static void scfjnwbsynzbksyh(ConsoleCommandSource sender) {
        reply(sender, RC.Lang("rustyconnector-messageGetUsage").generate());
    }

    @Command("rc message get <id>")
    public static void nidbtmkngikxlzyo(ConsoleCommandSource sender, @Argument(value = "id") String id) {
        try {
            reply(sender, RC.Lang("rustyconnector-message").generate(RC.P.MagicLink().messageCache().findMessage(NanoID.fromString(id))));
        } catch (Exception e) {
            reply(sender, RC.Lang("rustyconnector-error").generate(e.getMessage()));
        }
    }

    @Command("send")
    public static void acmednsmiufxxviz(ConsoleCommandSource sender) {
        reply(sender, RC.Lang("rustyconnector-sendUsage").generate());
    }
    @Command("send <username>")
    @Command("send <username> server")
    public static void acmednrmiufxxviz(ConsoleCommandSource sender, @Argument(value = "username") String username) {
        reply(sender, RC.Lang("rustyconnector-sendUsage").generate());
    }

    @Command("send <username> <family_name>")
    public static void qxeafgbinengqytu(ConsoleCommandSource sender, @Argument(value = "username") String username, @Argument(value = "family_name") String family_name) {
        try {
            Player player = RC.P.Player(username).orElseThrow();
            if (!player.online()) {
                reply(sender, RC.Lang("rustyconnector-missing2").generate("user", username));
                return;
            }

            Family family = RC.P.Family(family_name).orElseThrow();

            Player.Connection.Request request = family.connect(player);
            Player.Connection.Result result = request.result().get(30, TimeUnit.SECONDS);

            if (result.connected()) return;

            reply(sender, result.message());
        } catch (NoSuchElementException e) {
            reply(sender, RC.Lang("rustyconnector-missing2").generate("family", family_name));
        } catch (Exception e) {
            reply(sender, RC.Lang("rustyconnector-error").generate(e.getMessage()));
        }
    }

    @Command("send <username> server <server_uuid>")
    public static void mlavtgbdguegwcwi(ConsoleCommandSource sender, @Argument(value = "username") String username, @Argument(value = "server_uuid") String server_uuid) {
        try {
            Player player = RC.P.Player(username).orElseThrow();
            if (!player.online()) {
                reply(sender, RC.Lang("rustyconnector-missing2").generate("user", username));
                return;
            }

            Server server;
            try {
                server = RC.P.Server(UUID.fromString(server_uuid)).orElseThrow();
            } catch (Exception ignore) {
                reply(sender, RC.Lang("rustyconnector-missing2").generate("server", server_uuid));
                return;
            }

            server.connect(player);
        } catch (Exception e) {
            reply(sender, RC.Lang("rustyconnector-error").generate(e.getMessage()));
        }
    }

    @Command("servers")
    public static void tdrdolhdmcjhaskb(ConsoleCommandSource sender) {
        reply(sender, RC.Lang("rustyconnector-servers").generate());
    }

    @Command("family")
    public static void tdrdolhxvcjhaskb(ConsoleCommandSource sender) {
        reply(sender, RC.Lang("rustyconnector-families").generate());
    }
    @Command("family <id>")
    public static void mfndwqqzuiqmesyn(ConsoleCommandSource sender, @Argument(value = "id") String id) {
        Family family = RC.P.Family(id).orElseThrow();

        reply(sender, RC.Lang("rustyconnector-family").generate(family));
    }
    @Command("family <id> reload")
    public static void mfndwqqzwodmesyn(ConsoleCommandSource sender, @Argument(value = "id") String id) {
        try {
            reply(sender, RC.Lang("rustyconnector-waiting").generate());
            Particle.Flux<? extends Family> flux = RC.P.Families().find(id).orElseThrow();
            flux.reignite().get(1, TimeUnit.MINUTES);
            reply(sender, RC.Lang("rustyconnector-finished").generate());
        } catch (NoSuchElementException e) {
            reply(sender, RC.Lang("rustyconnector-missing2").generate("family", id));
        } catch (Exception e) {
            reply(sender, RC.Lang("rustyconnector-error").generate(e.getMessage()));
        }
    }

    @Command("family <id> plugin <plugin_id>")
    public static void mfndwqqzwodmesyn(ConsoleCommandSource sender, @Argument(value = "id") String id, @Argument(value = "pluginID") String pluginID) {
        try {
            reply(sender, RC.Lang("rustyconnector-waiting").generate());
            Family family = RC.P.Family(id).orElseThrow();

            try {

                family.fetchPlugin(pluginID).orElseThrow().reignite();
            } catch (NoSuchElementException e) {
                reply(sender, RC.Lang("rustyconnector-missing2").generate("family plugin", pluginID));
                return;
            }

            reply(sender, RC.Lang("rustyconnector-finished").generate());
        } catch (NoSuchElementException e) {
            reply(sender, RC.Lang("rustyconnector-missing2").generate("family plugin", id));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}