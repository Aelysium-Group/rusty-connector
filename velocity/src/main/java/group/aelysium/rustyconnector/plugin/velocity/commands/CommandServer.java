package group.aelysium.rustyconnector.plugin.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.proxy.player.Player;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.proxy.family.Family;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

public class CommandServer {
    private static void reply(CommandSource source, Component response) {
        source.sendMessage(response);
    }

    @Command("server")
    public void esdfdfitotgxtbmf(com.velocitypowered.api.proxy.Player player) {
        if(player == null) {
            RC.Adapter().log(Error.from("/server can only be used by players!").toComponent());
            return;
        }
        player.sendMessage(RC.Lang("velocity-serverUsage").generate());
    }

    @Command("server <family_name>")
    public void esdfdfitotgxtbmf(com.velocitypowered.api.proxy.Player player, @Argument(value = "family_name") String family_name) {
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

            reply(player, result.message());
        } catch (NoSuchElementException e) {
            reply(player, RC.Lang("rustyconnector-missing2").generate("family", family_name));
        } catch (Exception e) {
            reply(player, Error.from(e).toComponent());
        }
    }
}
