package group.aelysium.rustyconnector.plugin.paper.commands;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.LongArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.paper.PaperCommandManager;
import group.aelysium.rustyconnector.core.lib.data_messaging.cache.CacheableMessage;
import group.aelysium.rustyconnector.core.lib.data_messaging.cache.MessageCache;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.lib.lang_messaging.PaperLang;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public final class CommandRusty {
    public static void create(PaperCommandManager<CommandSender> manager) {
        PaperRustyConnector plugin = PaperRustyConnector.getInstance();
        final Command.Builder<CommandSender> builder = manager.commandBuilder("rc","rc", "rusty","rustyconnector");

        manager.command(builder.literal("message")
                .senderType(ConsoleCommandSender.class)
                .argument(LongArgument.of("snowflake"), ArgumentDescription.of("Message ID"))
                .handler(context -> manager.taskRecipe().begin(context)
                    .asynchronous(commandContext -> {
                        try {
                            final Long snowflake = commandContext.get("snowflake");

                            MessageCache messageCache = PaperRustyConnector.getInstance().getVirtualServer().getMessageCache();

                            CacheableMessage message = messageCache.getMessage(snowflake);

                            PaperLang.RC_MESSAGE_GET_MESSAGE.send(plugin.logger(), message.getSnowflake(), message.getDate(), message.getContents());
                        } catch (NullPointerException e) {
                            plugin.logger().log("That message either doesn't exist or is no-longer available in the cache!");
                        } catch (Exception e) {
                            plugin.logger().log("An error stopped us from getting that message!", e);
                        }
                    }).execute())
        ).command(builder.literal("send")
                .senderType(ConsoleCommandSender.class)
                .argument(PlayerArgument.of("player"), ArgumentDescription.of("Player"))
                .argument(StringArgument.of("family-name"), ArgumentDescription.of("Family Name"))
                .handler(context -> manager.taskRecipe().begin(context)
                        .synchronous(commandContext -> {
                            try {
                                final Player player = commandContext.get("player");
                                final String familyName = commandContext.get("family-name");

                                plugin.getVirtualServer().sendToOtherFamily(player,familyName);
                            } catch (NullPointerException e) {
                                PaperLang.RC_SEND_USAGE.send(plugin.logger());
                            } catch (Exception e) {
                                plugin.logger().log("An error stopped us from processing the request!", e);
                            }
                        }).execute())
        ).command(builder.literal("register")
                .senderType(ConsoleCommandSender.class)
                .handler(context -> manager.taskRecipe().begin(context)
                        .synchronous(commandContext -> {
                            try {
                                plugin.getVirtualServer().registerToProxy();
                            } catch (Exception e) {
                                plugin.logger().log("An error stopped us from sending your request!", e);
                            }
                        }).execute())
        ).command(builder.literal("unregister")
                .senderType(ConsoleCommandSender.class)
                .handler(context -> manager.taskRecipe().begin(context)
                        .synchronous(commandContext -> {
                            try {
                                plugin.getVirtualServer().unregisterFromProxy();
                            } catch (Exception e) {
                                plugin.logger().log("An error stopped us from sending your request!", e);
                            }
                        }).execute())
        ).command(builder.literal("sendmessage")
                .senderType(ConsoleCommandSender.class)
                .argument(StringArgument.of("message"), ArgumentDescription.of("A message"))
                .handler(context -> manager.taskRecipe().begin(context)
                        .asynchronous(commandContext -> {
                            try {
                                final String message = commandContext.get("message");

                                plugin.getVirtualServer().sendMessage(message);
                            } catch (Exception e) {
                                plugin.logger().log("There was an error sending that!");
                            }
                        }).execute())
        );
    }
}