package group.aelysium.rustyconnector.plugin.velocity.lang;

import group.aelysium.ara.Particle;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.errors.Error;
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
                text("rc packets", BLUE),
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

    @Lang("rustyconnector-serverRegister")
    public static Component serverRegister(Server.Configuration server, Particle.Flux<? extends Family> family) {
        Optional<String> displayName = Optional.ofNullable(server.displayName());
        try {
            return join(
                    JoinConfiguration.separator(empty()),
                    text(displayName.orElse(server.uuid().toString()), BLUE),
                    space(),
                    text("(", DARK_GRAY),
                    text(AddressUtil.addressToString(server.address()), YELLOW),
                    text(")", DARK_GRAY),
                    space(),
                    text("->", GREEN),
                    space(),
                    text(family.orElseThrow().id(), GRAY)
            );
        } catch (NoSuchElementException e) {
            RC.Error(Error.from(e).whileAttempting("To inform the console of new server registration (failed silently since not urgent)"));
            return join(
                    JoinConfiguration.separator(empty()),
                    text(displayName.orElse(server.uuid().toString()), BLUE),
                    space(),
                    text("(", DARK_GRAY),
                    text(AddressUtil.addressToString(server.address()), YELLOW),
                    text(")", DARK_GRAY),
                    space(),
                    text("Registered", GREEN)
            );
        }
    }

    @Lang("rustyconnector-serverUnregister")
    public static Component serverUnregister(Server server) {
        try {
            return join(
                    JoinConfiguration.separator(empty()),
                    text(server.displayName().orElse((String) server.property("velocity_registration_name").orElse(server.uuid().toString())), GRAY),
                    space(),
                    text("(", DARK_GRAY),
                    text(AddressUtil.addressToString(server.address()), YELLOW),
                    text(")", DARK_GRAY),
                    space(),
                    text("-x", RED),
                    space(),
                    text(server.family().orElseThrow().orElseThrow().id(), GRAY)
            );
        } catch (NoSuchElementException e) {
            RC.Error(Error.from(e).whileAttempting("To inform the console of new server registration (failed silently since not urgent)"));
            return join(
                    JoinConfiguration.separator(empty()),
                    text(server.displayName().orElse((String) server.property("velocity_registration_name").orElse(server.uuid().toString())), GRAY),
                    space(),
                    text("(", DARK_GRAY),
                    text(AddressUtil.addressToString(server.address()), YELLOW),
                    text(")", DARK_GRAY),
                    space(),
                    text("Unregistered", RED)
            );
        }
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

        return RC.Lang("rustyconnector-headerBox").generate(
                "families",
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
        );
    };

    @Lang("rustyconnector-servers")
    public static Component servers() {
        return RC.Lang("rustyconnector-headerBox").generate(
                "servers",
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
                                    text(s.uuid().toString(), BLUE),
                                    space(),
                                    text(AddressUtil.addressToString(s.address()), YELLOW),
                                    text("]: ", DARK_GRAY),
                                    text(s.displayName().orElse((String) s.property("velocity_registration_name").orElse("")), GRAY),
                                    (
                                        familyName == null ? empty() : join(
                                                JoinConfiguration.separator(empty()),
                                                text(" (Family: ", DARK_GRAY),
                                                text(familyName, DARK_AQUA),
                                                text(")", DARK_GRAY)
                                        )
                                    )
                            );
                        }).toList()
                )
        );
    }

    @Lang("rustyconnector-serverRegistered")
    public static Component serverRegistered(Server server) {
        return Component.text(server.property("velocity_registration_name").orElse(server.uuid().toString())+" ({address}) -> {Family}");
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
                        keyValue("UUID",           server.uuid()),
                        keyValue("Display Name",   server.displayName().orElse("None")),
                        keyValue("Address",        AddressUtil.addressToString(server.address())),
                        keyValue("Family",         (family == null ? (missing ? "Unavailable" : "None") : family.id())),
                        keyValue("Online Players", server.players()),
                        keyValue("Player Limit",   text("(Soft: "+server.softPlayerCap()+", Hard: "+server.hardPlayerCap()+")", GOLD)),
                        keyValue("Weight",         server.weight()),
                        keyValue("Locked",         locked),
                        keyValue("Stale",          server.stale()),
                        empty(),
                        text("Extra Properties:", DARK_GRAY),
                        join(
                                newlines(),
                                server.properties().entrySet().stream().map(e -> keyValue(e.getKey(), e.getValue().toString())).toList()
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
                text("Servers:", DARK_GRAY),
                join(
                        newlines(),
                        family.servers().stream().map(s->{
                            boolean locked = family.isLocked(s);
                            if(locked) return join(
                                    JoinConfiguration.separator(empty()),
                                    text("[", DARK_GRAY),
                                    text(s.uuid().toString(), GRAY),
                                    space(),
                                    text(AddressUtil.addressToString(s.address()), GRAY),
                                    text("]: ", DARK_GRAY),
                                    text((String) s.property("velocity_registration_name").orElse("This server hasn't been registered yet."), GRAY),
                                    space(),
                                    s.displayName().isEmpty() || s.property("velocity_registration_name").isEmpty() ? empty() :
                                            text("["+s.displayName().orElse("No Display Name Exists")+"]", GRAY)
                            );

                            return join(
                                    JoinConfiguration.separator(empty()),
                                    text("[", DARK_GRAY),
                                    text(s.uuid().toString(), BLUE),
                                    space(),
                                    text(AddressUtil.addressToString(s.address()), YELLOW),
                                    text("]: ", DARK_GRAY),
                                    text((String) s.property("velocity_registration_name").orElse("This server hasn't been registered yet."), GREEN),
                                    space(),
                                    s.displayName().isEmpty() || s.property("velocity_registration_name").isEmpty() ? empty() :
                                            text("["+s.displayName().orElse("No Display Name Exists")+"]", DARK_GRAY)
                            );
                        }).toList()
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
