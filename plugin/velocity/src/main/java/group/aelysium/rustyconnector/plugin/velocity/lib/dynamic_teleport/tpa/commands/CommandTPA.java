package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.TPAHandler;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.TPAService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.plugin.velocity.lib.Permission;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.TPARequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.NoSuchElementException;

public final class CommandTPA {

    /**
     * Check if /tpa is enabled in a player's family.
     * @param sender The player to check.
     * @return `true` is /tpa is allowed. `false` otherwise.
     */
    public static boolean tpaEnabled(com.velocitypowered.api.proxy.Player sender) {
        Tinder api = Tinder.get();
        try {
            TPAService tpaService = api.services().dynamicTeleport().orElseThrow()
                                       .services().tpaService().orElseThrow();

            ServerInfo serverInfo = sender.getCurrentServer().orElseThrow().getServerInfo();
            MCLoader targetServer = new MCLoader.Reference(serverInfo).get();
            Family family = targetServer.family();

            if(!family.metadata().tpaAllowed()) return false;

            return tpaService.settings().enabledFamilies().contains(family.id());
        } catch (Exception ignore) {}
        return false;
    }

    public static BrigadierCommand create(DependencyInjector.DI3<FamilyService, ServerService, TPAService> dependencies) {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();

        FamilyService familyService = dependencies.d1();
        ServerService serverService = dependencies.d2();
        TPAService tpaService = dependencies.d3();

        LiteralCommandNode<CommandSource> tpa = LiteralArgumentBuilder
                .<CommandSource>literal("tpa")
                .requires(source -> source instanceof com.velocitypowered.api.proxy.Player)
                .executes(context -> {
                    if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player player)) {
                        logger.log("/tpa must be sent as a player!");
                        return Command.SINGLE_SUCCESS;
                    }

                    if(!CommandTPA.tpaEnabled(player)) {
                        context.getSource().sendMessage(VelocityLang.UNKNOWN_COMMAND);
                        return Command.SINGLE_SUCCESS;
                    }
                    if(!Permission.validate(player, "rustyconnector.command.tpa")) {
                        player.sendMessage(VelocityLang.NO_PERMISSION);
                        return Command.SINGLE_SUCCESS;
                    }

                    context.getSource().sendMessage(VelocityLang.TPA_USAGE);
                    return Command.SINGLE_SUCCESS;
                })
                .then(LiteralArgumentBuilder.<CommandSource>literal("deny")
                        .executes(context -> {
                            if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player)) {
                                logger.log("/tpa must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            if(!CommandTPA.tpaEnabled((com.velocitypowered.api.proxy.Player) context.getSource())) {
                                context.getSource().sendMessage(VelocityLang.UNKNOWN_COMMAND);
                                return Command.SINGLE_SUCCESS;
                            }


                            context.getSource().sendMessage(VelocityLang.TPA_DENY_USAGE.build());
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player player)) return builder.buildFuture();

                                    try {
                                        ServerInfo sendingServer = player.getCurrentServer().orElseThrow().getServerInfo();
                                        MCLoader server = new MCLoader.Reference(sendingServer).get();

                                        Family family = server.family();

                                        if(!family.metadata().tpaAllowed()) return builder.buildFuture();

                                        TPAHandler tpaHandler = tpaService.tpaHandler(family);
                                        List<TPARequest> requests = tpaHandler.findRequestsForTarget(player);

                                        if(requests.size() == 0) {
                                            builder.suggest("You have no pending TPA requests!");
                                            return builder.buildFuture();
                                        }

                                        tpaHandler.findRequestsForTarget(player).forEach(targetRequest -> builder.suggest(targetRequest.sender().getUsername()));

                                        return builder.buildFuture();
                                    } catch (Exception ignored) {}

                                    builder.suggest("Searching for players...");
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player player)) {
                                        logger.log("/tpa must be sent as a player!");
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    if(!CommandTPA.tpaEnabled(player)) {
                                        context.getSource().sendMessage(VelocityLang.UNKNOWN_COMMAND);
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    if(!Permission.validate(player, "rustyconnector.command.tpa")) {
                                        player.sendMessage(VelocityLang.NO_PERMISSION);
                                        return 0;
                                    }

                                    String username = context.getArgument("username", String.class);

                                    try {
                                        com.velocitypowered.api.proxy.Player senderPlayer = api.velocityServer().getPlayer(username).orElseThrow();
                                        ServerInfo targetServerInfo = player.getCurrentServer().orElseThrow().getServerInfo();

                                        try {
                                            MCLoader targetServer = new MCLoader.Reference(targetServerInfo).get();
                                            Family family = targetServer.family();
                                            if(!family.metadata().tpaAllowed()) throw new NullPointerException();

                                            TPAHandler tpaHandler = tpaService.tpaHandler(family);
                                            TPARequest request = tpaHandler.findRequest(senderPlayer, player);
                                            if(request == null) {
                                                context.getSource().sendMessage(VelocityLang.TPA_FAILURE_NO_REQUEST.build(username));
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            request.deny();
                                            tpaHandler.remove(request);
                                            return Command.SINGLE_SUCCESS;
                                        } catch (NullPointerException e) {
                                            logger.send(Component.text("Player attempted to use /tpa deny from a family that doesn't exist!", NamedTextColor.RED));
                                            context.getSource().sendMessage(VelocityLang.TPA_FAILURE.build(username));
                                        }
                                    } catch (NoSuchElementException e) {
                                        context.getSource().sendMessage(VelocityLang.TPA_FAILURE_NO_USERNAME.build(username));
                                    } catch (Exception e) {
                                        context.getSource().sendMessage(VelocityLang.TPA_FAILURE.build(username));
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("accept")
                        .executes(context -> {
                            if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player player)) {
                                logger.log("/tpa must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            if(!CommandTPA.tpaEnabled(player)) {
                                context.getSource().sendMessage(VelocityLang.UNKNOWN_COMMAND);
                                return Command.SINGLE_SUCCESS;
                            }
                            if(!Permission.validate(player, "rustyconnector.command.tpa")) {
                                player.sendMessage(VelocityLang.NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }


                            context.getSource().sendMessage(VelocityLang.TPA_ACCEPT_USAGE.build());
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player player)) return builder.buildFuture();

                                    try {
                                        ServerInfo serverInfo = ((com.velocitypowered.api.proxy.Player) context.getSource()).getCurrentServer().orElseThrow().getServerInfo();

                                        MCLoader targetServer = new MCLoader.Reference(serverInfo).get();
                                        Family family = targetServer.family();
                                        if(!family.metadata().tpaAllowed()) throw new NullPointerException();

                                        TPAHandler tpaHandler = tpaService.tpaHandler(family);
                                        List<TPARequest> requests = tpaHandler.findRequestsForTarget(player);

                                        if(requests.size() == 0) {
                                            builder.suggest("You have no pending TPA requests!");
                                            return builder.buildFuture();
                                        }

                                        tpaHandler.findRequestsForTarget(player).forEach(targetRequest -> builder.suggest(targetRequest.sender().getUsername()));

                                        return builder.buildFuture();
                                    } catch (Exception ignored) {}

                                    builder.suggest("Searching for players...");
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player player)) {
                                        logger.log("/tpa must be sent as a player!");
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    String username = context.getArgument("username", String.class);

                                    try {
                                        com.velocitypowered.api.proxy.Player senderPlayer = api.velocityServer().getPlayer(username).orElseThrow();
                                        ServerInfo targetServerInfo = ((com.velocitypowered.api.proxy.Player) context.getSource()).getCurrentServer().orElseThrow().getServerInfo();

                                        try {
                                            MCLoader targetServer = new MCLoader.Reference(targetServerInfo).get();
                                            Family family = targetServer.family();
                                            if(!family.metadata().tpaAllowed()) throw new NullPointerException();

                                            TPAHandler tpaHandler = tpaService.tpaHandler(family);
                                            TPARequest request = tpaHandler.findRequest(senderPlayer, player);
                                            if(request == null) {
                                                context.getSource().sendMessage(VelocityLang.TPA_FAILURE_NO_REQUEST.build(username));
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            request.accept();
                                            tpaHandler.remove(request);
                                            return Command.SINGLE_SUCCESS;
                                        } catch (NullPointerException e) {
                                            logger.send(Component.text("Player attempted to use /tpa accept from a family that doesn't exist!", NamedTextColor.RED));
                                            context.getSource().sendMessage(VelocityLang.TPA_FAILURE.build(username));
                                        }
                                    } catch (NoSuchElementException e) {
                                        context.getSource().sendMessage(VelocityLang.TPA_FAILURE_NO_USERNAME.build(username));
                                    } catch (Exception e) {
                                        context.getSource().sendMessage(VelocityLang.TPA_FAILURE.build(username));
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player player)) return builder.buildFuture();

                            try {
                                ServerInfo sendingServer = ((com.velocitypowered.api.proxy.Player) context.getSource()).getCurrentServer().orElseThrow().getServerInfo();

                                MCLoader server = new MCLoader.Reference(sendingServer).get();
                                Family family = server.family();
                                if(!family.metadata().tpaAllowed()) throw new NullPointerException();

                                family.players(50).forEach(nearbyPlayer -> {
                                    if(nearbyPlayer.equals(player)) return;

                                    builder.suggest((nearbyPlayer).getUsername());
                                });

                                return builder.buildFuture();
                            } catch (Exception ignored) {}

                            builder.suggest("Searching for players...");
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player player)) {
                                logger.log("/tpa must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            if(!CommandTPA.tpaEnabled(player)) {
                                context.getSource().sendMessage(VelocityLang.UNKNOWN_COMMAND);
                                return Command.SINGLE_SUCCESS;
                            }
                            if(!Permission.validate(player, "rustyconnector.command.tpa")) {
                                player.sendMessage(VelocityLang.NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }

                            String username = context.getArgument("username", String.class);

                            try {
                                com.velocitypowered.api.proxy.Player targetPlayer = api.velocityServer().getPlayer(username).orElseThrow();

                                if(player.equals(targetPlayer)) {
                                    player.sendMessage(VelocityLang.TPA_FAILURE_SELF_TP);
                                    return Command.SINGLE_SUCCESS;
                                }

                                if(tpaService.settings().friendsOnly())
                                    try {
                                        FriendsService friendsService = Tinder.get().services().friends().orElseThrow();
                                        boolean areFriends = friendsService.areFriends(Player.from(player), Player.from(targetPlayer));
                                        if(!areFriends) {
                                            context.getSource().sendMessage(VelocityLang.TPA_NOT_FRIENDS.build(targetPlayer.getUsername()));
                                            return Command.SINGLE_SUCCESS;
                                        }
                                    } catch (NoSuchElementException ignore) {
                                        logger.warn("TPA is set to only allow teleportation between friends, but the friends module doesn't seem to be enabled! Ignoring this setting...");
                                    }

                                ServerInfo sendersServerInfo = player.getCurrentServer().orElseThrow().getServerInfo();
                                try {
                                    MCLoader sendersServer = new MCLoader.Reference(sendersServerInfo).get();
                                    Family family = sendersServer.family();
                                    if(!family.metadata().tpaAllowed()) throw new NullPointerException();
                                    TPAHandler tpaHandler = tpaService.tpaHandler(family);

                                    if(tpaHandler.findRequestSender(player) != null) {
                                        context.getSource().sendMessage(VelocityLang.TPA_REQUEST_DUPLICATE.build(targetPlayer.getUsername()));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    TPARequest request = tpaHandler.newRequest(player, targetPlayer);
                                    request.submit();

                                    return Command.SINGLE_SUCCESS;
                                } catch (NullPointerException e) {
                                    logger.send(Component.text("Player attempted to use /tpa from a family that doesn't exist! (How did this happen?)", NamedTextColor.RED));
                                    context.getSource().sendMessage(VelocityLang.TPA_FAILURE.build(username));
                                }
                            } catch (NoSuchElementException e) {
                                context.getSource().sendMessage(VelocityLang.TPA_FAILURE_NO_USERNAME.build(username));
                            } catch (Exception e) {
                                context.getSource().sendMessage(VelocityLang.TPA_FAILURE.build(username));
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .build();

        // BrigadierCommand implements Command
        return new BrigadierCommand(tpa);
    }
}