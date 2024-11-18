package group.aelysium.rustyconnector.plugin.velocity.commands;

import group.aelysium.ara.Particle;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.crypt.NanoID;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.plugins.Plugin;
import group.aelysium.rustyconnector.plugin.common.command.Client;
import group.aelysium.rustyconnector.plugin.velocity.VirtualFamilyServers;
import group.aelysium.rustyconnector.proxy.family.Family;
import group.aelysium.rustyconnector.proxy.family.Server;
import group.aelysium.rustyconnector.proxy.player.Player;
import group.aelysium.rustyconnector.proxy.player.PlayerRegistry;
import net.kyori.adventure.text.format.NamedTextColor;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.kyori.adventure.text.Component.text;

public final class CommandRusty {
    private final VirtualFamilyServers virtualFamilyServers;
    public CommandRusty(VirtualFamilyServers virtualFamilyServers) {
        this.virtualFamilyServers = virtualFamilyServers;
    }

    @Command("rc send <playerTarget> <target>")
    public void qxeafgbinengqytu(Client<?> client, String playerTarget, String target) {
        Player player = null;
        try {
            try {
                player = RC.P.Player(UUID.fromString(playerTarget)).orElseThrow();
            } catch (Exception ignore) {}
            player = RC.P.Player(playerTarget).orElseThrow();
        } catch (Exception ignore) {}
        if (player == null) {
            client.send(text("No player "+playerTarget+" could be found.", NamedTextColor.DARK_BLUE));
            return;
        }
        if (!player.online()) {
            client.send(text(player.username()+" isn't online.", NamedTextColor.DARK_BLUE));
            return;
        }

        boolean isServer = RC.P.Server(target).isPresent();
        boolean isFamily = RC.P.Family(target).isPresent();

        if(isServer && isFamily) {
            client.send(text("Both a server and family have the id `"+target+"`. Please clarify if you want to send the player to a family or a server.", NamedTextColor.DARK_BLUE));
            return;
        }

        Player.Connectable connectable = null;
        if(isServer) connectable = RC.P.Server(target).orElseThrow();
        if(isFamily) connectable = RC.P.Family(target).orElseThrow();

        if(connectable == null) {
            client.send(RC.Lang("rustyconnector-sendFail").generate(target));
            return;
        }

        try {
            Player.Connection.Request request = connectable.connect(player);
            Player.Connection.Result result = request.result().get(30, TimeUnit.SECONDS);

            if (result.connected()) return;

            client.send(result.message());
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }

    @Command("rc send <playerTarget> <target> family")
    public void mgwsedhgsmudghug(Client<?> client, String playerTarget, String target) {
        Player player = null;
        try {
            try {
                player = RC.P.Player(UUID.fromString(playerTarget)).orElseThrow();
            } catch (Exception ignore) {}
            player = RC.P.Player(playerTarget).orElseThrow();
        } catch (Exception ignore) {}
        if (player == null) {
            client.send(text("No player "+playerTarget+" could be found.", NamedTextColor.DARK_BLUE));
            return;
        }
        if (!player.online()) {
            client.send(text(player.username()+" isn't online.", NamedTextColor.DARK_BLUE));
            return;
        }

        Player.Connectable connectable = RC.P.Family(target).orElseThrow();

        try {
            Player.Connection.Request request = connectable.connect(player);
            Player.Connection.Result result = request.result().get(30, TimeUnit.SECONDS);

            if (result.connected()) return;

            client.send(result.message());
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }
    @Command("rc send <playerTarget> <target> server")
    public void zmasuiymddiumgsa(Client<?> client, String playerTarget, String target) {
        Player player = null;
        try {
            try {
                player = RC.P.Player(UUID.fromString(playerTarget)).orElseThrow();
            } catch (Exception ignore) {}
            player = RC.P.Player(playerTarget).orElseThrow();
        } catch (Exception ignore) {}
        if (player == null) {
            client.send(text("No player "+playerTarget+" could be found.", NamedTextColor.DARK_BLUE));
            return;
        }
        if (!player.online()) {
            client.send(text(player.username()+" isn't online.", NamedTextColor.DARK_BLUE));
            return;
        }

        Player.Connectable connectable =  RC.P.Server(target).orElseThrow();

        try {
            Player.Connection.Request request = connectable.connect(player);
            Player.Connection.Result result = request.result().get(30, TimeUnit.SECONDS);

            if (result.connected()) return;

            client.send(result.message());
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }

    @Command("rc server")
    @Command("rc servers")
    public void ftuynemwdiuemhid(Client<?> client) {
        client.send(RC.Lang("rustyconnector-servers").generate());
    }

    @Command("rc server <serverID>")
    @Command("rc servers <serverID>")
    public void fneriygwehmigimh(Client<?> client, String serverID) {
        try {
            Server server = RC.P.Server(serverID)
                    .orElseThrow(()->new NoSuchElementException("No server with the id '"+serverID+"' exists."));
            client.send(RC.Lang("rustyconnector-serverDetails").generate(server));
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }

    @Command("rc family")
    @Command("rc families")
    public void tdrdolhxvcjhaskb(Client<?> client) {
        client.send(RC.Lang("rustyconnector-families").generate());
    }

    @Command("rc family <id>")
    @Command("rc families <id>")
    public void mfndwqqzuiqmesyn(Client<?> client, String id) {
        try {
            Family family = RC.P.Family(id)
                    .orElseThrow(()->new NoSuchElementException("No family with the id ["+id+"] exists."));

            client.send(RC.Lang("rustyconnector-family").generate(family));
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }
    @Command("rc family <id> reload")
    @Command("rc families <id> reload")
    public void mfndwqqzwodmesyn(Client<?> client, String id) {
        try {
            client.send(RC.Lang("rustyconnector-waiting").generate());
            Particle.Flux<? extends Family> flux = RC.P.Families().find(id)
                    .orElseThrow(()->new NoSuchElementException("So family with the id ["+id+"] exists."));
            flux.reignite().get(1, TimeUnit.MINUTES);
            client.send(RC.Lang("rustyconnector-finished").generate());
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }
}