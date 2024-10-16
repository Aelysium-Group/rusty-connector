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
    public String alreadyConnected() {
        return "You're already connected to this server.";
    }

    @Lang("rustyconnector-rootUsage")
    public Component usage() {
        return RC.Lang("rustyconnector-box").generate(
                join(
                        newlines(),
                        text("family", AQUA),
                        text("View family related information.", DARK_GRAY),
                        space(),
                        text("rc message", AQUA),
                        text("Access recently sent RustyConnector messages.", DARK_GRAY),
                        space(),
                        text("rc reload", GOLD),
                        text("Reload entire plugin.", DARK_GRAY),
                        space(),
                        text("rc send", AQUA),
                        text("Send a player from families and servers to other families or servers.", DARK_GRAY)
                )
        );
    }

    @Lang("rustyconnector-moduleReloadList")
    public Component moduleReloadList(List<String> validModules) {
        return Component.join(
                JoinConfiguration.builder().separator(Component.newline()).build(),
                Component.text("Please provide the name of the module you want to reload. Valid options are: "+String.join(", ", validModules)),
                Component.text("If you wish to reload specific families, or family specific modules, you can do that under the /family menu.")
        );
    }

    @Lang("rustyconnector-offlineMode")
    public Component offlineMode() {
        return RC.Lang("rustyconnector-box").generate(Component.text("Your network is running in offline mode! YOU WILL RECEIVE NO SUPPORT AT ALL WITH RUSTYCONNECTOR!"), RED);
    }

    @Lang("rustyconnector-hybrid")
    public Component hybrid() {
        return RC.Lang("rustyconnector-box").generate(
                Component.join(
                        JoinConfiguration.newlines(),
                        Component.text("Your network is identified as having multiple, pre-defined, non-RC servers in it!"),
                        Component.text("Please note that you will receive no help in regards to making RC work with predefined servers!")
                ), NamedTextColor.RED
        );
    }

    @Lang("rustyconnector-families")
    public Component families() {
        AtomicReference<Component> families = new AtomicReference<>(text(""));

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
                        text("*root family", GRAY),
                        space(),
                        RC.Lang("rustyconnector-border").generate(),
                        space(),
                        text("/rc family <family word_id>",DARK_AQUA),
                        text("See more details about a particular family.", GRAY)
                )
        );
    };

    public Component server(Server server) {
        boolean hasServerName = server.uuid().toString().equals(server.displayName());
        return Component.text("["+server.uuid()+"] "+ (hasServerName ? server.displayName() : "") +"("+ AddressUtil.addressToString(server.address()) +") ["+server.players()+" ("+server.softPlayerCap()+" <--> "+server.hardPlayerCap()+") w-"+server.weight()+"]");
    }

    @Lang("rustyconnector-servers")
    public Component servers() {
        List<Server> servers = RC.P.Servers();
        AtomicReference<Component> serversComponent = new AtomicReference<>(servers.isEmpty() ? text("There are no servers to show.", DARK_GRAY) : empty());
        servers.forEach(s -> serversComponent.set(serversComponent.get().appendNewline().append(this.server(s))));

        return headerBox(
                "servers",
                serversComponent.get()
        );
    };

    @Lang("rustyconnector-family")
    public Component family(@NotNull Family family) {
        AtomicReference<Component> serversComponent = new AtomicReference<>(family.servers().isEmpty() ? text("There are no servers to show.", DARK_GRAY) : empty());
        family.servers().forEach(s -> serversComponent.set(serversComponent.get().appendNewline().append(this.server(s))));

        AtomicReference<String> parentName = new AtomicReference<>("none");
        try {
             Particle.Flux<? extends Family> parent = family.parent().orElse(null);
             if(parent == null) throw new RuntimeException();
             parent.executeLocking(f -> parentName.set(f.id()), ()->parentName.set("exists but unknown (did it crash?)"), 10, TimeUnit.SECONDS);
        } catch (Exception ignore) {}

        return headerBox(family.id(),
                join(
                        newlines(),
                        text("   ---| Parent Family: "+parentName),
                        text("   ---| Plugins: ").append(join(
                                newlines(),
                                family.plugins().stream().map(Component::text).toList()
                        )),
                        space(),
                        RC.Lang("rustyconnector-border").generate(),
                        space(),
                        serversComponent.get()
                )
        );
    };

    @Lang("velocity-serverUsage")
    public Component velocityServer(Server server) {
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
