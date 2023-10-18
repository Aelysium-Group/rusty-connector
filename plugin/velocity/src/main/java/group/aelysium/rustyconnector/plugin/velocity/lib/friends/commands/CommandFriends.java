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
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.Permission;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendRequest;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.ResolvablePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

public final class CommandFriends {
    public static BrigadierCommand create(FriendsService friendsService) {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();

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
                        player.sendMessage(VelocityLang.NO_PERMISSION);
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
                                    player.sendMessage(VelocityLang.NO_PERMISSION);
                                    return Command.SINGLE_SUCCESS;
                                }

                                String username = context.getArgument("username", String.class);
                                ResolvablePlayer targetPlayer = api.services().playerService().fetch(username).orElseThrow();

                                if(friendsService.areFriends(ResolvablePlayer.from(player), targetPlayer))
                                    return closeMessage(player, VelocityLang.FRIEND_REQUEST_ALREADY_FRIENDS.build(username));

                                if(targetPlayer == null)
                                    return closeMessage(player, VelocityLang.NO_PLAYER.build(username));

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
                                player.sendMessage(VelocityLang.NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }

                            context.getSource().sendMessage(VelocityLang.FRIEND_REQUEST_USAGE.build());
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    if(!(context.getSource() instanceof Player player)) return builder.buildFuture();

                                    try {
                                        List<FriendRequest> requests = friendsService.findRequestsToTarget(ResolvablePlayer.from(player));

                                        if(requests.size() == 0) {
                                            builder.suggest("You have no pending friend requests!");
                                            return builder.buildFuture();
                                        }

                                        requests.forEach(invite -> {
                                            builder.suggest(invite.sender().username());
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
                                        player.sendMessage(VelocityLang.NO_PERMISSION);
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
                                                player.sendMessage(VelocityLang.NO_PERMISSION);
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            String username = context.getArgument("username", String.class);
                                            ResolvablePlayer senderPlayer = api.services().playerService().fetch(username).orElseThrow();

                                            if(senderPlayer == null)
                                                return closeMessage(player, VelocityLang.NO_PLAYER.build(username));

                                            try {
                                                FriendRequest invite = friendsService.findRequest(ResolvablePlayer.from(player), senderPlayer).orElse(null);
                                                if (invite == null) throw new NoOutputException();

                                                try {
                                                    invite.ignore();

                                                    return Command.SINGLE_SUCCESS;
                                                } catch (Exception ignore) {
                                                    friendsService.closeInvite(invite);
                                                }

                                                return closeMessage(player, VelocityLang.FRIEND_REQUEST_IGNORE.build(username));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                return closeMessage(player, VelocityLang.INTERNAL_ERROR);
                                            }
                                        })
                                )
                                .then(LiteralArgumentBuilder.<CommandSource>literal("accept")
                                        .executes(context -> {
                                            if(!(context.getSource() instanceof Player player)) {
                                                logger.log("/friends must be sent as a player!");
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            if(!Permission.validate(player, "rustyconnector.command.friends")) {
                                                player.sendMessage(VelocityLang.NO_PERMISSION);
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            String username = context.getArgument("username", String.class);
                                            ResolvablePlayer senderPlayer = api.services().playerService().fetch(username).orElseThrow();

                                            if (senderPlayer == null)
                                                return closeMessage(player, VelocityLang.NO_PLAYER.build(username));

                                            FriendRequest invite = friendsService.findRequest(ResolvablePlayer.from(player), senderPlayer).orElse(null);
                                            if (invite == null)
                                                return closeMessage(player, VelocityLang.FRIEND_REQUEST_EXPIRED);

                                            try {
                                                invite.accept();
                                                return Command.SINGLE_SUCCESS;
                                            } catch (IllegalStateException e) {
                                                return closeMessage(player, Component.text(e.getMessage(), NamedTextColor.RED));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                return closeMessage(player, VelocityLang.INTERNAL_ERROR);
                                            }
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