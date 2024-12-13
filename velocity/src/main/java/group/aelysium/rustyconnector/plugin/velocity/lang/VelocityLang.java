package group.aelysium.rustyconnector.plugin.velocity.lang;

import group.aelysium.ara.Particle;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.lang.Lang;
import group.aelysium.rustyconnector.plugin.common.lang.CommonLang;
import group.aelysium.rustyconnector.proxy.ProxyKernel;
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

import java.util.*;
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
    public static Component usage(ProxyKernel kernel) {
        int families = RC.P.Families().size();
        int servers = RC.P.Servers().size();
        int players = RC.P.Players().dump().size();

        return RC.Lang("rustyconnector-headerBox").generate(
            "proxy",
            join(
                newlines(),
                text("Details:", DARK_GRAY),
                keyValue("ID",          kernel.id()),
                keyValue("Plugins",     kernel.plugins().size()),
                keyValue("Families",    families),
                keyValue("Servers",     servers),
                keyValue("Players",     players),
                empty(),
                text("Commands:", DARK_GRAY),
                text("rc families", BLUE),
                text("View all families.", DARK_GRAY),
                empty(),
                text("rc servers", BLUE),
                text("View all servers.", DARK_GRAY),
                empty(),
                text("rc send", BLUE),
                text("Send a player a family or server.", DARK_GRAY),
                empty(),
                text("rc packets", BLUE),
                text("Access recently sent MagicLink packets.", DARK_GRAY),
                empty(),
                text("rc reload", BLUE),
                text("Reload RustyConnector.", DARK_GRAY),
                empty(),
                text("rc plugins", BLUE),
                text("Get details for RustyConnector modules.", DARK_GRAY),
                empty(),
                text("rc errors", BLUE),
                text("Fetches the recent errors thrown by RustyConnector.", DARK_GRAY)
            )
        );
    }

    @Lang("rustyconnector-offlineMode")
    public static Component offlineMode() {
        return RC.Lang("rustyconnector-box").generate(Component.text("Your network is running in offline mode! YOU WILL RECEIVE NO SUPPORT AT ALL WITH RUSTYCONNECTOR!"), RED);
    }

    @Lang("rustyconnector-serverRegister")
    public static Component serverRegister(Server.Configuration server, Particle.Flux<? extends Family> family) {
        Optional<String> displayName = Optional.ofNullable((String) server.metadata().get("displayName"));
        try {
            return join(
                    JoinConfiguration.separator(empty()),
                    text("[", DARK_GRAY),
                    text(displayName.orElse(server.id()), BLUE),
                    space(),
                    text(AddressUtil.addressToString(server.address()), YELLOW),
                    text("]", DARK_GRAY),
                    space(),
                    text("->", GREEN),
                    space(),
                    text(family.orElseThrow().id(), GRAY)
            );
        } catch (NoSuchElementException e) {
            RC.Error(Error.from(e).whileAttempting("To inform the console of new server registration (failed silently since not urgent)"));
            return join(
                    JoinConfiguration.separator(empty()),
                    text("[", DARK_GRAY),
                    text(displayName.orElse(server.id()), BLUE),
                    space(),
                    text(AddressUtil.addressToString(server.address()), YELLOW),
                    text("]", DARK_GRAY),
                    space(),
                    text("Registered", GREEN)
            );
        }
    }

    @Lang("rustyconnector-serverUnregister")
    public static Component serverUnregister(Server server, Family family) {
        return join(
                JoinConfiguration.separator(empty()),
                text("[", DARK_GRAY),
                text(server.id(), BLUE),
                space(),
                text(AddressUtil.addressToString(server.address()), YELLOW),
                text("]", DARK_GRAY),
                space(),
                text("-x", RED),
                space(),
                text(family.id(), GRAY)
        );
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
        try {
            RC.P.Families().rootFamily().executeNow(r -> rootFamily.set(r.id()));
        } catch (Exception ignore) {}

        return RC.Lang("rustyconnector-headerBox").generate(
                "families",
                (
                    RC.P.Families().size() == 0 ?
                        text("There are no available families at the moment.", DARK_GRAY)
                    :
                        join(
                                newlines(),
                                RC.P.Families().fetchAll().stream().map(flux->{
                                    try {
                                        Family family = flux.orElseThrow();
                                        return join(
                                                JoinConfiguration.separator(empty()),
                                                text("[", DARK_GRAY),
                                                text(family.id(), BLUE),
                                                text("]: ", DARK_GRAY),
                                                (
                                                    family.displayName() == null ? empty() :
                                                    text(family.displayName(), GRAY)
                                                ),
                                                text("(Servers: ", DARK_GRAY),
                                                text(family.servers().size(), YELLOW),
                                                text(") ", DARK_GRAY),
                                                text("(Players: ", DARK_GRAY),
                                                text(family.players(), YELLOW),
                                                text(")", DARK_GRAY),
                                                (
                                                    !family.id().equals(rootFamily.get()) ? empty() : join(
                                                            JoinConfiguration.separator(empty()),
                                                            text(" [", DARK_GRAY),
                                                            text("Root Family", GREEN),
                                                            text("]", DARK_GRAY)
                                                    )
                                                )
                                        );
                                    } catch (Exception ignore) {}
                                    return null;
                                }).filter(Objects::nonNull).toList()
                        )
                )
        );
    };

    @Lang("rustyconnector-servers")
    public static Component servers() {
        return RC.Lang("rustyconnector-headerBox").generate(
                "servers",
                (
                    RC.P.Servers().isEmpty() ?
                        text("There are no servers to show.", DARK_GRAY)
                        :
                        join(
                            newlines(),
                            RC.P.Servers().stream().map(s->{
                                String familyName = null;
                                try {
                                    familyName = s.family().orElseThrow().orElseThrow().id();
                                } catch (Exception ignore) {}

                                return join(
                                        JoinConfiguration.separator(empty()),
                                        text("[", DARK_GRAY),
                                        text(s.id(), BLUE),
                                        space(),
                                        text(AddressUtil.addressToString(s.address()), YELLOW),
                                        text("]:", DARK_GRAY),
                                        space(),
                                        (
                                                s.displayName() == null ? empty() :
                                                text(Objects.requireNonNull(s.displayName()), AQUA)
                                                .append(space())
                                        ),
                                        text("(Players: ", DARK_GRAY),
                                        text(s.players(), DARK_AQUA),
                                        text(")", DARK_GRAY),
                                        space(),
                                        (
                                                familyName == null ? text("This server has no family", DARK_GRAY) : join(
                                                        JoinConfiguration.separator(empty()),
                                                        text("(Family: ", DARK_GRAY),
                                                        text(familyName, DARK_AQUA),
                                                        text(")", DARK_GRAY)
                                                )
                                        )
                                );
                            }).toList()
                        )
                )
        );
    }

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

        return RC.Lang("rustyconnector-headerBox").generate(
                "server",
                join(
                        newlines(),
                        text("Details:", DARK_GRAY),
                        keyValue("ID",           server.id()),
                        keyValue("Address",        AddressUtil.addressToString(server.address())),
                        keyValue("Family",         (family == null ? (missing ? "Unavailable" : "None") : family.id())),
                        keyValue("Online Players", server.players()),
                        keyValue("Weight",         server.weight()),
                        keyValue("Locked",         locked),
                        keyValue("Stale",          server.stale()),
                        empty(),
                        text("Extra Properties:", DARK_GRAY),
                        (
                            server.metadata().isEmpty() ?
                                text("There are no properties to show.", DARK_GRAY)
                            :
                                join(
                                        newlines(),
                                        server.metadata().entrySet().stream().map(e -> keyValue(e.getKey(), e.getValue())).toList()
                                )
                        )

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
        try {
            familyRegistry.rootFamily().executeNow(rootFamily::set);
        } catch (Exception ignore) {}
        return join(
                newlines(),
                keyValue("Total Families", familyRegistry.size()),
                keyValue("Available Families", families.size()),
                keyValue("Root Family", rootFamily.get() == null ? "Unavailable" : rootFamily.get().id()),
                keyValue("Families", join(
                        JoinConfiguration.separator(text(", ", DARK_BLUE)),
                        families.stream().map(f -> text(f.id(), BLUE)).toList()
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
                text("Extra Properties:", DARK_GRAY),
                (
                    family.metadata().isEmpty() ?
                        text("There are no properties to show.", DARK_GRAY)
                        :
                        join(
                            newlines(),
                            family.metadata().entrySet().stream().map(e -> keyValue(e.getKey(), e.getValue())).toList()
                        )
                ),
                space(),
                text("Servers:", DARK_GRAY),
                (
                    family.servers().isEmpty() ?
                        text("There are no servers in this family.", DARK_GRAY)
                    :
                        join(
                                newlines(),
                                family.servers().stream().map(s->{
                                    boolean locked = family.isLocked(s);
                                    return join(
                                            JoinConfiguration.separator(empty()),
                                            text("[", DARK_GRAY),
                                            text(s.id(), BLUE),
                                            space(),
                                            text(AddressUtil.addressToString(s.address()), YELLOW),
                                            text("]:", DARK_GRAY),
                                            space(),
                                            (
                                                    s.displayName() == null ? empty() :
                                                    text(Objects.requireNonNull(s.displayName()), AQUA)
                                                    .append(space())
                                            ),
                                            text("(Players: ", DARK_GRAY),
                                            text(s.players(), YELLOW),
                                            text(")", DARK_GRAY),
                                            space(),
                                            (
                                                locked ? text("Locked", RED) : empty()
                                            )
                                    );
                                }).toList()
                        )
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
