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
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

public final class CommandFM {
    public static BrigadierCommand create() {
        VelocityAPI api = VelocityAPI.get();
        PluginLogger logger = api.logger();

        FriendsService friendsService = api.services().friendsService().orElse(null);
        if (friendsService == null) {
            logger.send(Component.text("The Friends service must be enabled to load the /friends command.", NamedTextColor.YELLOW));
            return null;
        }

        LiteralCommandNode<CommandSource> fm = LiteralArgumentBuilder
                .<CommandSource>literal("fm")
                .requires(source -> source instanceof Player)
                .executes(context -> {
                    if(!(context.getSource() instanceof Player player)) {
                        logger.log("/fm must be sent as a player!");
                        return Command.SINGLE_SUCCESS;
                    }

                    if(!Permission.validate(player, "rustyconnector.command.fm")) {
                        player.sendMessage(VelocityLang.COMMAND_NO_PERMISSION);
                        return Command.SINGLE_SUCCESS;
                    }

                    return closeMessage(player, VelocityLang.FM_USAGE.build());
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
                                logger.log("/fm must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            if(!Permission.validate(player, "rustyconnector.command.fm")) {
                                player.sendMessage(VelocityLang.COMMAND_NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }

                            return closeMessage(player, VelocityLang.FM_USAGE.build());
                        }).then(RequiredArgumentBuilder.<CommandSource, String>argument("message", StringArgumentType.greedyString())
                            .executes(context -> {
                                if(!(context.getSource() instanceof Player player)) {
                                    logger.log("/fm must be sent as a player!");
                                    return Command.SINGLE_SUCCESS;
                                }

                                if(!Permission.validate(player, "rustyconnector.command.fm")) {
                                    player.sendMessage(VelocityLang.COMMAND_NO_PERMISSION);
                                    return Command.SINGLE_SUCCESS;
                                }

                                String username = context.getArgument("username", String.class);
                                Player targetPlayer = api.velocityServer().getPlayer(username).orElse(null);

                                if(targetPlayer == null)
                                    return closeMessage(player, Component.text(username + " doesn't seem to exist!", NamedTextColor.RED));
                                if(player.equals(targetPlayer))
                                    return closeMessage(player, Component.text("You can't message yourself!", NamedTextColor.RED));
                                if(!friendsService.areFriends(player, targetPlayer))
                                    return closeMessage(player, Component.text("You can only send messages to your friends!", NamedTextColor.RED));

                                String message = context.getArgument("message", String.class);

                                player.sendMessage(Component.text("[you -> "+targetPlayer.getUsername()+"]: "+message, NamedTextColor.GRAY));
                                targetPlayer.sendMessage(Component.text("["+player.getUsername()+" -> you]: "+message, NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text("Click to reply"))).clickEvent(ClickEvent.suggestCommand("/fm "+player.getUsername()+" ")));

                                return Command.SINGLE_SUCCESS;
                            })
                        )
                )
                .build();

        // BrigadierCommand implements Command
        return new BrigadierCommand(fm);
    }

    public static int closeMessage(Player player, Component message) {
        player.sendMessage(message);
        return Command.SINGLE_SUCCESS;
    }
}