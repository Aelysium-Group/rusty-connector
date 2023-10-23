package group.aelysium.rustyconnector.plugin.fabric.commands;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.arguments.standard.LongArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.fabric.FabricServerCommandManager;
import group.aelysium.rustyconnector.api.velocity.lib.PluginLogger;
import group.aelysium.rustyconnector.core.central.Tinder;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.CacheableMessage;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.plugin.Plugin;
import group.aelysium.rustyconnector.core.plugin.lib.lang.PluginLang;
import net.minecraft.command.CommandSource;

import java.util.List;

public final class CommandRusty {
    public static void create(FabricServerCommandManager<CommandSource> manager) {
        manager.command(messageList(manager));
        manager.command(messageGet(manager));
        manager.command(send(manager));
        manager.command(unlock(manager));
        manager.command(lock(manager));
    }

    private static Command.Builder<CommandSource> messageGet(FabricServerCommandManager<CommandSource> manager) {
        Tinder api = Plugin.getAPI();
        PluginLogger logger = api.logger();
        final Command.Builder<CommandSource> builder = ((group.aelysium.rustyconnector.plugin.fabric.central.Tinder) api).commandManager().commandBuilder("rc", "/rc");

        return builder.literal("message")
                //.senderType(ConsoleCommandSender.class)
                .argument(StaticArgument.of("get"))
                .argument(LongArgument.of("snowflake"), ArgumentDescription.of("Message ID"))
                .handler(context -> manager.taskRecipe().begin(context)
                        .asynchronous(commandContext -> {
                            try {
                                final Long snowflake = commandContext.get("snowflake");

                                MessageCacheService messageCacheService = api.services().messageCache();

                                CacheableMessage message = messageCacheService.findMessage(snowflake);

                                PluginLang.RC_MESSAGE_GET_MESSAGE.send(logger, message.getSnowflake(), message.getDate(), message.getContents());
                            } catch (NullPointerException e) {
                                logger.log("That message either doesn't exist or is no-longer available in the cache!");
                            } catch (Exception e) {
                                logger.log("An error stopped us from getting that message!", e);
                            }
                        }).execute());
    }

    private static Command.Builder<CommandSource> messageList(FabricServerCommandManager<CommandSource> manager) {
        Tinder api = Plugin.getAPI();
        PluginLogger logger = api.logger();
        final Command.Builder<CommandSource> builder = ((group.aelysium.rustyconnector.plugin.fabric.central.Tinder) api).commandManager().commandBuilder("rc", "/rc");

        return builder.literal("message")
                //.senderType(ConsoleCommandSender.class)
                .argument(StaticArgument.of("list"))
                .handler(context -> manager.taskRecipe().begin(context)
                        .asynchronous(commandContext -> {
                            try {
                                MessageCacheService messageCacheService = api.services().messageCache();
                                try {
                                    if(messageCacheService.size() > 10) {
                                        int numberOfPages = Math.floorDiv(messageCacheService.size(),10) + 1;

                                        List<CacheableMessage> messagesPage = messageCacheService.fetchMessagesPage(1);

                                        PluginLang.RC_MESSAGE_PAGE.send(logger,messagesPage,1,numberOfPages);

                                        return;
                                    }

                                    List<CacheableMessage> messages = messageCacheService.messages();

                                    PluginLang.RC_MESSAGE_PAGE.send(logger,messages,1,1);

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

    private static Command.Builder<CommandSource> send(FabricServerCommandManager<CommandSource> manager) {
        Tinder api = Plugin.getAPI();
        PluginLogger logger = api.logger();
        final Command.Builder<CommandSource> builder = ((group.aelysium.rustyconnector.plugin.fabric.central.Tinder) api).commandManager().commandBuilder("rc", "/rc");

        return builder.literal("send")
                //.senderType(ConsoleCommandSender.class)
                .argument(PlayerArgument.of("player"), ArgumentDescription.of("Player"))
                .argument(StringArgument.of("family-name"), ArgumentDescription.of("Family Name"))
                .handler(context -> manager.taskRecipe().begin(context)
                        .asynchronous(commandContext -> {
                            try {
                                final Player player = commandContext.get("player");
                                final String familyName = commandContext.get("family-name");

                                api.services().packetBuilder().sendToOtherFamily(player.getUniqueId(), familyName);
                            } catch (NullPointerException e) {
                                PluginLang.RC_SEND_USAGE.send(logger);
                            } catch (Exception e) {
                                logger.log("An error stopped us from processing the request!", e);
                            }
                        }).execute());
    }

    private static Command.Builder<CommandSource> unlock(FabricServerCommandManager<CommandSource> manager) {
        Tinder api = Plugin.getAPI();
        PluginLogger logger = api.logger();

        final Command.Builder<CommandSource> builder = ((group.aelysium.rustyconnector.plugin.fabric.central.Tinder) api).commandManager().commandBuilder("rc", "/rc");

        return builder.literal("unlock")
                //.senderType(ConsoleCommandSender.class)
                .handler(context -> manager.taskRecipe().begin(context)
                        .asynchronous(commandContext -> {
                    try {
                        api.services().packetBuilder().unlockServer();
                        logger.log("Unlocking server.");
                    } catch (NullPointerException e) {
                        PluginLang.RC_SEND_USAGE.send(logger);
                    } catch (Exception e) {
                        logger.log("An error stopped us from processing the request!", e);
                    }
                }).execute());
    }

    private static Command.Builder<CommandSource> lock(FabricServerCommandManager<CommandSource> manager) {
        Tinder api = Plugin.getAPI();
        PluginLogger logger = api.logger();

        final Command.Builder<CommandSource> builder = ((group.aelysium.rustyconnector.plugin.fabric.central.Tinder) api).commandManager().commandBuilder("rc", "/rc");

        return builder.literal("lock")
                //.senderType(ConsoleCommandSender.class)
                .handler(context -> manager.taskRecipe().begin(context)
                        .asynchronous(commandContext -> {
                            try {
                                api.services().packetBuilder().lockServer();
                                logger.log("Locking server.");
                            } catch (NullPointerException e) {
                                PluginLang.RC_SEND_USAGE.send(logger);
                            } catch (Exception e) {
                                logger.log("An error stopped us from processing the request!", e);
                            }
                        }).execute());
    }
}