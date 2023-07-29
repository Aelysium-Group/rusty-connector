package group.aelysium.rustyconnector.plugin.velocity.lib.friends.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.Permission;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.FakePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

public final class CommandUnFriend {
    public static BrigadierCommand create() {
        VelocityAPI api = VelocityAPI.get();
        PluginLogger logger = api.logger();

        FriendsService friendsService = api.services().friendsService().orElse(null);
        if (friendsService == null) {
            logger.send(Component.text("The Friends service must be enabled to load the /friend command.", NamedTextColor.YELLOW));
            return null;
        }

        LiteralCommandNode<CommandSource> unfriend = LiteralArgumentBuilder
                .<CommandSource>literal("unfriend")
                .requires(source -> source instanceof Player)
                .executes(context -> {
                    if(!(context.getSource() instanceof Player player)) {
                        logger.log("/unfriend must be sent as a player!");
                        return Command.SINGLE_SUCCESS;
                    }

                    if(!Permission.validate(player, "rustyconnector.command.unfriend")) {
                        player.sendMessage(VelocityLang.COMMAND_NO_PERMISSION);
                        return Command.SINGLE_SUCCESS;
                    }

                    return closeMessage(player, VelocityLang.UNFRIEND_USAGE.build());
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            if(!(context.getSource() instanceof Player player)) return builder.buildFuture();

                            try {
                                List<FakePlayer> friends = friendsService.findFriends(player, false).orElseThrow();

                                friends.forEach(friend -> {
                                    try {
                                        builder.suggest(friend.username());
                                    } catch (Exception ignore) {}
                                });

                                return builder.buildFuture();
                            } catch (Exception ignored) {}

                            builder.suggest("Error while finding friends!");
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            if(!(context.getSource() instanceof Player player)) {
                                logger.log("/unfriend must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            if(!Permission.validate(player, "rustyconnector.command.unfriend")) {
                                player.sendMessage(VelocityLang.COMMAND_NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }

                            String username = context.getArgument("username", String.class);
                            Player targetPlayer = api.velocityServer().getPlayer(username).orElse(null);

                            if(targetPlayer == null)
                                return closeMessage(player, Component.text(username + " doesn't seem to exist!", NamedTextColor.RED));

                            try {
                                if(friendsService.removeFriend(player, targetPlayer))
                                    return closeMessage(player, Component.text("You are no longer friends with "+username, NamedTextColor.GREEN));
                            } catch (IllegalStateException e) {
                                return closeMessage(player, Component.text(e.getMessage(), NamedTextColor.RED));
                            } catch (Exception ignore) {}

                            return closeMessage(player, Component.text("There was an issue unfriending "+username, NamedTextColor.RED));
                        })
                )
                .build();

        // BrigadierCommand implements Command
        return new BrigadierCommand(unfriend);
    }

    public static int closeMessage(Player player, Component message) {
        player.sendMessage(message);
        return Command.SINGLE_SUCCESS;
    }
}