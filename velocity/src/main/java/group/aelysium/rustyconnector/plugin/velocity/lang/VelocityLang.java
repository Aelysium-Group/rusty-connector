package group.aelysium.rustyconnector.plugin.velocity.lang;

import group.aelysium.ara.Particle;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.lang.Lang;
import group.aelysium.rustyconnector.plugin.common.lang.CommonLang;
import group.aelysium.rustyconnector.proxy.family.Family;
import group.aelysium.rustyconnector.proxy.family.Server;
import group.aelysium.rustyconnector.proxy.util.AddressUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;

public class VelocityLang extends CommonLang {
    public static String alreadyConnected() {
        return "You're already connected to this server.";
    }

    @Lang("rustyconnector-rootUsage")
    public static Component usage() {
        return RC.Lang("rustyconnector-box").generate(
                join(
                        newlines(),
                        text("rc family", BLUE),
                        text("View all families.", DARK_GRAY),
                        space(),
                        text("rc server", BLUE),
                        text("View all servers.", DARK_GRAY),
                        space(),
                        text("rc send", BLUE),
                        text("Send a player a family or server.", DARK_GRAY),
                        space(),
                        text("rc message", BLUE),
                        text("Access recently sent MagicLink packets.", DARK_GRAY),
                        space(),
                        text("rc reload", BLUE),
                        text("Reload entire plugin.", DARK_GRAY),
                        space(),
                        text("rc errors", BLUE),
                        text("Fetches the recent errors thrown by RustyConnector.", DARK_GRAY)
                )
        );
    }

    @Lang("rustyconnector-offlineMode")
    public static Component offlineMode() {
        return RC.Lang("rustyconnector-box").generate(Component.text("Your network is running in offline mode! YOU WILL RECEIVE NO SUPPORT AT ALL WITH RUSTYCONNECTOR!"), RED);
    }

    @Lang("rustyconnector-hybrid")
    public static Component hybrid() {
        return RC.Lang("rustyconnector-box").generate(
                Component.join(
                        JoinConfiguration.newlines(),
                        Component.text("Your network is identified as having multiple, pre-defined, non-RC servers in it!"),
                        Component.text("Please note that you will receive no help in regards to making RC work with predefined servers!")
                ), NamedTextColor.RED
        );
    }

    @Lang("rustyconnector-families")
    public static Component families() {
        AtomicReference<Component> families = new AtomicReference<>(text(""));
        AtomicReference<String> rootFamily = new AtomicReference<>("Unknown");
        RC.P.Families().rootFamily().executeNow(r -> rootFamily.set(r.id()));

        for (Particle.Flux<? extends Family> family : RC.P.Families().dump())
            family.executeNow(f -> families.set(families.get().append(text("["+f.id()+"*] ").color(BLUE))));

        return RC.Lang("rustyconnector-box").generate(
                join(
                        newlines(),
                        RC.Lang().asciiAlphabet().generate("family"),
                        space(),
                        RC.Lang().asciiAlphabet().generate("registry"),
                        space(),
                        RC.Lang("rustyconnector-border").generate(),
                        space(),
                        families.get(),
                        space(),
                        text("Root Family: ", BLUE).append(text(rootFamily.get(), DARK_GRAY)),
                        space(),
                        RC.Lang("rustyconnector-border").generate(),
                        space(),
                        text("/rc family <family word_id>",BLUE),
                        text("See more details about a particular family.", GRAY)
                )
        );
    };

    @Lang("rustyconnector-serverNugget")
    public static Component serverNugget(Server server) {
        boolean hasServerName = server.uuid().toString().equals(server.displayName());
        return Component.text("["+server.uuid()+"] "+ (hasServerName ? server.displayName() : "") +"("+ AddressUtil.addressToString(server.address()) +") ["+server.players()+" ("+server.softPlayerCap()+" <--> "+server.hardPlayerCap()+") w-"+server.weight()+"]");
    }

    @Lang("rustyconnector-serverList")
    public static Component serverList() {
        List<Server> servers = RC.P.Servers();
        AtomicReference<Component> serversComponent = new AtomicReference<>(servers.isEmpty() ? text("There are no servers to show.", DARK_GRAY) : empty());
        servers.forEach(s -> serversComponent.set(serversComponent.get().appendNewline().append(RC.Lang("rustyconnector-serverNugget").generate(s))));

        return RC.Lang("rustyconnector-headerBox").generate(
                "servers",
                serversComponent.get()
        );
    };

    @Lang("rustyconnector-serverDetails")
    public static Component serverDetails(Server server) {
        boolean missing = server.family().isEmpty();
        Family family = null;
        try {
            family = server.family().orElseThrow().orElseThrow();
        } catch (Exception ignore) {}
        boolean locked = false;
        try {
            if(family == null) throw new NullPointerException();
            locked = family.isLocked(server);
        } catch (Exception ignore) {}

        return RC.Lang("rustyconnector-box").generate(
                join(
                        newlines(),
                        RC.Lang("rustyconnector-border").generate(),
                        space(),
                        text(""),
                        space(),
                        RC.Lang("rustyconnector-border").generate(),
                        space(),
                        text("Details:", DARK_GRAY),
                        text(" • UUID: ",           DARK_GRAY).append(RC.Lang("rustyconnector-typedValue").generate(server.uuid())),
                        text(" • Display Name: ",   DARK_GRAY).append(RC.Lang("rustyconnector-typedValue").generate(server.displayName().orElse("None"))),
                        text(" • Address: ",        DARK_GRAY).append(RC.Lang("rustyconnector-typedValue").generate(AddressUtil.addressToString(server.address()))),
                        text(" • Family: ",         DARK_GRAY).append(RC.Lang("rustyconnector-typedValue").generate((family == null ? (missing ? "Unavailable" : "None") : family.id()))),
                        text(" • Online Players: ", DARK_GRAY).append(RC.Lang("rustyconnector-typedValue").generate(server.players())),
                        text(" • Player Limit: ",   DARK_GRAY).append(RC.Lang("rustyconnector-typedValue").generate("(Soft: "+server.softPlayerCap()+", Hard: "+server.hardPlayerCap())),
                        text(" • Weight: ",         DARK_GRAY).append(RC.Lang("rustyconnector-typedValue").generate(server.weight())),
                        text(" • Locked: ",         DARK_GRAY).append(RC.Lang("rustyconnector-typedValue").generate(locked)),
                        text(" • Stale: ",          DARK_GRAY).append(RC.Lang("rustyconnector-typedValue").generate(server.stale()))
                )
        );
    };

    @Lang("rustyconnector-family")
    public static Component family(@NotNull Family family) {
        AtomicReference<Component> serversComponent = new AtomicReference<>(family.servers().isEmpty() ? text("There are no servers to show.", DARK_GRAY) : empty());
        family.servers().forEach(s -> serversComponent.set(serversComponent.get().appendNewline().append(RC.Lang("rustyconnector-serverNugget").generate(s))));

        AtomicReference<String> parentName = new AtomicReference<>("none");
        try {
            Particle.Flux<? extends Family> parent = family.parent().orElse(null);
            if(parent == null) throw new RuntimeException();
            parent.executeLocking(f -> parentName.set(f.id()), ()->parentName.set("exists but unknown (did it crash?)"), 10, TimeUnit.SECONDS);
        } catch (Exception ignore) {}

        return RC.Lang("rustyconnector-headerBox").generate(
                family.id(),
                join(
                        newlines(),
                        text("Details:", DARK_GRAY),
                        text(" • Parent Family: ", DARK_GRAY).append(RC.Lang("rustyconnector-typedValue").generate(parentName)),
                        text(" • Plugins: ",       DARK_GRAY).append(RC.Lang("rustyconnector-typedValue").generate(join(
                                newlines(),
                                family.plugins().stream().map(Component::text).toList()
                        ))),
                        space(),
                        RC.Lang("rustyconnector-border").generate(),
                        space(),
                        serversComponent.get()
                )
        );
    };

    @Lang("rustyconnector-plugin")
    public static Component plugin(@NotNull Family.Plugin plugin) {
        return RC.Lang("rustyconnector-headerBox").generate(
                plugin.name(),
                join(
                        newlines(),
                        text("Details:", DARK_GRAY),
                        text(" • Name: ", DARK_GRAY).append(RC.Lang("rustyconnector-typedValue").generate(plugin.name())),
                        join(
                                newlines(),
                                plugin.details().entrySet().stream().map(e->text(" • "+e.getKey()+": ", DARK_GRAY).append(RC.Lang("rustyconnector-typedValue").generate(e.getValue()))).toList()
                        )
                )
        );
    };

    @Lang("velocity-serverUsage")
    public static Component velocityServer(Server server) {
        List<String> families = new ArrayList<>();
        RC.P.Families().dump().forEach(flux -> flux.executeNow(f -> families.add(f.id())));
        try {
            Family family = server.family().orElseThrow().observe(1, TimeUnit.MINUTES);

            return join(
                    newlines(),
                    text("You are currently on server: "+family.id()),
                    text("Available servers: "+families),
                    text("Type /server <server_name> to switch to another server.")
            );
        } catch (Exception e) {
            return join(
                    newlines(),
                    text("You are currently on an unknown server."),
                    text("Available servers: "+String.join(", ", families)),
                    text("Type /server <server_name> to switch to another server.")
            );
        }
    }
}
