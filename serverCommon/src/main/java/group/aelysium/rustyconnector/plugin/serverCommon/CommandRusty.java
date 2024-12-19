package group.aelysium.rustyconnector.plugin.serverCommon;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.magic_link.MagicLinkCore;
import group.aelysium.rustyconnector.plugin.common.command.Client;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

@Command("rc")
@Permission("rustyconnector.commands.rc")
public final class CommandRusty {
    @Command("send <playerTarget> <target>")
    private  void sertgsdbfdfxxviz(Client.Console<?> client, String playerTarget, String target) {
        try {
            MagicLinkCore.Packets.Response packet = RC.S.Kernel().send(playerTarget, target, "").get(15, TimeUnit.SECONDS);

            client.send(text(packet.message(), packet.successful() ? GREEN : RED));
        } catch (TimeoutException e) {
            client.send(text("The send request took to long to response.", BLUE));
        } catch (Exception e) {
            RC.Error(Error.from(e).whileAttempting("To send a player to a server or family.").urgent(true));
        }
    }
    @Command("send <playerTarget> <target> <flags>")
    private  void sdfvqewrsczzsrff(
            Client.Console<?> client,
            String playerTarget,
            String target,
            @Greedy String flags
    ) {
        System.out.println(flags);
        String flagsArray = flags.replaceAll("\\s*-+([a-zA-Z])\\s*", "$1");
        System.out.println(flagsArray);
        try {
            MagicLinkCore.Packets.Response packet = RC.S.Kernel().send(playerTarget, target, flagsArray).get(15, TimeUnit.SECONDS);

            client.send(text(packet.message(), packet.successful() ? GREEN : RED));
        } catch (TimeoutException e) {
            client.send(text("The send request took to long to response.", BLUE));
        } catch (Exception e) {
            RC.Error(Error.from(e).whileAttempting("To send a player to a family.").urgent(true));
        }
    }

    @Command("lock")
    private void zxcvssdbfdfxxviz(Client.Console<?> client) {
        try {
            client.send(RC.Lang("rustyconnector-waiting").generate());
            MagicLinkCore.Packets.Response packet = RC.S.Kernel().lock().get(7, TimeUnit.SECONDS);
            client.send(text(packet.message(), packet.successful() ? GREEN : RED));
        } catch (TimeoutException e) {
            client.send(text("The server took to long attempting to lock itself.", BLUE));
        } catch (Exception e) {
            RC.Error(Error.from(e).whileAttempting("To lock the server.").urgent(true));
        }
    }

    @Command("unlock")
    private void sertgssdfvsscefd(Client.Console<?> client) {
        try {
            client.send(RC.Lang("rustyconnector-waiting").generate());
            MagicLinkCore.Packets.Response packet = RC.S.Kernel().unlock().get(7, TimeUnit.SECONDS);
            client.send(text(packet.message(), packet.successful() ? GREEN : RED));
        } catch (TimeoutException e) {
            client.send(text("The server took to long attempting to unlock itself.", BLUE));
        } catch (Exception e) {
            RC.Error(Error.from(e).whileAttempting("To unlock the server.").urgent(true));
        }
    }
}