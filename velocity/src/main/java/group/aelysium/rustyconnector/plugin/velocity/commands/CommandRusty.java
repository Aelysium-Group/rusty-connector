package group.aelysium.rustyconnector.plugin.velocity.commands;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.plugin.common.command.Client;
import group.aelysium.rustyconnector.proxy.family.Family;
import group.aelysium.rustyconnector.proxy.family.Server;
import group.aelysium.rustyconnector.proxy.player.Player;
import group.aelysium.rustyconnector.shaded.group.aelysium.ara.Particle;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_BLUE;

@Command("rc")
@Permission("rustyconnector.commands.rc")
public final class CommandRusty {
    @Command("send <playerTarget> <target>")
    public void qxeafgbinengqytu(Client.Console<?> client, String playerTarget, String target) {
        Player player = null;
        try {
            try {
                player = RC.P.Player(UUID.fromString(playerTarget)).orElseThrow();
            } catch (Exception ignore) {}
            player = RC.P.Player(playerTarget).orElseThrow();
        } catch (Exception ignore) {}
        if (player == null) {
            client.send(text("No player "+playerTarget+" could be found.", DARK_BLUE));
            return;
        }
        if (!player.online()) {
            client.send(text(player.username()+" isn't online.", DARK_BLUE));
            return;
        }

        boolean isServer = RC.P.Server(target).isPresent();
        boolean isFamily = RC.P.Family(target).isPresent();

        if(isServer && isFamily) {
            client.send(text("Both a server and family have the id `"+target+"`. Please clarify if you want to send the player to a family or a server.", DARK_BLUE));
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

    @Command("send <playerTarget> <target> family")
    public void mgwsedhgsmudghug(Client.Console<?> client, String playerTarget, String target) {
        Player player = null;
        try {
            try {
                player = RC.P.Player(UUID.fromString(playerTarget)).orElseThrow();
            } catch (Exception ignore) {}
            player = RC.P.Player(playerTarget).orElseThrow();
        } catch (Exception ignore) {}
        if (player == null) {
            client.send(text("No player "+playerTarget+" could be found.", DARK_BLUE));
            return;
        }
        if (!player.online()) {
            client.send(text(player.username()+" isn't online.", DARK_BLUE));
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
    @Command("send <playerTarget> <target> server")
    public void zmasuiymddiumgsa(Client.Console<?> client, String playerTarget, String target) {
        Player player = null;
        try {
            try {
                player = RC.P.Player(UUID.fromString(playerTarget)).orElseThrow();
            } catch (Exception ignore) {}
            player = RC.P.Player(playerTarget).orElseThrow();
        } catch (Exception ignore) {}
        if (player == null) {
            client.send(text("No player "+playerTarget+" could be found.", DARK_BLUE));
            return;
        }
        if (!player.online()) {
            client.send(text(player.username()+" isn't online.", DARK_BLUE));
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

    @Command("server")
    @Command("servers")
    public void ftuynemwdiuemhid(Client.Console<?> client) {
        client.send(RC.Lang("rustyconnector-servers").generate());
    }

    @Command("server <serverID>")
    @Command("servers <serverID>")
    public void fneriygwehmigimh(Client.Console<?> client, String serverID) {
        try {
            Server server = RC.P.Server(serverID)
                    .orElseThrow(()->new NoSuchElementException("No server with the id '"+serverID+"' exists."));
            client.send(RC.Lang("rustyconnector-serverDetails").generate(server));
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }

    @Command("family")
    @Command("families")
    public void tdrdolhxvcjhaskb(Client.Console<?> client) {
        client.send(RC.Lang("rustyconnector-families").generate());
    }

    @Command("family <id>")
    @Command("families <id>")
    public void mfndwqqzuiqmesyn(Client.Console<?> client, String id) {
        try {
            Family family = RC.P.Family(id)
                    .orElseThrow(()->new NoSuchElementException("No family with the id ["+id+"] exists."));

            client.send(RC.Lang("rustyconnector-family").generate(family));
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }
    @Command("family <id> reload")
    @Command("families <id> reload")
    public void mfndwqqzwodmesyn(Client.Console<?> client, String id) {
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