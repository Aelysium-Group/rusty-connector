package group.aelysium.rustyconnector.plugin.common.command;

import group.aelysium.ara.Particle;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.RustyConnector;
import group.aelysium.rustyconnector.common.Plugin;
import group.aelysium.rustyconnector.common.crypt.NanoID;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.magic_link.packet.Packet;
import group.aelysium.rustyconnector.plugin.common.lang.CommonLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static net.kyori.adventure.text.Component.text;

public class CommonCommands {

    @Command("rc")
    public void hizfafjjszjivcys(Client<?> client) {
        client.send(RC.Kernel().details());
    }

    @Command("rc reload")
    public void nglbwcmuzzxvjaon(Client<?> client) {
        try {
            client.send(RC.Lang("rustyconnector-waiting").generate());
            Particle.Flux<?> particle = RustyConnector.Toolkit.Proxy().orElseThrow();
            particle.reignite();
            particle.observe();
            client.send(RC.Lang("rustyconnector-finished").generate());
        } catch (Exception e) {
            client.send(Error.from(e).toComponent());
        }
    }

    @Command("rc plugins")
    public void nglbwcmuvchdjaon(Client<?> client) {
        client.send(RC.Lang("rustyconnector-pluginList").generate(RC.Kernel().allPlugins().keySet()));
    }
    @Command("rc plugins <plugin>")
    public void nglbwcmuvchdjaon(Client<?> client, @Argument(value = "plugin") String plugin) {
        try {
            Particle.Flux<? extends Plugin> flux = fetchPlugin(client, plugin);
            if(!flux.exists()) {
                client.send(
                    Error.withHint(
                                "While attempting to fetch the plugin "+plugin+" a plugin in the chain was unavailable.",
                                "This issue typically arises when a plugin is being reloaded. In which case wait a bit before attempting to access it."
                        )
                        .causedBy("Attempting to fetch the plugin "+plugin)
                );
                return;
            }

            client.send(RC.Lang("rustyconnector-details").generate(flux.orElseThrow()));
        } catch (Exception e) {
            client.send(Error.from(e).toComponent());
        }
    }

    @Command("rc plugins <plugin> reload")
    public void nglbwzmspchdjaon(Client<?> client, @Argument(value = "plugin") String plugin) {
        try {
            Particle.Flux<? extends Plugin> flux = fetchPlugin(client, plugin);
            if(flux == null) return;
            client.send(RC.Lang("rustyconnector-waiting").generate());
            flux.reignite().get();
            client.send(RC.Lang("rustyconnector-finished").generate());
        } catch (Exception e) {
            client.send(Error.from(e).toComponent());
        }
    }
    @Command("rc plugins <plugin> stop")
    public void nglbwzmzpsodjaon(Client<?> client, @Argument(value = "plugin") String plugin) {
        Particle.Flux<? extends Plugin> flux = fetchPlugin(client, plugin);
        if(flux == null) return;
        if(!flux.exists()) {
            client.send(RC.Lang("rustyconnector-pluginAlreadyStopped").generate());
            return;
        }
        client.send(RC.Lang("rustyconnector-waiting").generate());
        flux.close();
        client.send(RC.Lang("rustyconnector-finished").generate());
    }
    @Command("rc plugins <plugin> start")
    public void asfdmgfsgsodjaon(Client<?> client, @Argument(value = "plugin") String plugin) {
        try {
            Particle.Flux<? extends Plugin> flux = fetchPlugin(client, plugin);
            if(flux == null) return;
            if(flux.exists()) {
                client.send(RC.Lang("rustyconnector-pluginAlreadyStarted").generate());
                return;
            }
            client.send(RC.Lang("rustyconnector-waiting").generate());
            flux.observe();
            client.send(RC.Lang("rustyconnector-finished").generate());
        } catch (Exception e) {
            client.send(Error.from(e).toComponent());
        }
    }

    private static @Nullable Particle.Flux<? extends Plugin> fetchPlugin(Client<?> client, String plugin) {
        String[] nodes = plugin.split("\\.");
        AtomicReference<Particle.Flux<? extends Plugin>> current = new AtomicReference<>(
                RustyConnector.Toolkit.Kernel()
                .orElseThrow(()->new NoSuchElementException("No RustyConnector Kernel has been registered."))
        );

        for (String node : nodes) {
            if(!current.get().exists()) {
                client.send(Error.withHint(
                                "While attempting to fetch the plugin "+plugin+" a plugin in the chain was unavailable.",
                                "This issue typically arises when a plugin is being reloaded. In which case wait a bit before attempting to access it."
                        )
                        .causedBy("Attempting to fetch the plugin "+plugin)
                );
                return null;
            }

            Plugin resolvedPlugin = current.get().orElseThrow();
            if(!resolvedPlugin.hasPlugins()) {
                client.send(Error.from(
                                node+" doesn't exist on "+resolvedPlugin.name()+". "+resolvedPlugin.name()+" actually doesn't have any children plugins.")
                        .causedBy("Attempting to fetch the plugin "+plugin)
                );
                return null;
            }

            Optional<Particle.Flux<? extends Plugin>> newCurrent = resolvedPlugin.plugins().stream().filter(f->{
                AtomicBoolean matches = new AtomicBoolean(false);
                f.executeNow(p->matches.set(p.name().equalsIgnoreCase(node)));
                return matches.get();
            }).findAny();
            if(newCurrent.isEmpty()) {
                client.send(Error.withSolution(
                            node+" doesn't exist on "+resolvedPlugin.name()+".",
                            "Available plugins are: "+String.join(", "+resolvedPlugin.plugins().stream().map(f->{
                                AtomicReference<String> name = new AtomicReference<>(null);
                                f.executeNow(p->name.set(p.name()));
                                return name.get();
                            }).toList())
                    )
                    .causedBy("Attempting to fetch the plugin "+plugin)
                );
                return null;
            }
            if(!newCurrent.get().exists()) {
                client.send(Error.withHint(
                                "Despite existing and being correct; "+node+" is not currently available. It's probably rebooting.",
                                "This issue typically occurs when a plugin is restarting. You can try again after a little bit, or try reloading the plugin directly and see if that works."
                        )
                        .causedBy("Attempting to fetch the plugin "+plugin)
                );
                return null;
            }

            current.set(newCurrent.orElseThrow());
        }

        return current.get();
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
                        (
                            RC.Errors().fetchAll().isEmpty() ?
                                text("There are no errors to show.", NamedTextColor.DARK_GRAY)
                            :
                                Component.join(
                                    CommonLang.newlines(),
                                    RC.Errors().fetchAll().stream().map(e->Component.join(
                                            CommonLang.newlines(),
                                            RC.Lang("rustyconnector-border").generate(),
                                            e.toComponent()
                                    )).toList()
                                )
                        ),
                        RC.Lang("rustyconnector-border").generate()
                )
        );
    }

    @Command("rc errors <uuid>")
    public void nglbwzmxvchdjaon(Client<?> client, @Argument(value = "uuid") String uuid) {
        try {
            UUID errorUUID;
            try {
                errorUUID = UUID.fromString(uuid);
            } catch (IllegalArgumentException e) {
                client.send(Error.from(e).wrongValue("A valid UUID.", uuid).urgent(true));
                return;
            }

            Error error = RC.Errors().fetch(errorUUID)
                    .orElseThrow(()->new NoSuchElementException("No Error entry exists with the uuid ["+uuid+"]"));
            if(error.throwable() == null) client.send(Error.from(new NoSuchElementException("The error ["+uuid+"] doesn't have a throwable to inspect.")));
            RC.Adapter().log(RC.Lang("rustyconnector-exception").generate(error.throwable()));
        } catch (Exception e) {
            client.send(Error.from(e).urgent(true));
        }
    }

    @Command("rc messages")
    public void yckarhhyoblbmbdl(Client<?> client) {
        try {
            List<Packet.Remote> messages = RC.MagicLink().messageCache().messages();
            client.send(RC.P.Lang().lang("rustyconnector-messages").generate(messages, 1, (int) Math.floor((double) messages.size() / 10)));
        } catch (Exception e) {
            client.send(Error.from(e).toComponent());
        }
    }
    @Command("rc messages <id>")
    public void nidbtmkngikxlzyo(Client<?> client, @Argument(value = "id") String id) {
        try {
            client.send(RC.Lang("rustyconnector-message").generate(
                    RC.P.MagicLink().messageCache().findMessage(NanoID.fromString(id))
            ));
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }
}
