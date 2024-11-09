package group.aelysium.rustyconnector.plugin.velocity.lang;

import group.aelysium.ara.Particle;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.lang.Lang;
import group.aelysium.rustyconnector.plugin.common.lang.CommonLang;
import group.aelysium.rustyconnector.proxy.family.Family;
import group.aelysium.rustyconnector.proxy.family.FamilyRegistry;
import group.aelysium.rustyconnector.proxy.family.Server;
import group.aelysium.rustyconnector.proxy.family.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.proxy.family.scalar_family.ScalarFamily;
import group.aelysium.rustyconnector.proxy.player.PlayerRegistry;
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

    @Lang("rustyconnector-kernelDetails")
    public static Component usage() {
        return join(
                newlines(),
                space(),
                text("rc families", BLUE),
                text("View all families.", DARK_GRAY),
                space(),
                text("rc servers", BLUE),
                text("View all servers.", DARK_GRAY),
                space(),
                text("rc send", BLUE),
                text("Send a player a family or server.", DARK_GRAY),
                space(),
                text("rc messages", BLUE),
                text("Access recently sent MagicLink packets.", DARK_GRAY),
                space(),
                text("rc reload", BLUE),
                text("Reload RustyConnector.", DARK_GRAY),
                space(),
                text("rc plugins", BLUE),
                text("Get details for RustyConnector modules.", DARK_GRAY),
                space(),
                text("rc errors", BLUE),
                text("Fetches the recent errors thrown by RustyConnector.", DARK_GRAY)
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
        AtomicReference<String> rootFamily = new AtomicReference<>("[Unavailable]");
        RC.P.Families().rootFamily().executeNow(r -> rootFamily.set(r.id()));

        return RC.Lang("rustyconnector-box").generate(
                join(
                        newlines(),
                        RC.Lang().asciiAlphabet().generate("family"),
                        space(),
                        RC.Lang().asciiAlphabet().generate("registry"),
                        space(),
                        RC.Lang("rustyconnector-border").generate(),
                        space(),
                        text(String.join(", ",RC.P.Families().fetchAll().stream().map(f->{
                            AtomicReference<String> id = new AtomicReference<>("[Unavailable]");
                            f.executeNow(fa->id.set(fa.id()));
                            return id.get();
                        }).toList()), BLUE),
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
                        keyValue("UUID",           server.uuid()),
                        keyValue("Display Name",   server.displayName().orElse("None")),
                        keyValue("Address",        AddressUtil.addressToString(server.address())),
                        keyValue("Family",         (family == null ? (missing ? "Unavailable" : "None") : family.id())),
                        keyValue("Online Players", server.players()),
                        keyValue("Player Limit",   "(Soft: "+server.softPlayerCap()+", Hard: "+server.hardPlayerCap()),
                        keyValue("Weight",         server.weight()),
                        keyValue("Locked",         locked),
                        keyValue("Stale",          server.stale())
                )
        );
    };

    @Lang("rustyconnector-family")
    public static Component family(@NotNull Family family) {
        return RC.Lang("rustyconnector-headerBox").generate(
                family.id(),
                RC.Lang("rustyconnector-familyDetails").generate(family)
        );
    };

    @Lang("velocity-serverUsage")
    public static Component velocityServer(Server server) {
        List<String> families = new ArrayList<>();
        RC.P.Families().fetchAll().forEach(flux -> flux.executeNow(f -> families.add(f.id())));
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

    @Lang("rustyconnector-playerRegistryDetails")
    public static Component playerRegistryDetails(PlayerRegistry playerRegistry) {
        return join(
                newlines(),
                keyValue("Players", join(
                        JoinConfiguration.separator(text(", ", DARK_BLUE)),
                        playerRegistry.dump().stream().map(p -> text(p.username(), BLUE)).toList()
                ))
        );
    }

    @Lang("rustyconnector-familyRegistryDetails")
    public static Component familyRegistryDetails(FamilyRegistry familyRegistry) {
        List<Family> families = new ArrayList<>();
        familyRegistry.fetchAll().forEach(f -> f.executeNow(families::add));

        AtomicReference<Family> rootFamily = new AtomicReference<>(null);
        familyRegistry.rootFamily().executeNow(rootFamily::set);
        return join(
                newlines(),
                keyValue("Total Families", familyRegistry.size()),
                keyValue("Available Families", families.size()),
                keyValue("Root Family", rootFamily.get() == null ? "Unavailable" : rootFamily.get().name()),
                keyValue("Families", join(
                        JoinConfiguration.separator(text(", ", DARK_BLUE)),
                        families.stream().map(f -> text(f.name(), BLUE)).toList()
                )),
                space(),
                text("Families are technically considered plugins as well, you can view details for the above families if you'd like.", DARK_GRAY)
        );
    }

    @Lang("rustyconnector-familyDetails")
    public static Component familyDetails(Family family) {
        AtomicReference<String> parentName = new AtomicReference<>("none");
        try {
            Particle.Flux<? extends Family> parent = family.parent().orElse(null);
            if(parent == null) throw new RuntimeException();
            parent.executeLocking(f -> parentName.set(f.id()), ()->parentName.set("[Unavailable]"), 10, TimeUnit.SECONDS);
        } catch (Exception ignore) {}

        return join(
                newlines(),
                keyValue("Display Name", family.displayName() == null ? "No Display Name" : family.displayName()),
                keyValue("Parent Family", parentName.get()),
                keyValue("Servers", family.servers().size()),
                keyValue("Players", family.players()),
                keyValue("Plugins", text(String.join(", ",family.plugins().keySet()), BLUE)),
                space(),
                RC.Lang("rustyconnector-border").generate(),
                space(),
                join(
                        newlines(),
                        family.servers().stream().map(s -> RC.Lang("rustyconnector-serverNugget").generate(s)).toList()
                )
        );
    }

    @Lang("rustyconnector-scalarFamilyDetails")
    public static Component scalarFamilyDetails(ScalarFamily family) {
        return RC.Lang("rustyconnector-familyDetails").generate(family);
    }

    @Lang("rustyconnector-loadBalancerDetails")
    public static Component loadBalancerDetails(LoadBalancer loadBalancer) {
        return join(
                newlines(),
                keyValue("Algorithm", loadBalancer.getClass().getSimpleName()),
                keyValue("Total Servers", loadBalancer.servers().size()),
                keyValue("Unlocked Servers", loadBalancer.unlockedServers().size()),
                keyValue("Locked Servers", loadBalancer.lockedServers().size()),
                keyValue("Weighted", loadBalancer.weighted()),
                keyValue("Persistence", loadBalancer.persistent() ? "Enabled ("+loadBalancer.attempts()+")" : "Disabled")
        );
    }
}
