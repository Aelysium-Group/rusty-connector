package group.aelysium.rustyconnector.plugin.common.command;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.RustyConnector;
import group.aelysium.rustyconnector.common.crypt.NanoID;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.magic_link.packet.Packet;
import group.aelysium.rustyconnector.common.plugins.PluginHolder;
import group.aelysium.rustyconnector.plugin.common.lang.CommonLang;
import group.aelysium.rustyconnector.shaded.group.aelysium.ara.Particle;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Nullable;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Command("rc")
@Permission("rustyconnector.commands.rc")
public class CommonCommands {

    @Command("")
    public void hizfafjjszjivcys(Client.Console<?> client) {
        client.send(RC.Lang("rustyconnector-kernelDetails").generate(RC.Kernel()));
    }

    @Command("reload")
    public void nglbwcmuzzxvjaon(Client.Console<?> client) {
        try {
            client.send(RC.Lang("rustyconnector-waiting").generate());
            Particle.Flux<?> particle = RustyConnector.Kernel();
            particle.reignite();
            particle.observe();
            client.send(RC.Lang("rustyconnector-finished").generate());
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }

    @Command("plugin")
    @Command("plugins")
    public void nglbwcmuvchdjaon(Client.Console<?> client) {
        try {
            client.send(RC.Lang("rustyconnector-pluginList").generate(RC.Kernel().plugins().keySet()));
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }
    @Command("plugin <pluginTree>")
    @Command("plugins <pluginTree>")
    public void nglbwcmuschdjaon(Client.Console<?> client, String pluginTree) {
        try {
            Particle.Flux<?> flux = fetchPlugin(client, pluginTree);
            if(flux == null || !flux.exists()) {
                client.send(
                    Error.withHint(
                                "While attempting to fetch the plugin "+pluginTree+" a plugin in the chain was unavailable.",
                                "This issue typically arises when a plugin is being reloaded. In which case wait a bit before attempting to access it."
                        )
                        .causedBy("Attempting to fetch the plugin "+pluginTree)
                );
                return;
            }

            client.send(RC.Lang("rustyconnector-details").generate(flux));
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }

    @Command("plugin <pluginTree> reload")
    @Command("plugins <pluginTree> reload")
    public void nglbwzmspchdjaon(Client.Console<?> client, String pluginTree) {
        try {
            Particle.Flux<?> flux = fetchPlugin(client, pluginTree);
            if(flux == null) return;
            client.send(RC.Lang("rustyconnector-waiting").generate());
            flux.reignite().get();
            client.send(RC.Lang("rustyconnector-finished").generate());
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }
    @Command("plugin <pluginTree> stop")
    @Command("plugins <pluginTree> stop")
    public void nglbwzmzpsodjaon(Client.Console<?> client, String pluginTree) {
        Particle.Flux<?> flux = fetchPlugin(client, pluginTree);
        if(flux == null) return;
        if(!flux.exists()) {
            client.send(RC.Lang("rustyconnector-pluginAlreadyStopped").generate());
            return;
        }
        client.send(RC.Lang("rustyconnector-waiting").generate());
        flux.close();
        try {
            client.send(RC.Lang("rustyconnector-finished").generate());
        } catch (NoSuchElementException e) {
            client.send(Component.text("Successfully stopped that plugin!"));
        }
    }
    @Command("plugin <pluginTree> start")
    @Command("plugins <pluginTree> start")
    public void asfdmgfsgsodjaon(Client.Console<?> client, String pluginTree) {
        try {
            Particle.Flux<?> flux = fetchPlugin(client, pluginTree);
            if(flux == null) return;
            if(flux.exists()) {
                client.send(RC.Lang("rustyconnector-pluginAlreadyStarted").generate());
                return;
            }
            client.send(RC.Lang("rustyconnector-waiting").generate());
            flux.observe();
            client.send(RC.Lang("rustyconnector-finished").generate());
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }

    private static @Nullable Particle.Flux<? extends Particle> fetchPlugin(Client.Console<?> client, String pluginTree) {
        String[] nodes = pluginTree.split("\\.");
        AtomicReference<Particle.Flux<? extends Particle>> current = new AtomicReference<>(RustyConnector.Kernel());

        for (int i = 0; i < nodes.length; i++) {
            String node = nodes[i];
            boolean isLast = i == (nodes.length - 1);
            if(!current.get().exists()) {
                client.send(Error.withHint(
                                "While attempting to fetch the plugin "+pluginTree+" a plugin in the chain was unavailable.",
                                "This issue typically arises when a plugin is being reloaded. In which case wait a bit before attempting to access it."
                        )
                        .causedBy("Attempting to fetch the plugin "+pluginTree)
                );
                return null;
            }

            String name = current.get().metadata("name");
            if(name == null) throw new IllegalArgumentException("Fluxes provided to `rustyconnector-details` must contain `name`, `description`, and `details` metadata.");

            Particle plugin = null;
            try {
                plugin = current.get().observe(3, TimeUnit.SECONDS);
            } catch(Exception ignore) {}

            if(!(plugin instanceof PluginHolder pluginHolder)) {
                client.send(Error.from(
                                node+" doesn't exist on "+name+". "+name+" actually doesn't have any children plugins.")
                        .causedBy("Attempting to fetch the plugin "+pluginTree)
                );
                return null;
            }

            Particle.Flux<? extends Particle> newCurrent = pluginHolder.plugins().get(node);
            if(newCurrent == null) {
                client.send(Error.withSolution(
                            node+" doesn't exist on "+name+".",
                            "Available plugins are: "+String.join(", "+pluginHolder.plugins().keySet())
                    )
                    .causedBy("Attempting to fetch the plugin "+pluginTree)
                );
                return null;
            }
            if(!newCurrent.exists() && !isLast) {
                client.send(Error.withHint(
                                "Despite existing and being correct; "+node+" is not currently available. It's probably rebooting.",
                                "This issue typically occurs when a plugin is restarting. You can try again after a little bit, or try reloading the plugin directly and see if that works."
                        )
                        .causedBy("Attempting to fetch the plugin "+pluginTree)
                );
                return null;
            }

            current.set(newCurrent);
        }

        return current.get();
    }

    @Command("error")
    @Command("errors")
    public void nglbwzmxvchdjaon() {
        RC.Adapter().log(
                Component.join(
                        CommonLang.newlines(),
                        Component.empty(),
                        RC.Lang().asciiAlphabet().generate("Errors").color(NamedTextColor.BLUE),
                        Component.empty(),
                        (
                            RC.Errors().fetchAll().isEmpty() ?
                                    Component.text("There are no errors to show.", NamedTextColor.DARK_GRAY)
                            :
                                Component.join(
                                    CommonLang.newlines(),
                                    RC.Errors().fetchAll().stream().map(e->Component.join(
                                            CommonLang.newlines(),
                                            Component.text("------------------------------------------------------", NamedTextColor.DARK_GRAY),
                                            e.toComponent()
                                    )).toList()
                                )
                        ),
                        Component.empty()
                )
        );
    }

    @Command("error <uuid>")
    @Command("errors <uuid>")
    public void nglbwzmxvchdjaon(Client.Console<?> client, String uuid) {
        try {
            UUID errorUUID;
            try {
                errorUUID = UUID.fromString(uuid);
            } catch (IllegalArgumentException e) {
                client.send(Component.text("Please provide a valid UUID.", NamedTextColor.BLUE));
                return;
            }

            Error error = RC.Errors().fetch(errorUUID)
                    .orElseThrow(()->new NoSuchElementException("No Error entry exists with the uuid ["+uuid+"]"));
            if(error.throwable() == null) client.send(Component.text("The error ["+uuid+"] doesn't have a throwable to inspect.", NamedTextColor.BLUE));
            RC.Adapter().log(RC.Lang("rustyconnector-exception").generate(error.throwable()));
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }

    @Command("packet")
    @Command("packets")
    public void yckarhhyoblbmbdl(Client.Console<?> client) {
        try {
            List<Packet> messages = RC.MagicLink().packetCache().packets();
            client.send(RC.Lang("rustyconnector-packets").generate(messages));
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }

    @Command("packet clear")
    @Command("packets clear")
    public void wuifhmwefmhuidid(Client.Console<?> client) {
        try {
            client.send(RC.Lang("rustyconnector-waiting").generate());
            RC.MagicLink().packetCache().empty();
            client.send(RC.Lang("rustyconnector-finished").generate());
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }

    @Command("packet <id>")
    @Command("packets <id>")
    public void nidbtmkngikxlzyo(Client.Console<?> client, String id) {
        try {
            client.send(RC.Lang("rustyconnector-packetDetails").generate(
                    RC.MagicLink().packetCache().find(NanoID.fromString(id)).orElseThrow(
                            ()->new NoSuchElementException("Unable to find packet with id "+id)
                    )
            ));
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }

    @Command("send")
    public void acmednrmiufxxviz(Client.Console<?> client) {
        client.send(RC.Lang("rustyconnector-sendUsage").generate());
    }
    @Command("send <playerTarget>")
    public void acmednrmiusgxviz(Client.Console<?> client, String playerTarget) {
        acmednrmiufxxviz(client);
    }
}