package group.aelysium.rustyconnector.toolkit.proxy.lang;

import group.aelysium.rustyconnector.toolkit.common.lang.ASCIIAlphabet;
import group.aelysium.rustyconnector.toolkit.RC;
import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;
import group.aelysium.rustyconnector.toolkit.common.lang.Lang;
import group.aelysium.rustyconnector.toolkit.proxy.family.Family;
import group.aelysium.rustyconnector.toolkit.proxy.family.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.toolkit.proxy.family.scalar_family.ScalarFamily;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class ProxyLang extends Lang {
    public ProxyLang(ASCIIAlphabet asciiAlphabet) {
        super(asciiAlphabet);
    }

    public String no_player(String username) {
        return "There is no online player with the username " + username;
    }
    public String already_connected() {
        return "You're already connected to this server.";
    }

    public Component usage() {
        return usageBox(
            join(
                newlines(),
                text("/rc family", AQUA),
                text("View family related information.", DARK_GRAY),
                space(),
                text("/rc message", AQUA),
                text("Access recently sent RustyConnector messages.", DARK_GRAY),
                space(),
                text("/rc reload", GOLD),
                text("Reload entire plugin.", DARK_GRAY),
                space(),
                text("/rc send", AQUA),
                text("Send players from families and servers to other families or servers.", DARK_GRAY)
            )
        );
    }

    public String noFamily(@NotNull String familyName) {
        return "There is no family with the name: " + familyName;
    }
    public String noServer(@NotNull String serverName) {
        return "There is no server with the name: " + serverName;
    }
    public String sameFamily() {
        return "You're already in that server.";
    }

    public Component families() {
        AtomicReference<Component> families = new AtomicReference<>(text(""));

        for (Particle.Flux<Family> family : RC.P.Families().dump())
            family.executeNow(f -> families.set(families.get().append(text("["+f.id()+"*] ").color(BLUE))));

        return boxed(
            join(
                newlines(),
                this.asciiAlphabet.generate("registered"),
                space(),
                this.asciiAlphabet.generate("families"),
                space(),
                border(),
                space(),
                families.get(),
                text("*root family", GRAY),
                space(),
                border(),
                space(),
                text("/rc family <family word_id>",DARK_AQUA),
                text("See more details about a particular family.", GRAY)
            )
        );
    };

    public Component loadBalancer(LoadBalancer loadBalancer) {
        int locked = loadBalancer.lockedMCLoaders().size();
        int unlocked = loadBalancer.unlockedMCLoaders().size();
        int total = locked + unlocked;

        double lockedPercentage = 0;
        try {
            lockedPercentage = (double) locked / total;
        } catch (Exception ignore) {}

        int totalBlocks = 200;
        double lockedBlocks = Math.floor(totalBlocks * lockedPercentage);

        StringBuilder lockedBlockSB = new StringBuilder();
        for (int i = 0; i < lockedBlocks; i++)
            lockedBlockSB.append("█");
        StringBuilder unlockedBlockSB = new StringBuilder();
        for (int i = 0; i < totalBlocks - lockedBlocks; i++)
            unlockedBlockSB.append("█");

        Component blocks = join(
                JoinConfiguration.noSeparators(),
                text(lockedBlockSB.toString(), GRAY),
                text(unlockedBlockSB.toString(), GREEN)
        );

        return blocks;
    }

    public Component scalarFamily(ScalarFamily family) throws ExecutionException, InterruptedException, TimeoutException {
        Component servers = text("");
        int i = 0;

        LoadBalancer loadBalancer = family.loadBalancer().access().get(5, TimeUnit.SECONDS);

        if(family.mcloaders().isEmpty()) servers = text("There are no registered servers.", DARK_GRAY);
        else if(family.unlockedMCLoaders().isEmpty()) servers = text("All the MCLoaders in this family are locked.", DARK_GRAY);

        Family rootFamily = RC.P.Families().rootFamily().orElseThrow();
        String parentFamilyName = rootFamily.id();
        try {
            parentFamilyName = family.parent().orElseThrow().orElseThrow().id();
        } catch (Exception ignore) {}
        if(family.equals(rootFamily)) parentFamilyName = "none";

        return headerBox(family.id(),
            join(
                newlines(),
                text("   ---| Display Name: "+family.displayName()),
                text("   ---| Parent Family: "+parentFamilyName),
                text("   ---| Online Players: "+family.players()),
                text(""),
                text("   ---| Servers:"),
                text("      | - Total: "+family.mcloaders().size()),
                text("      | - Open: "),
                text("      | - Locked: "),
                space(),
                border(),
                space(),
                text("Open Servers", AQUA),
                space(),
                text("/rc family <family word_id> sort", GOLD),
                text("Resort all mcloaders in the family.", DARK_GRAY),
                space(),
                text("/rc family <family word_id> resetIndex", GOLD),
                text("Reset player insertion point to first mcloader in the family.", DARK_GRAY),
                space(),
                text("/rc family <family word_id> locked", GOLD),
                text("View mcloaders that are locked."),
                space(),
                loadBalancer(loadBalancer)
            )
        );
    };
}
