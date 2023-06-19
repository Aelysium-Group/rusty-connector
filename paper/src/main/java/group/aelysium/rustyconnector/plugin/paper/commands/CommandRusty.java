package group.aelysium.rustyconnector.plugin.paper.commands;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.LongArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.paper.PaperCommandManager;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.cache.CacheableMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.cache.MessageCache;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.lib.lang_messaging.PaperLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public final class CommandRusty {
    public static void create(PaperCommandManager<CommandSender> manager) {
        PaperAPI api = PaperRustyConnector.getAPI();

        manager.command(message(manager));
        manager.command(send(manager));
        manager.command(register(manager));
        manager.command(unregister(manager));
    }

    private static Command.Builder<CommandSender> message(PaperCommandManager<CommandSender> manager) {
        PaperAPI api = PaperRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();
        final Command.Builder<CommandSender> builder = api.getCommandManager().commandBuilder("rc", "/rc");

        return builder.literal("message")
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
                        }).execute());
    }

    private static Command.Builder<CommandSender> send(PaperCommandManager<CommandSender> manager) {
        PaperAPI api = PaperRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();
        final Command.Builder<CommandSender> builder = api.getCommandManager().commandBuilder("rc", "/rc");

        return builder.literal("send")
                .senderType(ConsoleCommandSender.class)
                .argument(PlayerArgument.of("player"), ArgumentDescription.of("Player"))
                .argument(StringArgument.of("family-name"), ArgumentDescription.of("Family Name"))
                .handler(context -> manager.taskRecipe().begin(context)
                        .asynchronous(commandContext -> {
                            try {
                                final Player player = commandContext.get("player");
                                final String familyName = commandContext.get("family-name");

                                api.getVirtualProcessor().sendToOtherFamily(player,familyName);
                            } catch (NullPointerException e) {
                                PaperLang.RC_SEND_USAGE.send(logger);
                            } catch (Exception e) {
                                logger.log("An error stopped us from processing the request!", e);
                            }
                        }).execute());
    }

    private static Command.Builder<CommandSender> register(PaperCommandManager<CommandSender> manager) {
        PaperAPI api = PaperRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();
        final Command.Builder<CommandSender> builder = api.getCommandManager().commandBuilder("rc", "/rc");

        return builder.literal("register")
                .senderType(ConsoleCommandSender.class)
                .handler(context -> manager.taskRecipe().begin(context)
                        .asynchronous(commandContext -> {
                            try {
                                api.getVirtualProcessor().registerToProxy();
                                Lang.BOXED_MESSAGE_COLORED.send(PaperRustyConnector.getAPI().getLogger(), Component.text("Send registration request!"), NamedTextColor.GREEN);
                            } catch (Exception e) {
                                logger.log("An error stopped us from sending your request!", e);
                            }
                        }).execute());
    }

    private static Command.Builder<CommandSender> unregister(PaperCommandManager<CommandSender> manager) {
        PaperAPI api = PaperRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();
        final Command.Builder<CommandSender> builder = api.getCommandManager().commandBuilder("rc", "/rc");

        return builder.literal("unregister")
                .senderType(ConsoleCommandSender.class)
                .handler(context -> manager.taskRecipe().begin(context)
                        .asynchronous(commandContext -> {
                            try {
                                api.getVirtualProcessor().unregisterFromProxy();
                                Lang.BOXED_MESSAGE_COLORED.send(PaperRustyConnector.getAPI().getLogger(), Component.text("Send unregister request!"), NamedTextColor.GREEN);
                            } catch (Exception e) {
                                logger.log("An error stopped us from sending your request!", e);
                            }
                        }).execute());
    }
}