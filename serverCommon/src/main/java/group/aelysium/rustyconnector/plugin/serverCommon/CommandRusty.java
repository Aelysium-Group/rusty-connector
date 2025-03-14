package group.aelysium.rustyconnector.plugin.serverCommon;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.magic_link.MagicLinkCore;
import group.aelysium.rustyconnector.plugin.common.command.Client;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.Component.*;

@Command("rc")
@Permission("rustyconnector.commands.rc")
public final class CommandRusty {
    @Command("send <playerTarget> <target>")
    private  void sertgsdbfdfxxviz(Client.Console<?> client, String playerTarget, String target) {
        try {
            boolean isUUID = false;
            try {
                UUID.fromString(playerTarget);
                isUUID = true;
            } catch (Exception ignore) {}
            
            MagicLinkCore.Packets.Response packet = isUUID ?
                RC.S.Kernel().sendID(playerTarget, target, Set.of()).get(15, TimeUnit.SECONDS)
                :
                RC.S.Kernel().sendUsername(playerTarget, target, Set.of()).get(15, TimeUnit.SECONDS);

            client.send(text(packet.message(), packet.successful() ? GREEN : RED));
        } catch (TimeoutException e) {
            client.send(text("The send request took to long to respond.", BLUE));
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
        boolean isUUID = false;
        try {
            UUID.fromString(playerTarget);
            isUUID = true;
        } catch (Exception ignore) {}
        
        Set<MagicLinkCore.Packets.SendPlayer.Flag> flagSet = Arrays.stream(
                flags.replaceAll("\\s*-+([a-zA-Z])\\s*", "$1").split("")
            )
            .map(f -> switch (f) {
                case "f" -> MagicLinkCore.Packets.SendPlayer.Flag.FAMILY;
                case "s" -> MagicLinkCore.Packets.SendPlayer.Flag.SERVER;
                case "i" -> MagicLinkCore.Packets.SendPlayer.Flag.MINIMAL;
                case "o" -> MagicLinkCore.Packets.SendPlayer.Flag.MODERATE;
                case "a" -> MagicLinkCore.Packets.SendPlayer.Flag.AGGRESSIVE;
                default -> null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        
        try {
            MagicLinkCore.Packets.Response packet = isUUID ?
                RC.S.Kernel().sendID(playerTarget, target, flagSet).get(15, TimeUnit.SECONDS)
                :
                RC.S.Kernel().sendUsername(playerTarget, target, flagSet).get(15, TimeUnit.SECONDS);

            client.send(text(packet.message(), packet.successful() ? GREEN : RED));
        } catch (TimeoutException e) {
            client.send(text("The send request took to long to respond.", BLUE));
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