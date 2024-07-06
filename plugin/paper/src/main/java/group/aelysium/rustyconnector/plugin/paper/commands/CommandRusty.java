package group.aelysium.rustyconnector.plugin.paper.commands;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.LongArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import group.aelysium.rustyconnector.toolkit.common.cache.CacheableMessage;
import group.aelysium.rustyconnector.toolkit.common.cache.MessageCache;
import group.aelysium.rustyconnector.common.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.central.Tinder;
import group.aelysium.rustyconnector.toolkit.mc_loader.lang.MCLoaderLang;
import group.aelysium.rustyconnector.toolkit.common.logger.IPluginLogger;
import group.aelysium.rustyconnector.common.packets.SendPlayerPacket;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

public final class CommandRusty {
    public static void create(PaperCommandManager<CommandSender> manager) {
        manager.command(messageList(manager));
        manager.command(messageListPage(manager));
        manager.command(messageGet(manager));
        manager.command(send(manager));
        manager.command(unlock(manager));
        manager.command(lock(manager));
        manager.command(uuid(manager));
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
                            MessageCache cache = TinderAdapterForCore.getTinder().services().messageCache();
                            try {
                                final Long snowflake = commandContext.get("snowflake");

                                CacheableMessage message = cache.findMessage(snowflake);

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
                            MessageCache cache = TinderAdapterForCore.getTinder().services().messageCache();
                            try {
                                if(cache.size() > 10) {
                                    int numberOfPages = Math.floorDiv(cache.size(),10) + 1;

                                    List<CacheableMessage> messagesPage = cache.fetchMessagesPage(1);

                                    MCLoaderLang.RC_MESSAGE_PAGE.send(logger,messagesPage,1,numberOfPages);

                                    return;
                                }
                                List<CacheableMessage> messages = cache.messages();

                                MCLoaderLang.RC_MESSAGE_PAGE.send(logger,messages,1,1);

                            } catch (Exception e) {
                                logger.send(Component.text("There was an issue getting those messages!\n"+e.getMessage(), NamedTextColor.RED));
                            }
                        }).execute());
    }

    private static Command.Builder<CommandSender> messageListPage(PaperCommandManager<CommandSender> manager) {
        Tinder api = Tinder.get();
        IPluginLogger logger = api.logger();
        final Command.Builder<CommandSender> builder = api.commandManager().commandBuilder("rc", "/rc");

        return builder.literal("message")
                .senderType(ConsoleCommandSender.class)
                .argument(StaticArgument.of("list"))
                .argument(IntegerArgument.of("page"), ArgumentDescription.of("The page number to fetch."))
                .handler(context -> manager.taskRecipe().begin(context)
                        .asynchronous(commandContext -> {
                            MessageCache cache = TinderAdapterForCore.getTinder().services().messageCache();
                            try {
                                final Integer page = context.get("page");

                                List<CacheableMessage> messages = cache.fetchMessagesPage(page);

                                int numberOfPages = Math.floorDiv(cache.size(),10) + 1;

                                MCLoaderLang.RC_MESSAGE_PAGE.send(logger,messages,page,numberOfPages);
                            } catch (Exception e) {
                                logger.send(Component.text("There was an issue getting those messages!\n"+e.getMessage(), NamedTextColor.RED));
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

                                Packet message = api.services().packetBuilder().newBuilder()
                                        .identification(BuiltInIdentifications.SEND_PLAYER)
                                        .sendingToProxy()
                                        .parameter(SendPlayerPacket.Parameters.TARGET_FAMILY_NAME, familyName)
                                        .parameter(SendPlayerPacket.Parameters.PLAYER_UUID, player.getUniqueId().toString())
                                        .build();

                                api.services().magicLink().connection().orElseThrow().publish(message);
                            } catch (NullPointerException e) {
                                MCLoaderLang.RC_SEND_USAGE.send(logger);
                            } catch (Exception e) {
                                logger.log("An error stopped us from processing the request!", e);
                            }
                        }).execute());
    }

    private static Command.Builder<CommandSender> uuid(PaperCommandManager<CommandSender> manager) {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();
        final Command.Builder<CommandSender> builder = api.commandManager().commandBuilder("rc", "/rc");

        return builder.literal("uuid")
                .senderType(ConsoleCommandSender.class)
                .handler(context -> manager.taskRecipe().begin(context)
                        .asynchronous(commandContext -> {
                            try {
                                logger.log("This MCLoader's UUID is: "+api.services().serverInfo().uuid());
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
                        api.flame().unlock();
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
                                api.flame().lock();
                                logger.log("Locking server.");
                            } catch (NullPointerException e) {
                                MCLoaderLang.RC_SEND_USAGE.send(logger);
                            } catch (Exception e) {
                                logger.log("An error stopped us from processing the request!", e);
                            }
                        }).execute());
    }
}