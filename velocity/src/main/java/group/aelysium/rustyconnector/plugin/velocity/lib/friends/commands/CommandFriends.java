package group.aelysium.rustyconnector.plugin.velocity.lib.friends.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.Permission;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendRequest;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

public final class CommandFriends {
    public static BrigadierCommand create() {
        VelocityAPI api = VelocityAPI.get();
        PluginLogger logger = api.logger();

        FriendsService friendsService = api.services().friendsService().orElse(null);
        if (friendsService == null) {
            logger.send(Component.text("The Friends service must be enabled to load the /friends command.", NamedTextColor.YELLOW));
            return null;
        }

        LiteralCommandNode<CommandSource> friends = LiteralArgumentBuilder
                .<CommandSource>literal("friends")
                .requires(source -> source instanceof Player)
                .executes(context -> {
                    if(!(context.getSource() instanceof Player player)) {
                        logger.log("/friends must be sent as a player!");
                        return Command.SINGLE_SUCCESS;
                    }

                    if(!Permission.validate(player, "rustyconnector.command.friends")) {
                        player.sendMessage(VelocityLang.COMMAND_NO_PERMISSION);
                        return Command.SINGLE_SUCCESS;
                    }

                    return closeMessage(player, VelocityLang.FRIENDS_BOARD.build(player));
                })
                .then(LiteralArgumentBuilder.<CommandSource>literal("add")
                    .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                            .suggests((context, builder) -> {
                                if(!(context.getSource() instanceof Player player)) return builder.buildFuture();

                                try {
                                    RegisteredServer server = player.getCurrentServer().orElseThrow().getServer();

                                    server.getPlayersConnected().forEach(localPlayer -> {
                                        if(localPlayer.equals(player)) return;

                                        builder.suggest(localPlayer.getUsername());
                                    });

                                    return builder.buildFuture();
                                } catch (Exception ignored) {}

                                return builder.buildFuture();
                            })
                            .executes(context -> {
                                if(!(context.getSource() instanceof Player player)) {
                                    logger.log("/friends must be sent as a player!");
                                    return Command.SINGLE_SUCCESS;
                                }

                                if(!Permission.validate(player, "rustyconnector.command.friends")) {
                                    player.sendMessage(VelocityLang.COMMAND_NO_PERMISSION);
                                    return Command.SINGLE_SUCCESS;
                                }

                                String username = context.getArgument("username", String.class);
                                Player targetPlayer = api.velocityServer().getPlayer(username).orElse(null);

                                if(friendsService.areFriends(player, targetPlayer))
                                    return closeMessage(player, Component.text(username + " is already your friend!", NamedTextColor.RED));

                                if(targetPlayer == null)
                                    return closeMessage(player, Component.text(username + " isn't available to send requests to!", NamedTextColor.RED));

                                friendsService.sendRequest(player, targetPlayer);

                                return Command.SINGLE_SUCCESS;
                            })
                    )
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("requests")
                        .executes(context -> {
                            if(!(context.getSource() instanceof Player player)) {
                                logger.log("/friends must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            if(!Permission.validate(player, "rustyconnector.command.friends")) {
                                player.sendMessage(VelocityLang.COMMAND_NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }

                            context.getSource().sendMessage(VelocityLang.FRIEND_REQUEST_USAGE.build());
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    if(!(context.getSource() instanceof Player player)) return builder.buildFuture();

                                    try {
                                        List<FriendRequest> requests = friendsService.findRequestsToTarget(player);

                                        if(requests.size() == 0) {
                                            builder.suggest("You have no pending friend requests!");
                                            return builder.buildFuture();
                                        }

                                        requests.forEach(invite -> {
                                            builder.suggest(invite.sender().getUsername());
                                        });

                                        return builder.buildFuture();
                                    } catch (Exception ignored) {}

                                    builder.suggest("Searching for friend requests...");
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    if(!(context.getSource() instanceof Player player)) {
                                        logger.log("/friends must be sent as a player!");
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    if(!Permission.validate(player, "rustyconnector.command.friends")) {
                                        player.sendMessage(VelocityLang.COMMAND_NO_PERMISSION);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    context.getSource().sendMessage(VelocityLang.FRIEND_REQUEST_USAGE.build());
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(LiteralArgumentBuilder.<CommandSource>literal("ignore")
                                        .executes(context -> {
                                            if(!(context.getSource() instanceof Player player)) {
                                                logger.log("/friends must be sent as a player!");
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            if(!Permission.validate(player, "rustyconnector.command.friends")) {
                                                player.sendMessage(VelocityLang.COMMAND_NO_PERMISSION);
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            String username = context.getArgument("username", String.class);
                                            Player senderPlayer = api.velocityServer().getPlayer(username).orElse(null);
                                            if(senderPlayer == null)
                                                return closeMessage(player, Component.text(username + " doesn't seem to exist on this server! (How did this happen?)", NamedTextColor.RED));

                                            try {
                                                FriendRequest invite = friendsService.findRequest(player, senderPlayer).orElse(null);
                                                if(invite == null) throw new NoOutputException();

                                                try {
                                                    invite.ignore();
                                                } catch (Exception ignore) {
                                                    friendsService.closeInvite(invite);
                                                }

                                                return closeMessage(player, Component.text("Ignored the friend request from "+username, NamedTextColor.GREEN));
                                            } catch (Exception ignore) {}

                                            return closeMessage(player, Component.text("There was an issue ignoring the friend request from "+username, NamedTextColor.RED));
                                        })
                                )
                                .then(LiteralArgumentBuilder.<CommandSource>literal("accept")
                                        .executes(context -> {
                                            if(!(context.getSource() instanceof Player player)) {
                                                logger.log("/friends must be sent as a player!");
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            if(!Permission.validate(player, "rustyconnector.command.friends")) {
                                                player.sendMessage(VelocityLang.COMMAND_NO_PERMISSION);
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            String username = context.getArgument("username", String.class);
                                            Player senderPlayer = api.velocityServer().getPlayer(username).orElse(null);
                                            if(senderPlayer == null)
                                                return closeMessage(player, Component.text(username + " doesn't seem to exist on this server! (How did this happen?)", NamedTextColor.RED));

                                            FriendRequest invite = friendsService.findRequest(player, senderPlayer).orElse(null);
                                            if(invite == null)
                                                return closeMessage(player, Component.text("The friend request from " + senderPlayer.getUsername() + " has expired!", NamedTextColor.RED));

                                            try {
                                                invite.accept();
                                            } catch (IllegalStateException e) {
                                                return closeMessage(player, Component.text(e.getMessage(), NamedTextColor.RED));
                                            } catch (Exception ignore) {
                                                return closeMessage(player, Component.text("There was an issue accepting that friend request!", NamedTextColor.RED));
                                            }

                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
                .build();

        // BrigadierCommand implements Command
        return new BrigadierCommand(friends);
    }

    public static int closeMessage(Player player, Component message) {
        player.sendMessage(message);
        return Command.SINGLE_SUCCESS;
    }
}