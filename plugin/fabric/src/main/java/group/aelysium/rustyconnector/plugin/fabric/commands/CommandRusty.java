package group.aelysium.rustyconnector.plugin.fabric.commands;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.arguments.standard.LongArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.fabric.FabricServerCommandManager;
import group.aelysium.rustyconnector.toolkit.core.logger.PluginLogger;
import group.aelysium.rustyconnector.core.lib.cache.CacheableMessage;
import group.aelysium.rustyconnector.core.lib.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.mcloader.lib.lang.PluginLang;
import group.aelysium.rustyconnector.plugin.fabric.central.Tinder;
import net.minecraft.command.CommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.UUID;

public final class CommandRusty {
    public static void create(FabricServerCommandManager<CommandSource> manager) {
        manager.command(messageList());
        manager.command(messageGet());
        manager.command(send());
        manager.command(unlock());
        manager.command(lock());
    }

    private static void checkForConsole(@NonNull CommandContext<CommandSource> context) {
        Tinder api = Tinder.get();
        if(!context.getSender().equals(api.fabricServer().getCommandSource())) return;

        throw new RuntimeException("This command must be run from the console!");
    }

    private static Command.Builder<CommandSource> messageGet() {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();
        final Command.Builder<CommandSource> builder = api.commandManager().commandBuilder("rc", "/rc");

        return builder.literal("message")
                .argument(StaticArgument.of("get"))
                .argument(LongArgument.of("snowflake"), ArgumentDescription.of("Message ID"))
                .handler(context -> {
                    checkForConsole(context);
                    try {
                        final Long snowflake = context.get("snowflake");

                        MessageCacheService messageCacheService = api.services().messageCache();

                        CacheableMessage message = messageCacheService.findMessage(snowflake);

                        PluginLang.RC_MESSAGE_GET_MESSAGE.send(logger, message.getSnowflake(), message.getDate(), message.getContents());
                    } catch (NullPointerException e) {
                        logger.log("That message either doesn't exist or is no-longer available in the cache!");
                    } catch (Exception e) {
                        logger.log("An error stopped us from getting that message!", e);
                    }
                });
    }

    private static Command.Builder<CommandSource> messageList() {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();
        final Command.Builder<CommandSource> builder = api.commandManager().commandBuilder("rc", "/rc");

        return builder.literal("message")
                .argument(StaticArgument.of("list"))
                .handler(context -> {
                    checkForConsole(context);
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
                });
    }

    private static Command.Builder<CommandSource> send() {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();
        final Command.Builder<CommandSource> builder = api.commandManager().commandBuilder("rc", "/rc");

        return builder.literal("send")
                .argument(StringArgument.of("username"), ArgumentDescription.of("Username"))
                .argument(StringArgument.of("family-name"), ArgumentDescription.of("Family Name"))
                .handler(context -> {
                    checkForConsole(context);
                    try {
                        final String username = context.get("username");
                        final String familyName = context.get("family-name");

                        UUID playerUUID = Tinder.get().getPlayerUUID(username);

                        api.services().packetBuilder().sendToOtherFamily(playerUUID, familyName);
                    } catch (NullPointerException e) {
                        PluginLang.RC_SEND_USAGE.send(logger);
                    } catch (Exception e) {
                        logger.log("An error stopped us from processing the request!", e);
                    }
                });
    }

    private static Command.Builder<CommandSource> unlock() {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();

        final Command.Builder<CommandSource> builder = api.commandManager().commandBuilder("rc", "/rc");

        return builder.literal("unlock")
                .handler(context -> {
                    try {
                        api.services().packetBuilder().unlockServer();
                        logger.log("Unlocking server.");
                    } catch (NullPointerException e) {
                        PluginLang.RC_SEND_USAGE.send(logger);
                    } catch (Exception e) {
                        logger.log("An error stopped us from processing the request!", e);
                    }
                });
    }

    private static Command.Builder<CommandSource> lock() {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();

        final Command.Builder<CommandSource> builder = api.commandManager().commandBuilder("rc", "/rc");

        return builder.literal("lock")
                .handler(context -> {
                    try {
                        api.services().packetBuilder().lockServer();
                        logger.log("Locking server.");
                    } catch (NullPointerException e) {
                        PluginLang.RC_SEND_USAGE.send(logger);
                    } catch (Exception e) {
                        logger.log("An error stopped us from processing the request!", e);
                    }
                });
    }
}