package group.aelysium.rustyconnector.plugin.paper.command;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.magic_link.MagicLinkCore;
import group.aelysium.rustyconnector.plugin.common.command.Client;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;

import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public final class CommandRusty {
    @Command("rc send <playerTarget> <target>")
    private  void sertgsdbfdfxxviz(Client<?> client, String playerTarget, String target) {
        try {
            MagicLinkCore.Packets.Response packet = RC.S.Kernel().send(playerTarget, target).get(15, TimeUnit.SECONDS);

            client.send(Component.text(packet.message(), packet.successful() ? GREEN : RED));
        } catch (TimeoutException e) {
            client.send(Error.from("The send request took to long to response.").urgent(true));
        } catch (Exception e) {
            client.send(Error.from(e).whileAttempting("To lock the server.").urgent(true));
        }
    }
    @Command("rc send <playerTarget> <target> family")
    private  void sdfvqewrsczzsrff(Client<?> client, String playerTarget, String target) {
        try {
            MagicLinkCore.Packets.Response packet = RC.S.Kernel().sendFamily(playerTarget, target).get(15, TimeUnit.SECONDS);

            client.send(Component.text(packet.message(), packet.successful() ? GREEN : RED));
        } catch (TimeoutException e) {
            client.send(Error.from("The send request took to long to respond.").urgent(true));
        } catch (Exception e) {
            client.send(Error.from(e).whileAttempting("To lock the server.").urgent(true));
        }
    }
    @Command("rc send <playerTarget> <target> server")
    private  void sdfhmzzmfiruwmog(Client<?> client, String playerTarget, String target) {
        try {
            MagicLinkCore.Packets.Response packet = RC.S.Kernel().sendServer(playerTarget, target).get(15, TimeUnit.SECONDS);

            client.send(Component.text(packet.message(), packet.successful() ? GREEN : RED));
        } catch (TimeoutException e) {
            client.send(Error.from("The send request took to long to respond.").urgent(true));
        } catch (Exception e) {
            client.send(Error.from(e).whileAttempting("To lock the server.").urgent(true));
        }
    }

    @Command("rc lock")
    private void zxcvssdbfdfxxviz(Client<?> client) {
        try {
            client.send(RC.Lang("rustyconnector-waiting").generate());
            MagicLinkCore.Packets.Response packet = RC.S.Kernel().lock().get(7, TimeUnit.SECONDS);
            client.send(Component.text(packet.message(), packet.successful() ? GREEN : RED));
        } catch (TimeoutException e) {
            client.send(Error.from("The server took to long attempting to lock itself.").urgent(true));
        } catch (Exception e) {
            client.send(Error.from(e).whileAttempting("To lock the server.").urgent(true));
        }
    }

    @Command("rc unlock")
    private void sertgssdfvsscefd(Client<?> client) {
        try {
            client.send(RC.Lang("rustyconnector-waiting").generate());
            MagicLinkCore.Packets.Response packet = RC.S.Kernel().unlock().get(7, TimeUnit.SECONDS);
            client.send(Component.text(packet.message(), packet.successful() ? GREEN : RED));
        } catch (TimeoutException e) {
            client.send(Error.from("The server took to long attempting to unlock itself.").urgent(true));
        } catch (Exception e) {
            client.send(Error.from(e).whileAttempting("To unlock the server.").urgent(true));
        }
    }
}