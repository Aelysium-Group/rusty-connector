package group.aelysium.rustyconnector.plugin.velocity.commands;

import com.velocitypowered.api.proxy.ConsoleCommandSource;
import group.aelysium.ara.Particle;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.RustyConnector;
import group.aelysium.rustyconnector.common.crypt.NanoID;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.magic_link.packet.Packet;
import group.aelysium.rustyconnector.plugin.common.lang.CommonLang;
import group.aelysium.rustyconnector.proxy.family.Family;
import group.aelysium.rustyconnector.proxy.family.Server;
import group.aelysium.rustyconnector.proxy.player.Player;
import group.aelysium.rustyconnector.proxy.player.PlayerRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public final class CommandRusty {

    private static void reply(ConsoleCommandSource source, Component response) {
        source.sendMessage(response);
    }
    private static void reply(ConsoleCommandSource source, Error response) {
        source.sendMessage(response.toComponent());
    }

    @Command("rc")
    public void hizfafjjszjivcys(ConsoleCommandSource sender) {
        reply(sender, RC.Lang("rustyconnector-rootUsage").generate());
    }

    @Command("rc reload")
    public void nglbwcmuvchdjaon(ConsoleCommandSource sender) {
        reply(sender, RC.Lang("rustyconnector-moduleReloadList").generate(RC.Kernel().allPlugins().keySet()));
    }
    @Command("rc reload <module>")
    public void nglbwcmuvchdjaon(ConsoleCommandSource sender, @Argument(value = "module") String module) {
        try {
            if(module.equals("Kernel")) {
                reply(sender, RC.Lang("rustyconnector-waiting").generate());
                Particle.Flux<?> particle = RustyConnector.Toolkit.Proxy().orElseThrow();
                particle.reignite();
                particle.observe();
                reply(sender, RC.Lang("rustyconnector-finished").generate());
                return;
            }

            if(!RC.Kernel().allPlugins().containsKey(module))
                reply(sender, RC.Lang("rustyconnector-moduleReloadList").generate(RC.Kernel().allPlugins().keySet()));

            Particle.Flux<?> particle = RC.Kernel().allPlugins().get(module);
            if(particle == null) throw new NoSuchElementException("No module exists with the name ["+module+"]. Module names are case sensitive.");
            reply(sender, RC.Lang("rustyconnector-waiting").generate());
            particle.reignite();
            particle.observe(1, TimeUnit.MINUTES);
            reply(sender, RC.Lang("rustyconnector-finished").generate());
        } catch (Exception e) {
            reply(sender, Error.from(e).toComponent());
        }
    }

    @Command("rc errors")
    public void nglbwzmxvchdjaon() {
        RC.Adapter().log(
                Component.join(
                        CommonLang.newlines(),
                        Component.space(),
                        RC.Lang("rustyconnector-border").generate(),
                        Component.space(),
                        RC.Lang().asciiAlphabet().generate("Errors").color(NamedTextColor.BLUE),
                        Component.space(),
                        Component.join(
                                CommonLang.newlines(),
                                RC.Errors().fetchAll().stream().map(e->Component.join(
                                            CommonLang.newlines(),
                                            RC.Lang("rustyconnector-border").generate(),
                                            e.toComponent()
                                )).toList()
                        ),
                        RC.Lang("rustyconnector-border").generate()
                )
        );
    }

    @Command("rc errors <uuid>")
    public void nglbwzmxvchdjaon(ConsoleCommandSource sender, @Argument(value = "uuid") String uuid) {
        try {
            UUID errorUUID;
            try {
                errorUUID = UUID.fromString(uuid);
            } catch (IllegalArgumentException e) {
                reply(sender, Error.from(e).wrongValue("A valid UUID.", uuid).urgent(true));
                return;
            }

            Error error = RC.Errors().fetch(errorUUID)
                    .orElseThrow(()->new NoSuchElementException("No Error entry exists with the uuid ["+uuid+"]"));
            if(error.throwable() == null) reply(sender, Error.from(new NoSuchElementException("The error ["+uuid+"] doesn't have a throwable to inspect.")));
            RC.Adapter().log(RC.Lang("rustyconnector-exception").generate(error.throwable()));
        } catch (Exception e) {
            reply(sender, Error.from(e).urgent(true));
        }
    }

    @Command("rc message")
    public void yckarhhyoblbmbdl(ConsoleCommandSource sender) {
        reply(sender, RC.Lang("rustyconnector-messageUsage").generate());
    }

    @Command("rc message list")
    public void pfnommtocuemordk(ConsoleCommandSource sender) {
        try {
            List<Packet.Remote> messages = RC.P.MagicLink().messageCache().messages();
            reply(sender, RC.P.Lang().lang("rustyconnector-messagePage").generate(messages, 1, (int) Math.floor((double) messages.size() / 10)));
        } catch (Exception e) {
            reply(sender, Error.from(e).toComponent());
        }
    }

    @Command("rc message list <page>")
    public void evyfrpitotgxtbmf(ConsoleCommandSource sender, @Argument(value = "page") int page) {
        try {
            List<Packet.Remote> messages = RC.P.MagicLink().messageCache().messages();
            reply(sender, RC.P.Lang().lang("rustyconnector-messagePage").generate(messages, page, (int) Math.floor((double) messages.size() / 10)));
        } catch (Exception e) {
            reply(sender, Error.from(e).toComponent());
        }
    }

    @Command("rc message get")
    public void scfjnwbsynzbksyh(ConsoleCommandSource sender) {
        reply(sender, RC.Lang("rustyconnector-messageGetUsage").generate());
    }

    @Command("rc message get <id>")
    public void nidbtmkngikxlzyo(ConsoleCommandSource sender, @Argument(value = "id") String id) {
        try {
            reply(sender, RC.Lang("rustyconnector-message").generate(
                    RC.P.MagicLink().messageCache().findMessage(NanoID.fromString(id))
            ));
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }

    @Command("rc send")
    public void acmednsmiufxxviz(ConsoleCommandSource sender) {
        reply(sender, RC.Lang("rustyconnector-sendUsage").generate());
    }
    @Command("rc send <username>")
    @Command("rc send <username> server")
    public void acmednrmiufxxviz(ConsoleCommandSource sender, @Argument(value = "username") String username) {
        reply(sender, RC.Lang("rustyconnector-sendUsage").generate());
    }

    @Command("rc send <username> <family_name>")
    public void qxeafgbinengqytu(ConsoleCommandSource sender, @Argument(value = "username") String username, @Argument(value = "family_name") String family_name) {
        try {
            Player player = RC.Kernel().fetchPlugin(PlayerRegistry.class).orElseThrow().fetch(username).orElse(null);
            if (!player.online()) {
                RC.Error(Error.from("No player with the username ["+username+"] is online.").urgent(true));
                return;
            }

            Family family = RC.P.Family(family_name)
                    .orElseThrow(()->new NoSuchElementException("No family with the id ["+family_name+"] exists."));

            Player.Connection.Request request = family.connect(player);
            Player.Connection.Result result = request.result().get(30, TimeUnit.SECONDS);

            if (result.connected()) return;

            reply(sender, result.message());
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }

    @Command("rc send <username> server <server_uuid>")
    public void mlavtgbdguegwcwi(ConsoleCommandSource sender, @Argument(value = "username") String username, @Argument(value = "server_uuid") String server_uuid) {
        try {
            Player player = RC.P.Player(username)
                    .orElseThrow(()->new NoSuchElementException("No player with the username ["+username+"] exists."));
            if (!player.online()) {
                reply(sender, Error.from(username+" isn't online."));
                return;
            }

            Server server = RC.P.Server(UUID.fromString(server_uuid))
                    .orElseThrow(()->new NoSuchElementException("No server with the uuid ["+server_uuid+"] exists."));

            server.connect(player);
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }

    @Command("rc server")
    public void ftuynemwdiuemhid(ConsoleCommandSource sender) {
        reply(sender, RC.Lang("rustyconnector-serverList").generate());
    }

    @Command("rc server <server_uuid>")
    public void fneriygwehmigimh(ConsoleCommandSource sender, @Argument(value = "server_uuid") String server_uuid) {
        try {
            Server server = RC.P.Server(UUID.fromString(server_uuid))
                    .orElseThrow(()->new NoSuchElementException("No server with the uuid ["+server_uuid+"] exists."));
            reply(sender, RC.Lang("rustyconnector-serverDetails").generate(server));
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }

    @Command("rc family")
    public void tdrdolhxvcjhaskb(ConsoleCommandSource sender) {
        reply(sender, RC.Lang("rustyconnector-families").generate());
    }
    @Command("rc family <id>")
    public void mfndwqqzuiqmesyn(ConsoleCommandSource sender, @Argument(value = "id") String id) {
        try {
            Family family = RC.P.Family(id)
                    .orElseThrow(()->new NoSuchElementException("No family with the id ["+id+"] exists."));

            reply(sender, RC.Lang("rustyconnector-family").generate(family));
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }
    @Command("rc family <id> reload")
    public void mfndwqqzwodmesyn(ConsoleCommandSource sender, @Argument(value = "id") String id) {
        try {
            reply(sender, RC.Lang("rustyconnector-waiting").generate());
            Particle.Flux<? extends Family> flux = RC.P.Families().find(id)
                    .orElseThrow(()->new NoSuchElementException("So family with the id ["+id+"] exists."));
            flux.reignite().get(1, TimeUnit.MINUTES);
            reply(sender, RC.Lang("rustyconnector-finished").generate());
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }

    @Command("rc family <id> <plugin_id>")
    public void mfndwmkpwodmesyn(ConsoleCommandSource sender, @Argument(value = "id") String id, @Argument(value = "plugin_id") String pluginID) {
        try {
            Family family = RC.P.Family(id)
                    .orElseThrow(()->new NoSuchElementException("No family with the id ["+id+"] exists."));

            Family.Plugin plugin = family.fetchPlugin(pluginID)
                    .orElseThrow(()->new NoSuchElementException("No plugin with the id ["+pluginID+"] exists."))
                    .orElseThrow(()->new NoSuchElementException("The plugin ["+pluginID+"] isn't currently available. It might be rebooting."));
            reply(sender, RC.Lang("rustyconnector-plugin").generate(plugin));
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }

    @Command("rc family <id> <plugin_id> reload")
    public void mfndwqqzwodmesyn(ConsoleCommandSource sender, @Argument(value = "id") String id, @Argument(value = "plugin_id") String pluginID) {
        try {
            Family family = RC.P.Family(id)
                    .orElseThrow(()->new NoSuchElementException("No family with the id ["+id+"] exists."));

            reply(sender, RC.Lang("rustyconnector-waiting").generate());
            family.fetchPlugin(pluginID)
                    .orElseThrow(()->new NoSuchElementException("No plugin with the id ["+pluginID+"] exists."))
                    .reignite();

            reply(sender, RC.Lang("rustyconnector-finished").generate());
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }
}