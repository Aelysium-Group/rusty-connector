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
    @Command("rc send <username> <target>")
    private  void sertgsdbfdfxxviz(Client<?> client, @Argument("username") String username, @Argument("target") String target) {
        try {
            boolean isServer = false;
            try {
                UUID.fromString(target);
                isServer = true;
            } catch (Exception ignore) {}

            if(isServer) {
                UUID uuid = RC.S.Adapter().playerUUID(username)
                        .orElseThrow(()->new NoSuchElementException("Unable to get the uuid for the username ["+username+"]."));

                UUID server = UUID.fromString(target);

                MagicLinkCore.Packets.Response packet = RC.S.Kernel().send(uuid, server).get(15, TimeUnit.SECONDS);

                client.send(Component.text(packet.message(), packet.successful() ? GREEN : RED));
                return;
            }

            UUID uuid = RC.S.Adapter().playerUUID(username)
                    .orElseThrow(()->new NoSuchElementException("Unable to get the uuid for the username ["+username+"]."));

            MagicLinkCore.Packets.Response packet = RC.S.Kernel().send(uuid, target).get(15, TimeUnit.SECONDS);

            client.send(Component.text(packet.message(), packet.successful() ? GREEN : RED));
        } catch (TimeoutException e) {
            client.send(Error.from("The server took to long attempting to lock itself.").urgent(true));
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