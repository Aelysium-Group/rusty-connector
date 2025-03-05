package group.aelysium.rustyconnector.plugin.velocity.commands;

import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.plugin.common.command.Client;
import group.aelysium.rustyconnector.proxy.player.Player;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.proxy.family.Family;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

public class CommandServer {
    @Command("server")
    public void esdfdfitotgxtbmf(Client.Player<?> player) {
        if(player == null) {
            RC.Adapter().log(Error.from("/server can only be used by players!").toComponent());
            return;
        }
        player.send(RC.Lang("velocity-serverUsage").generate((Object) null));
    }

    @Command("server <family_name>")
    public void esdfdfitotgxtbmf(Client.Player<?> player, @Argument(value = "family_name") String family_name) {
        if(player == null) {
            RC.Adapter().log(Error.from("/server can only be used by players!").toComponent());
            return;
        }
        try {
            Player rcPlayer = RC.P.Adapter().convertToRCPlayer(player);

            Family family = RC.P.Family(family_name).orElseThrow();

            Player.Connection.Request request = family.connect(rcPlayer);
            Player.Connection.Result result = request.result().get(30, TimeUnit.SECONDS);

            if (result.connected()) return;

            player.send(result.message());
        } catch (NoSuchElementException e) {
            player.send(RC.Lang("rustyconnector-missing2").generate("family", family_name));
        } catch (Exception e) {
            player.send(Error.from(e).toComponent());
        }
    }
}
