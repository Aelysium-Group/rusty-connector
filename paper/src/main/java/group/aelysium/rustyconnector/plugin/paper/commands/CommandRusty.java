package group.aelysium.rustyconnector.plugin.paper.commands;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.LongArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.paper.PaperCommandManager;
import group.aelysium.rustyconnector.core.lib.generic.Lang;
import group.aelysium.rustyconnector.core.lib.generic.MessageCache;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public final class CommandRusty {
    public static void create(PaperCommandManager<CommandSender> manager) {
        PaperRustyConnector plugin = PaperRustyConnector.getInstance();
        final Command.Builder<CommandSender> builder = manager.commandBuilder("rc","rusty","rustyconnector");

        manager.command(
                builder.literal("retrieveMessage")
                    .senderType(ConsoleCommandSender.class)
                    .argument(LongArgument.of("snowflake"), ArgumentDescription.of("Message ID"))
                    .handler(context -> manager.taskRecipe().begin(context)
                        .synchronous(commandContext -> {
                            try {
                                final Long snowflake = commandContext.get("snowflake");

                                MessageCache messageCache = PaperRustyConnector.getInstance().getMessageCache();

                                String message = messageCache.getMessage(snowflake);

                                Lang.print(plugin.logger(),
                                        Lang.get("boxed-message",
                                                "Found message with ID "+snowflake.toString(),
                                                Lang.spacing(),
                                                message
                                        )
                                );
                            } catch (NullPointerException e) {
                                plugin.logger().log("That message either doesn't exist or is no-longer available in the cache!");
                            } catch (Exception e) {
                                plugin.logger().log("An error stopped us from getting that message!", e);
                            }
                        }).execute()));
    }
}