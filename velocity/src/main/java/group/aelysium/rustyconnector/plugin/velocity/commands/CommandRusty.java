package group.aelysium.rustyconnector.plugin.velocity.commands;

import group.aelysium.ara.Particle;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.crypt.NanoID;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.plugins.Plugin;
import group.aelysium.rustyconnector.plugin.common.command.Client;
import group.aelysium.rustyconnector.proxy.family.Family;
import group.aelysium.rustyconnector.proxy.family.Server;
import group.aelysium.rustyconnector.proxy.player.Player;
import group.aelysium.rustyconnector.proxy.player.PlayerRegistry;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;

import java.util.*;
import java.util.concurrent.TimeUnit;

public final class CommandRusty {
    @Command("rc send <username> <target>")
    public void qxeafgbinengqytu(Client<?> client, @Argument(value = "username") String username, @Argument(value = "target") String target) {
        Player player = RC.P.Player(username)
                .orElseThrow(()->new NoSuchElementException("No player with the username ["+username+"] exists."));
        if (!player.online()) {
            client.send(Error.from(username+" isn't online."));
            return;
        }

        Player.Connectable connectable = null;
        try {
            connectable = RC.P.Server(UUID.fromString(target)).orElseThrow();
        } catch (Exception ignore) {}
        try {
            connectable = RC.P.Family(target).orElseThrow();
        } catch (Exception ignore) {}

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

    @Command("rc send <username> server <server_uuid>")
    public void mlavtgbdguegwcwi(Client<?> client, @Argument(value = "username") String username, @Argument(value = "server_uuid") String server_uuid) {

    }

    @Command("rc server")
    @Command("rc servers")
    public void ftuynemwdiuemhid(Client<?> client) {
        client.send(RC.Lang("rustyconnector-servers").generate());
    }

    @Command("rc server <server_uuid>")
    @Command("rc servers <server_uuid>")
    public void fneriygwehmigimh(Client<?> client, @Argument(value = "server_uuid") String server_uuid) {
        try {
            Server server = RC.P.Server(UUID.fromString(server_uuid))
                    .orElseThrow(()->new NoSuchElementException("No server with the uuid ["+server_uuid+"] exists."));
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
    public void mfndwqqzuiqmesyn(Client<?> client, @Argument(value = "id") String id) {
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
    public void mfndwqqzwodmesyn(Client<?> client, @Argument(value = "id") String id) {
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

    @Command("rc family <id> <plugin_id>")
    @Command("rc families <id> <plugin_id>")
    public void mfndwmkpwodmesyn(Client<?> client, @Argument(value = "id") String id, @Argument(value = "plugin_id") String pluginID) {
        try {
            Family family = RC.P.Family(id)
                    .orElseThrow(()->new NoSuchElementException("No family with the id ["+id+"] exists."));

            Plugin plugin = family.fetchPlugin(pluginID)
                    .orElseThrow(()->new NoSuchElementException("No plugin with the id ["+pluginID+"] exists."))
                    .orElseThrow(()->new NoSuchElementException("The plugin ["+pluginID+"] isn't currently available. It might be rebooting."));
            client.send(RC.Lang("rustyconnector-plugin").generate(plugin));
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }

    @Command("rc family <id> <plugin_id> reload")
    @Command("rc families <id> <plugin_id> reload")
    public void mfndwqqzwodmesyn(Client<?> client, @Argument(value = "id") String id, @Argument(value = "plugin_id") String pluginID) {
        try {
            Family family = RC.P.Family(id)
                    .orElseThrow(()->new NoSuchElementException("No family with the id ["+id+"] exists."));

            client.send(RC.Lang("rustyconnector-waiting").generate());
            family.fetchPlugin(pluginID)
                    .orElseThrow(()->new NoSuchElementException("No plugin with the id ["+pluginID+"] exists."))
                    .reignite();

            client.send(RC.Lang("rustyconnector-finished").generate());
        } catch (Exception e) {
            RC.Error(Error.from(e).urgent(true));
        }
    }
}