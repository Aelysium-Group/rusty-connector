package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.TPAHandler;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.TPAService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.Permission;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
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
    public static boolean tpaEnabled(Player sender) {
        VelocityAPI api = VelocityAPI.get();
        try {
            TPAService tpaService = api.services().dynamicTeleportService().orElseThrow()
                                       .services().tpaService().orElseThrow();

            ServerInfo serverInfo = sender.getCurrentServer().orElseThrow().getServerInfo();
            PlayerServer targetServer = api.services().serverService().search(serverInfo);
            String familyName = targetServer.family().name();

            return tpaService.settings().enabledFamilies().contains(familyName);
        } catch (Exception ignore) {}
        return false;
    }

    public static BrigadierCommand create() {
        VelocityAPI api = VelocityAPI.get();
        PluginLogger logger = api.logger();

        FamilyService familyService = api.services().familyService();
        ServerService serverService = api.services().serverService();
        TPAService tpaService = api.services().dynamicTeleportService().orElseThrow()
                                   .services().tpaService().orElseThrow();

        LiteralCommandNode<CommandSource> tpa = LiteralArgumentBuilder
                .<CommandSource>literal("tpa")
                .requires(source -> source instanceof Player)
                .executes(context -> {
                    if(!(context.getSource() instanceof Player player)) {
                        logger.log("/tpa must be sent as a player!");
                        return Command.SINGLE_SUCCESS;
                    }

                    if(!CommandTPA.tpaEnabled(player)) {
                        context.getSource().sendMessage(Lang.UNKNOWN_COMMAND);
                        return Command.SINGLE_SUCCESS;
                    }
                    if(!Permission.validate(player, "rustyconnector.command.tpa")) {
                        player.sendMessage(VelocityLang.COMMAND_NO_PERMISSION);
                        return Command.SINGLE_SUCCESS;
                    }


                    context.getSource().sendMessage(VelocityLang.TPA_USAGE.build());
                    return Command.SINGLE_SUCCESS;
                })
                .then(LiteralArgumentBuilder.<CommandSource>literal("ignore")
                        .executes(context -> {
                            if(!(context.getSource() instanceof Player)) {
                                logger.log("/tpa must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            if(!CommandTPA.tpaEnabled((Player) context.getSource())) {
                                context.getSource().sendMessage(VelocityLang.UNKNOWN_COMMAND);
                                return Command.SINGLE_SUCCESS;
                            }


                            context.getSource().sendMessage(VelocityLang.TPA_IGNORE_USAGE.build());
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    if(!(context.getSource() instanceof Player player)) return builder.buildFuture();

                                    try {
                                        ServerInfo sendingServer = ((Player) context.getSource()).getCurrentServer().orElseThrow().getServerInfo();

                                        String familyName = serverService.search(sendingServer).family().name();
                                        BaseServerFamily family = familyService.find(familyName);
                                        if(!(family instanceof PlayerFocusedServerFamily)) return builder.buildFuture();

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
                                    if(!(context.getSource() instanceof Player player)) {
                                        logger.log("/tpa must be sent as a player!");
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    if(!CommandTPA.tpaEnabled(player)) {
                                        context.getSource().sendMessage(Lang.UNKNOWN_COMMAND);
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    if(!Permission.validate(player, "rustyconnector.command.tpa")) {
                                        player.sendMessage(VelocityLang.COMMAND_NO_PERMISSION);
                                        return 0;
                                    }

                                    String username = context.getArgument("username", String.class);

                                    try {
                                        Player senderPlayer = api.velocityServer().getPlayer(username).orElseThrow();
                                        ServerInfo targetServerInfo = ((Player) context.getSource()).getCurrentServer().orElseThrow().getServerInfo();

                                        PlayerServer targetServer = serverService.search(targetServerInfo);
                                        String familyName = targetServer.family().name();
                                        try {
                                            BaseServerFamily family = familyService.find(familyName);
                                            if(family == null) throw new NullPointerException();
                                            if(!(family instanceof PlayerFocusedServerFamily)) throw new NullPointerException();

                                            TPAHandler tpaHandler = tpaService.tpaHandler(family);
                                            TPARequest request = tpaHandler.findRequest(senderPlayer, (Player) context.getSource());
                                            if(request == null) {
                                                context.getSource().sendMessage(VelocityLang.TPA_FAILURE_NO_REQUEST.build(username));
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            request.ignore();
                                            tpaHandler.remove(request);
                                            return Command.SINGLE_SUCCESS;
                                        } catch (NullPointerException e) {
                                            logger.send(Component.text("Player attempted to use /tpa deny from a family that doesn't exist! (How?)", NamedTextColor.RED));
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
                            if(!(context.getSource() instanceof Player player)) {
                                logger.log("/tpa must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            if(!CommandTPA.tpaEnabled(player)) {
                                context.getSource().sendMessage(Lang.UNKNOWN_COMMAND);
                                return Command.SINGLE_SUCCESS;
                            }
                            if(!Permission.validate(player, "rustyconnector.command.tpa")) {
                                player.sendMessage(VelocityLang.COMMAND_NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }


                            context.getSource().sendMessage(VelocityLang.TPA_ACCEPT_USAGE.build());
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    if(!(context.getSource() instanceof Player player)) return builder.buildFuture();

                                    try {
                                        ServerInfo sendingServer = ((Player) context.getSource()).getCurrentServer().orElseThrow().getServerInfo();

                                        String familyName = serverService.search(sendingServer).family().name();
                                        BaseServerFamily family = familyService.find(familyName);
                                        if(!(family instanceof PlayerFocusedServerFamily)) return builder.buildFuture();
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
                                    if(!(context.getSource() instanceof Player)) {
                                        logger.log("/tpa must be sent as a player!");
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    String username = context.getArgument("username", String.class);

                                    try {
                                        Player senderPlayer = api.velocityServer().getPlayer(username).orElseThrow();
                                        ServerInfo targetServerInfo = ((Player) context.getSource()).getCurrentServer().orElseThrow().getServerInfo();

                                        PlayerServer targetServer = serverService.search(targetServerInfo);
                                        try {
                                            BaseServerFamily family = targetServer.family();
                                            if(family == null) throw new NullPointerException();

                                            TPAHandler tpaHandler = tpaService.tpaHandler(family);
                                            TPARequest request = tpaHandler.findRequest(senderPlayer, (Player) context.getSource());
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
                            if(!(context.getSource() instanceof Player player)) return builder.buildFuture();

                            try {
                                ServerInfo sendingServer = ((Player) context.getSource()).getCurrentServer().orElseThrow().getServerInfo();

                                String familyName = serverService.search(sendingServer).family().name();
                                BaseServerFamily family = familyService.find(familyName);

                                family.allPlayers(50).forEach(nearbyPlayer -> {
                                    if(nearbyPlayer.equals(player)) return;

                                    builder.suggest(((Player) nearbyPlayer).getUsername());
                                });

                                return builder.buildFuture();
                            } catch (Exception ignored) {}

                            builder.suggest("Searching for players...");
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            if(!(context.getSource() instanceof Player player)) {
                                logger.log("/tpa must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            if(!CommandTPA.tpaEnabled(player)) {
                                context.getSource().sendMessage(Lang.UNKNOWN_COMMAND);
                                return Command.SINGLE_SUCCESS;
                            }
                            if(!Permission.validate(player, "rustyconnector.command.tpa")) {
                                player.sendMessage(VelocityLang.COMMAND_NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }

                            String username = context.getArgument("username", String.class);

                            try {
                                Player targetPlayer = api.velocityServer().getPlayer(username).orElseThrow();

                                if(player.equals(targetPlayer)) {
                                    player.sendMessage(VelocityLang.TPA_FAILURE_SELF_TP);
                                    return Command.SINGLE_SUCCESS;
                                }

                                ServerInfo sendersServerInfo = player.getCurrentServer().orElseThrow().getServerInfo();
                                PlayerServer sendersServer = serverService.search(sendersServerInfo);
                                try {
                                    BaseServerFamily family = sendersServer.family();
                                    if(family == null) throw new NullPointerException();
                                    if(!(family instanceof PlayerFocusedServerFamily)) throw new NullPointerException();
                                    TPAHandler tpaHandler = tpaService.tpaHandler(family);

                                    if(tpaHandler.findRequestSender((Player) context.getSource()) != null) {
                                        context.getSource().sendMessage(VelocityLang.TPA_REQUEST_DUPLICATE.build(targetPlayer.getUsername()));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    TPARequest request = tpaHandler.newRequest((Player) context.getSource(), targetPlayer);
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