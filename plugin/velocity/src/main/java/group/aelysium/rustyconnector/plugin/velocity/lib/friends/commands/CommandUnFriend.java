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
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.Permission;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.FakePlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.PlayerDataEnclave;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.SyncFailedException;
import java.util.List;

public final class CommandUnFriend {
    public static BrigadierCommand create(FriendsService friendsService) {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();

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
                        player.sendMessage(VelocityLang.NO_PERMISSION);
                        return Command.SINGLE_SUCCESS;
                    }

                    return closeMessage(player, VelocityLang.UNFRIEND_USAGE.build());
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            if(!(context.getSource() instanceof Player player)) return builder.buildFuture();

                            try {
                                List<FakePlayer> friends = friendsService.findFriends(player).orElseThrow();

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
                                player.sendMessage(VelocityLang.NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }

                            String username = context.getArgument("username", String.class);
                            try {
                                FakePlayer targetPlayer = api.services().playerService().orElseThrow().dataEnclave().fetch(username).orElseThrow();

                                if(!friendsService.areFriends(FakePlayer.from(player), targetPlayer))
                                    return closeMessage(player, Component.text(username + " isn't your friend!", NamedTextColor.RED));

                                if(targetPlayer == null)
                                    return closeMessage(player, Component.text(username + " has never joined this network!", NamedTextColor.RED));

                                try {
                                    friendsService.removeFriends(FakePlayer.from(player), targetPlayer);

                                    return closeMessage(player, Component.text("You are no longer friends with " + username, NamedTextColor.GREEN));
                                } catch (IllegalStateException e) {
                                    return closeMessage(player, Component.text(e.getMessage(), NamedTextColor.RED));
                                } catch (Exception ignore) {}
                            } catch (SyncFailedException e) {
                                e.printStackTrace();
                                return closeMessage(player, Component.text("There was an internal error while trying to find "+username+"!", NamedTextColor.RED));
                            }

                            return closeMessage(player, Component.text("There was an issue unfriending " + username, NamedTextColor.RED));
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