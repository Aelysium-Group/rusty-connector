package group.aelysium.rustyconnector.plugin.paper.commands;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.arguments.standard.LongArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.paper.PaperCommandManager;
import group.aelysium.rustyconnector.core.lib.cache.CacheableMessage;
import group.aelysium.rustyconnector.core.lib.cache.MessageCacheService;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.central.Tinder;
import group.aelysium.rustyconnector.core.mcloader.lib.lang.MCLoaderLang;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class CommandRusty {
    public static void create(PaperCommandManager<CommandSender> manager) {
        manager.command(messageList(manager));
        manager.command(messageGet(manager));
        manager.command(send(manager));
        manager.command(unlock(manager));
        manager.command(lock(manager));
        manager.command(game(manager));
    }

    private static Command.Builder<CommandSender> messageGet(PaperCommandManager<CommandSender> manager) {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();
        final Command.Builder<CommandSender> builder = api.commandManager().commandBuilder("rc", "/rc");

        return builder.literal("message")
                .senderType(ConsoleCommandSender.class)
                .argument(StaticArgument.of("get"))
                .argument(LongArgument.of("snowflake"), ArgumentDescription.of("Message ID"))
                .handler(context -> manager.taskRecipe().begin(context)
                        .asynchronous(commandContext -> {
                            try {
                                final Long snowflake = commandContext.get("snowflake");

                                MessageCacheService messageCacheService = api.services().messageCache();

                                CacheableMessage message = messageCacheService.findMessage(snowflake);

                                MCLoaderLang.RC_MESSAGE_GET_MESSAGE.send(logger, message.getSnowflake(), message.getDate(), message.getContents());
                            } catch (NullPointerException e) {
                                logger.log("That message either doesn't exist or is no-longer available in the cache!");
                            } catch (Exception e) {
                                logger.log("An error stopped us from getting that message!", e);
                            }
                        }).execute());
    }

    private static Command.Builder<CommandSender> messageList(PaperCommandManager<CommandSender> manager) {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();
        final Command.Builder<CommandSender> builder = api.commandManager().commandBuilder("rc", "/rc");

        return builder.literal("message")
                .senderType(ConsoleCommandSender.class)
                .argument(StaticArgument.of("list"))
                .handler(context -> manager.taskRecipe().begin(context)
                        .asynchronous(commandContext -> {
                            try {
                                MessageCacheService messageCacheService = api.services().messageCache();
                                try {
                                    if(messageCacheService.size() > 10) {
                                        int numberOfPages = Math.floorDiv(messageCacheService.size(),10) + 1;

                                        List<CacheableMessage> messagesPage = messageCacheService.fetchMessagesPage(1);

                                        MCLoaderLang.RC_MESSAGE_PAGE.send(logger,messagesPage,1,numberOfPages);

                                        return;
                                    }

                                    List<CacheableMessage> messages = messageCacheService.messages();

                                    MCLoaderLang.RC_MESSAGE_PAGE.send(logger,messages,1,1);

                                } catch (Exception e) {
                                    logger.log("There was an issue getting those messages!\n"+e.getMessage());
                                }
                            } catch (NullPointerException e) {
                                logger.log("That message either doesn't exist or is no-longer available in the cache!");
                            } catch (Exception e) {
                                logger.log("An error stopped us from getting that message!", e);
                            }
                        }).execute());
    }

    private static Command.Builder<CommandSender> send(PaperCommandManager<CommandSender> manager) {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();
        final Command.Builder<CommandSender> builder = api.commandManager().commandBuilder("rc", "/rc");

        return builder.literal("send")
                .senderType(ConsoleCommandSender.class)
                .argument(PlayerArgument.of("player"), ArgumentDescription.of("Player"))
                .argument(StringArgument.of("family-name"), ArgumentDescription.of("Family Name"))
                .handler(context -> manager.taskRecipe().begin(context)
                        .asynchronous(commandContext -> {
                            try {
                                final Player player = commandContext.get("player");
                                final String familyName = commandContext.get("family-name");

                                api.services().packetBuilder().sendToOtherFamily(player.getUniqueId(), familyName);
                            } catch (NullPointerException e) {
                                MCLoaderLang.RC_SEND_USAGE.send(logger);
                            } catch (Exception e) {
                                logger.log("An error stopped us from processing the request!", e);
                            }
                        }).execute());
    }

    private static Command.Builder<CommandSender> unlock(PaperCommandManager<CommandSender> manager) {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();

        final Command.Builder<CommandSender> builder = api.commandManager().commandBuilder("rc", "/rc");

        return builder.literal("unlock")
                .senderType(ConsoleCommandSender.class)
                .handler(context -> manager.taskRecipe().begin(context)
                        .asynchronous(commandContext -> {
                    try {
                        api.services().packetBuilder().unlockServer();
                        logger.log("Unlocking server.");
                    } catch (NullPointerException e) {
                        MCLoaderLang.RC_SEND_USAGE.send(logger);
                    } catch (Exception e) {
                        logger.log("An error stopped us from processing the request!", e);
                    }
                }).execute());
    }

    private static Command.Builder<CommandSender> lock(PaperCommandManager<CommandSender> manager) {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();

        final Command.Builder<CommandSender> builder = api.commandManager().commandBuilder("rc", "/rc");

        return builder.literal("lock")
                .senderType(ConsoleCommandSender.class)
                .handler(context -> manager.taskRecipe().begin(context)
                        .asynchronous(commandContext -> {
                            try {
                                api.services().packetBuilder().lockServer();
                                logger.log("Locking server.");
                            } catch (NullPointerException e) {
                                MCLoaderLang.RC_SEND_USAGE.send(logger);
                            } catch (Exception e) {
                                logger.log("An error stopped us from processing the request!", e);
                            }
                        }).execute());
    }

    private static Command.Builder<CommandSender> game(PaperCommandManager<CommandSender> manager) {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();

        final Command.Builder<CommandSender> builder = api.commandManager().commandBuilder("rc", "/rc");

        return builder.literal("game")
                .senderType(ConsoleCommandSender.class)
                .argument(StaticArgument.of("end"))
                .handler(context -> manager.taskRecipe().begin(context)
                        .asynchronous(commandContext -> {
                            try {
                                api.services().rankedGameInterface().endGame();
                            } catch (Exception e) {
                                logger.log("An error stopped us from processing that request!", e);
                            }
                        }).execute());
    }
}