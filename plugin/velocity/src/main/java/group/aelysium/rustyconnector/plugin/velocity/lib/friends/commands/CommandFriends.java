package group.aelysium.rustyconnector.plugin.velocity.lib.friends.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.Permission;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.friends.IFriendRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.Optional;

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
                .requires(source -> source instanceof com.velocitypowered.api.proxy.Player)
                .executes(context -> {
                    if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player eventPlayer)) {
                        logger.log("/friends must be sent as a player!");
                        return Command.SINGLE_SUCCESS;
                    }
                    Player player = new Player(eventPlayer);

                    if(!Permission.validate(eventPlayer, "rustyconnector.command.friends")) {
                        player.sendMessage(ProxyLang.NO_PERMISSION);
                        return Command.SINGLE_SUCCESS;
                    }

                    return closeMessage(eventPlayer, ProxyLang.FRIENDS_BOARD.build(player));
                })
                .then(LiteralArgumentBuilder.<CommandSource>literal("add")
                    .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                            .suggests((context, builder) -> {
                                if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player player)) return builder.buildFuture();

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
                                if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) {
                                    logger.log("/friends must be sent as a player!");
                                    return Command.SINGLE_SUCCESS;
                                }

                                if(!Permission.validate(velocityPlayer, "rustyconnector.command.friends")) {
                                    velocityPlayer.sendMessage(ProxyLang.NO_PERMISSION);
                                    return Command.SINGLE_SUCCESS;
                                }

                                Player player = new Player(velocityPlayer);

                                String username = context.getArgument("username", String.class);
                                try {
                                    Player targetPlayer = new Player.UsernameReference(username).get();

                                    Optional<Boolean> contains = friendsService.friendStorage().contains(player, targetPlayer);
                                    if(contains.isEmpty())
                                        return closeMessage(velocityPlayer, ProxyLang.INTERNAL_ERROR);
                                    if (contains.get())
                                        return closeMessage(velocityPlayer, ProxyLang.FRIEND_REQUEST_ALREADY_FRIENDS.build(username));

                                    if (targetPlayer == null)
                                        return closeMessage(velocityPlayer, ProxyLang.NO_PLAYER.build(username));

                                    friendsService.sendRequest(player, username);
                                } catch (Exception ignore) {
                                    return closeMessage(velocityPlayer, ProxyLang.NO_PLAYER.build(username));
                                }

                                return Command.SINGLE_SUCCESS;
                            })
                    )
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("requests")
                        .executes(context -> {
                            if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player player)) {
                                logger.log("/friends must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            if(!Permission.validate(player, "rustyconnector.command.friends")) {
                                player.sendMessage(ProxyLang.NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }

                            context.getSource().sendMessage(ProxyLang.FRIEND_REQUEST_USAGE.build());
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player player)) return builder.buildFuture();

                                    try {
                                        List<IFriendRequest> requests = friendsService.findRequestsToTarget(new Player(player));

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
                                    if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player player)) {
                                        logger.log("/friends must be sent as a player!");
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    if(!Permission.validate(player, "rustyconnector.command.friends")) {
                                        player.sendMessage(ProxyLang.NO_PERMISSION);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    context.getSource().sendMessage(ProxyLang.FRIEND_REQUEST_USAGE.build());
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(LiteralArgumentBuilder.<CommandSource>literal("ignore")
                                        .executes(context -> {
                                            if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player player)) {
                                                logger.log("/friends must be sent as a player!");
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            if(!Permission.validate(player, "rustyconnector.command.friends")) {
                                                player.sendMessage(ProxyLang.NO_PERMISSION);
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            String username = context.getArgument("username", String.class);
                                            Player senderPlayer = new Player.UsernameReference(username).get();

                                            if(senderPlayer == null)
                                                return closeMessage(player, ProxyLang.NO_PLAYER.build(username));

                                            IFriendRequest invite = friendsService.findRequest(new Player(player), senderPlayer).orElse(null);
                                            if (invite == null)
                                                return closeMessage(player, ProxyLang.INTERNAL_ERROR);

                                            try {
                                                invite.ignore();
                                            } catch (Exception ignore) {
                                                friendsService.closeInvite(invite);
                                            }

                                            return closeMessage(player, ProxyLang.FRIEND_REQUEST_IGNORE.build(username));
                                        })
                                )
                                .then(LiteralArgumentBuilder.<CommandSource>literal("accept")
                                        .executes(context -> {
                                            if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player player)) {
                                                logger.log("/friends must be sent as a player!");
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            if(!Permission.validate(player, "rustyconnector.command.friends")) {
                                                player.sendMessage(ProxyLang.NO_PERMISSION);
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            String username = context.getArgument("username", String.class);
                                            Player senderPlayer = new Player.UsernameReference(username).get();

                                            if (senderPlayer == null)
                                                return closeMessage(player, ProxyLang.NO_PLAYER.build(username));

                                            IFriendRequest invite = friendsService.findRequest(new Player(player), senderPlayer).orElse(null);
                                            if (invite == null)
                                                return closeMessage(player, ProxyLang.FRIEND_REQUEST_EXPIRED);

                                            try {
                                                invite.accept();
                                                return Command.SINGLE_SUCCESS;
                                            } catch (IllegalStateException e) {
                                                return closeMessage(player, Component.text(e.getMessage(), NamedTextColor.RED));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                return closeMessage(player, ProxyLang.INTERNAL_ERROR);
                                            }
                                        })
                                )
                        )
                )
                .build();

        // BrigadierCommand implements Command
        return new BrigadierCommand(friends);
    }

    public static int closeMessage(com.velocitypowered.api.proxy.Player player, Component message) {
        player.sendMessage(message);
        return Command.SINGLE_SUCCESS;
    }
}