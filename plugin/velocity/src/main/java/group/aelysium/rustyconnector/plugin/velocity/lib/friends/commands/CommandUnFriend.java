package group.aelysium.rustyconnector.plugin.velocity.lib.friends.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.Permission;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.Optional;

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
                .requires(source -> source instanceof com.velocitypowered.api.proxy.Player)
                .executes(context -> {
                    if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player player)) {
                        logger.log("/unfriend must be sent as a player!");
                        return Command.SINGLE_SUCCESS;
                    }

                    if(!Permission.validate(player, "rustyconnector.command.unfriend")) {
                        player.sendMessage(ProxyLang.NO_PERMISSION);
                        return Command.SINGLE_SUCCESS;
                    }

                    return closeMessage(player, ProxyLang.UNFRIEND_USAGE);
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player eventPlayer)) return builder.buildFuture();
                            Player player = new Player(eventPlayer);

                            try {
                                List<IPlayer> friends = friendsService.friendStorage().get(player).orElseThrow();

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
                            if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player player)) {
                                logger.log("/unfriend must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            if(!Permission.validate(player, "rustyconnector.command.unfriend")) {
                                player.sendMessage(ProxyLang.NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }

                            String username = context.getArgument("username", String.class);
                            Player targetPlayer = new Player.UsernameReference(username).get();

                            Optional<Boolean> contains = friendsService.friendStorage().contains(new Player(player), targetPlayer);
                            if(contains.isEmpty())
                                return closeMessage(player, ProxyLang.INTERNAL_ERROR);
                            if(!contains.get())
                                return closeMessage(player, ProxyLang.UNFRIEND_NOT_FRIENDS.build(username));

                            if(targetPlayer == null)
                                return closeMessage(player, ProxyLang.NO_PLAYER.build(username));

                            try {
                                friendsService.friendStorage().delete(new Player(player), targetPlayer);

                                return closeMessage(player, ProxyLang.UNFRIEND_SUCCESS.build(username));
                            } catch (IllegalStateException e) {
                                return closeMessage(player, Component.text(e.getMessage(), NamedTextColor.RED));
                            } catch (Exception e) {
                                e.printStackTrace();
                                return closeMessage(player, ProxyLang.INTERNAL_ERROR);
                            }
                        })
                )
                .build();

        // BrigadierCommand implements Command
        return new BrigadierCommand(unfriend);
    }

    public static int closeMessage(com.velocitypowered.api.proxy.Player player, Component message) {
        player.sendMessage(message);
        return Command.SINGLE_SUCCESS;
    }
}