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
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.lib.lang_messaging.PaperLang;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public final class CommandRusty {
    public static void create(PaperCommandManager<CommandSender> manager) {
        PaperAPI api = PaperRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();

        final Command.Builder<CommandSender> builder = api.getCommandManager().commandBuilder("rc","rc", "rusty","rustyconnector");

        manager.command(builder.literal("message")
                .senderType(ConsoleCommandSender.class)
                .argument(LongArgument.of("snowflake"), ArgumentDescription.of("Message ID"))
                .handler(context -> manager.taskRecipe().begin(context)
                    .asynchronous(commandContext -> {
                        try {
                            final Long snowflake = commandContext.get("snowflake");

                            MessageCache messageCache = api.getVirtualProcessor().getMessageCache();

                            CacheableMessage message = messageCache.getMessage(snowflake);

                            PaperLang.RC_MESSAGE_GET_MESSAGE.send(logger, message.getSnowflake(), message.getDate(), message.getContents());
                        } catch (NullPointerException e) {
                            logger.log("That message either doesn't exist or is no-longer available in the cache!");
                        } catch (Exception e) {
                            logger.log("An error stopped us from getting that message!", e);
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

                                api.getVirtualProcessor().sendToOtherFamily(player,familyName);
                            } catch (NullPointerException e) {
                                PaperLang.RC_SEND_USAGE.send(logger);
                            } catch (Exception e) {
                                logger.log("An error stopped us from processing the request!", e);
                            }
                        }).execute())
        ).command(builder.literal("register")
                .senderType(ConsoleCommandSender.class)
                .handler(context -> manager.taskRecipe().begin(context)
                        .synchronous(commandContext -> {
                            try {
                                api.getVirtualProcessor().registerToProxy();
                            } catch (Exception e) {
                                logger.log("An error stopped us from sending your request!", e);
                            }
                        }).execute())
        ).command(builder.literal("unregister")
                .senderType(ConsoleCommandSender.class)
                .handler(context -> manager.taskRecipe().begin(context)
                        .synchronous(commandContext -> {
                            try {
                                api.getVirtualProcessor().unregisterFromProxy();
                            } catch (Exception e) {
                                logger.log("An error stopped us from sending your request!", e);
                            }
                        }).execute())
        ).command(builder.literal("sendmessage")
                .senderType(ConsoleCommandSender.class)
                .argument(StringArgument.of("message"), ArgumentDescription.of("A message"))
                .handler(context -> manager.taskRecipe().begin(context)
                        .asynchronous(commandContext -> {
                            try {
                                final String message = commandContext.get("message");

                                api.getVirtualProcessor().sendMessage(message);
                            } catch (Exception e) {
                                logger.log("There was an error sending that!");
                            }
                        }).execute())
        );
    }
}