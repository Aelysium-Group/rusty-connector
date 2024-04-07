package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa.ITPAHandler;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa.ITPARequest;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.TPAService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.plugin.velocity.lib.Permission;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

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
                                       .services().tpa().orElseThrow();

            UUID serverInfo = UUID.fromString(sender.getCurrentServer().orElseThrow().getServerInfo().getName());
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
                        context.getSource().sendMessage(ProxyLang.UNKNOWN_COMMAND);
                        return Command.SINGLE_SUCCESS;
                    }
                    if(!Permission.validate(player, "rustyconnector.command.tpa")) {
                        player.sendMessage(ProxyLang.NO_PERMISSION);
                        return Command.SINGLE_SUCCESS;
                    }

                    context.getSource().sendMessage(ProxyLang.TPA_USAGE);
                    return Command.SINGLE_SUCCESS;
                })
                .then(LiteralArgumentBuilder.<CommandSource>literal("deny")
                        .executes(context -> {
                            if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player)) {
                                logger.log("/tpa must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            if(!CommandTPA.tpaEnabled((com.velocitypowered.api.proxy.Player) context.getSource())) {
                                context.getSource().sendMessage(ProxyLang.UNKNOWN_COMMAND);
                                return Command.SINGLE_SUCCESS;
                            }


                            context.getSource().sendMessage(ProxyLang.TPA_DENY_USAGE.build());
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) return builder.buildFuture();
                                    try {
                                        Player target = new Player(velocityPlayer);

                                        Family family = (Family) target.server().orElseThrow().family();
                                        if(!family.metadata().tpaAllowed()) throw new NullPointerException();

                                        ITPAHandler tpaHandler = tpaService.tpaHandler(family);
                                        List<ITPARequest> requests = tpaHandler.findRequestsForTarget(target);

                                        if(requests.size() == 0) {
                                            builder.suggest("You have no pending TPA requests!");
                                            return builder.buildFuture();
                                        }

                                        requests.forEach(targetRequest -> builder.suggest(targetRequest.sender().username()));

                                        return builder.buildFuture();
                                    } catch (Exception ignored) {}

                                    builder.suggest("Searching for players...");
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) {
                                        logger.log("/tpa must be sent as a player!");
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    if(!CommandTPA.tpaEnabled(velocityPlayer)) {
                                        context.getSource().sendMessage(ProxyLang.UNKNOWN_COMMAND);
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    if(!Permission.validate(velocityPlayer, "rustyconnector.command.tpa")) {
                                        velocityPlayer.sendMessage(ProxyLang.NO_PERMISSION);
                                        return 0;
                                    }
                                    String username = context.getArgument("username", String.class);

                                    try {
                                        Player target = new Player(velocityPlayer);

                                        com.velocitypowered.api.proxy.Player requestSendingVelocityPlayer = api.velocityServer().getPlayer(username).orElseThrow();
                                        Player sender = new Player(requestSendingVelocityPlayer);

                                        try {
                                            Family family = target.server().orElseThrow().family();
                                            if(!family.metadata().tpaAllowed()) throw new NullPointerException();

                                            ITPAHandler tpaHandler = tpaService.tpaHandler(family);
                                            ITPARequest request = tpaHandler.findRequest(sender, target);
                                            if(request == null) {
                                                context.getSource().sendMessage(ProxyLang.TPA_FAILURE_NO_REQUEST.build(username));
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            request.deny();
                                            tpaHandler.remove(request);
                                            return Command.SINGLE_SUCCESS;
                                        } catch (NullPointerException e) {
                                            logger.send(Component.text("Player attempted to use /tpa deny from a family that doesn't exist!", NamedTextColor.RED));
                                            context.getSource().sendMessage(ProxyLang.TPA_FAILURE.build(username));
                                        }
                                    } catch (NoSuchElementException e) {
                                        context.getSource().sendMessage(ProxyLang.TPA_FAILURE_NO_USERNAME.build(username));
                                    } catch (Exception e) {
                                        context.getSource().sendMessage(ProxyLang.TPA_FAILURE.build(username));
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
                                context.getSource().sendMessage(ProxyLang.UNKNOWN_COMMAND);
                                return Command.SINGLE_SUCCESS;
                            }
                            if(!Permission.validate(player, "rustyconnector.command.tpa")) {
                                player.sendMessage(ProxyLang.NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }


                            context.getSource().sendMessage(ProxyLang.TPA_ACCEPT_USAGE.build());
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) return builder.buildFuture();
                                    try {
                                        Player target = new Player(velocityPlayer);

                                        Family family = (Family) target.server().orElseThrow().family();
                                        if(!family.metadata().tpaAllowed()) throw new NullPointerException();

                                        ITPAHandler tpaHandler = tpaService.tpaHandler(family);
                                        List<ITPARequest> requests = tpaHandler.findRequestsForTarget(target);

                                        if(requests.size() == 0) {
                                            builder.suggest("You have no pending TPA requests!");
                                            return builder.buildFuture();
                                        }

                                        requests.forEach(targetRequest -> builder.suggest(targetRequest.sender().username()));

                                        return builder.buildFuture();
                                    } catch (Exception ignored) {}

                                    builder.suggest("Searching for players...");
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player velocityTargetPlayer)) {
                                        logger.log("/tpa must be sent as a player!");
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    Player target = new Player(velocityTargetPlayer);

                                    String username = context.getArgument("username", String.class);

                                    try {
                                        com.velocitypowered.api.proxy.Player senderVelocityPlayer = api.velocityServer().getPlayer(username).orElseThrow();
                                        Player sender = new Player(senderVelocityPlayer);

                                        try {
                                            Family family = sender.server().orElseThrow().family();
                                            if(!family.metadata().tpaAllowed()) throw new NullPointerException();

                                            ITPAHandler tpaHandler = tpaService.tpaHandler(family);
                                            ITPARequest request = tpaHandler.findRequest(sender, target);
                                            if(request == null) {
                                                context.getSource().sendMessage(ProxyLang.TPA_FAILURE_NO_REQUEST.build(username));
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            request.accept();
                                            tpaHandler.remove(request);
                                            return Command.SINGLE_SUCCESS;
                                        } catch (NullPointerException e) {
                                            logger.send(Component.text("Player attempted to use /tpa accept from a family that doesn't exist!", NamedTextColor.RED));
                                            context.getSource().sendMessage(ProxyLang.TPA_FAILURE.build(username));
                                        }
                                    } catch (NoSuchElementException e) {
                                        context.getSource().sendMessage(ProxyLang.TPA_FAILURE_NO_USERNAME.build(username));
                                    } catch (Exception e) {
                                        context.getSource().sendMessage(ProxyLang.TPA_FAILURE.build(username));
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player player)) return builder.buildFuture();

                            try {
                                UUID sendingServer = UUID.fromString(player.getCurrentServer().orElseThrow().getServerInfo().getName());

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
                            if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player sendingVelocityPlayer)) {
                                logger.log("/tpa must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            if(!CommandTPA.tpaEnabled(sendingVelocityPlayer)) {
                                context.getSource().sendMessage(ProxyLang.UNKNOWN_COMMAND);
                                return Command.SINGLE_SUCCESS;
                            }
                            if(!Permission.validate(sendingVelocityPlayer, "rustyconnector.command.tpa")) {
                                sendingVelocityPlayer.sendMessage(ProxyLang.NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }

                            String username = context.getArgument("username", String.class);

                            try {
                                Player sender = new Player(sendingVelocityPlayer);

                                com.velocitypowered.api.proxy.Player targetVelocityPlayer = api.velocityServer().getPlayer(username).orElseThrow();
                                Player target = new Player(targetVelocityPlayer);

                                if(sender.equals(target)) {
                                    sender.sendMessage(ProxyLang.TPA_FAILURE_SELF_TP);
                                    return Command.SINGLE_SUCCESS;
                                }

                                if(tpaService.settings().friendsOnly())
                                    try {
                                        FriendsService friendsService = Tinder.get().services().friends().orElseThrow();
                                        Optional<Boolean> contains = friendsService.friendStorage().contains(sender, target);
                                        if(contains.isEmpty()) {
                                            context.getSource().sendMessage(ProxyLang.INTERNAL_ERROR);
                                            return Command.SINGLE_SUCCESS;
                                        }
                                        if(!contains.get()) {
                                            context.getSource().sendMessage(ProxyLang.TPA_NOT_FRIENDS.build(targetVelocityPlayer.getUsername()));
                                            return Command.SINGLE_SUCCESS;
                                        }
                                    } catch (NoSuchElementException ignore) {
                                        logger.warn("TPA is set to only allow teleportation between friends, but the friends module doesn't seem to be enabled! Ignoring this setting...");
                                    }

                                try {
                                    Family family = (Family) sender.server().orElseThrow().family();
                                    if(!family.metadata().tpaAllowed()) throw new NullPointerException();
                                    ITPAHandler tpaHandler = tpaService.tpaHandler(family);

                                    if (Permission.validate(sendingVelocityPlayer, "rustyconnector.command.tpa.bypassRequest")) {
                                        tpaService.tpaSendPlayer(sender, target, (MCLoader) target.server().orElseThrow());
                                        sender.sendMessage(ProxyLang.TPA_REQUEST_BYPASSED.build(targetVelocityPlayer.getUsername()));
                                    } else {
                                        if(tpaHandler.findRequestSender(sender) != null) {
                                            context.getSource().sendMessage(ProxyLang.TPA_REQUEST_DUPLICATE.build(targetVelocityPlayer.getUsername()));
                                            return Command.SINGLE_SUCCESS;
                                        }

                                        ITPARequest request = tpaHandler.newRequest(sender, target);
                                        request.submit();
                                    }
                                    return Command.SINGLE_SUCCESS;
                                } catch (NullPointerException e) {
                                    logger.send(Component.text("Player attempted to use /tpa from a family that doesn't exist!", NamedTextColor.RED));
                                    context.getSource().sendMessage(ProxyLang.TPA_FAILURE.build(username));
                                }
                            } catch (NoSuchElementException e) {
                                context.getSource().sendMessage(ProxyLang.TPA_FAILURE_NO_USERNAME.build(username));
                            } catch (Exception e) {
                                context.getSource().sendMessage(ProxyLang.TPA_FAILURE.build(username));
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .build();

        // BrigadierCommand implements Command
        return new BrigadierCommand(tpa);
    }
}