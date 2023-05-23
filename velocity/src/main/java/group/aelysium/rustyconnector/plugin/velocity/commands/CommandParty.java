package group.aelysium.rustyconnector.plugin.velocity.commands;

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
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.Permission;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.PartyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import group.aelysium.rustyconnector.plugin.velocity.lib.tpa.TPARequest;
import group.aelysium.rustyconnector.plugin.velocity.lib.tpa.TPACleaningService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.NoSuchElementException;

public final class CommandParty {

    /**
     * Check if /tpa is enabled in a player's family.
     * @param sender The player to check.
     * @return `true` is /tpa is allowed. `false` otherwise.
     */
    public static boolean tpaEnabled(Player sender) {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        try {
            ServerInfo serverInfo = sender.getCurrentServer().orElseThrow().getServerInfo();
            PlayerServer targetServer = api.getService(ServerService.class).findServer(serverInfo);
            String familyName = targetServer.getFamilyName();

            BaseServerFamily family = api.getService(FamilyService.class).find(familyName);
            if(family == null) return false;

            return family.getTPAHandler().getSettings().isEnabled();
        } catch (Exception ignore) {}
        return false;
    }

    /**
     * Check if /tpa is enabled in a player's family.
     * @param sender The player to check.
     * @return `true` is /tpa is allowed. `false` otherwise.
     */
    public static boolean hasPermission(Player sender) {
        if(Permission.validate(sender, "rustyconnector.command.party"))
            return true;
        return false;
    }

    public static BrigadierCommand create() {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();

        LiteralCommandNode<CommandSource> party = LiteralArgumentBuilder
                .<CommandSource>literal("party")
                .requires(source -> source instanceof Player)
                .executes(context -> {
                    if(!(context.getSource() instanceof Player player)) {
                        logger.log("/tpa must be sent as a player!");
                        return Command.SINGLE_SUCCESS;
                    }

                    if(!CommandParty.tpaEnabled(player)) {
                        context.getSource().sendMessage(Lang.UNKNOWN_COMMAND);
                        return Command.SINGLE_SUCCESS;
                    }
                    if(!Permission.validate(player, "rustyconnector.command.tpa")) {
                        player.sendMessage(VelocityLang.TPA_NO_PERMISSION);
                        return Command.SINGLE_SUCCESS;
                    }


                    context.getSource().sendMessage(VelocityLang.TPA_USAGE.build());
                    return Command.SINGLE_SUCCESS;
                })
                .then(LiteralArgumentBuilder.<CommandSource>literal("create")
                        .executes(context -> {
                            if(!(context.getSource() instanceof Player)) {
                                logger.log("/tpa must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            if(!CommandParty.tpaEnabled((Player) context.getSource())) {
                                context.getSource().sendMessage(VelocityLang.UNKNOWN_COMMAND);
                                return Command.SINGLE_SUCCESS;
                            }

                            Player player = (Player) context.getSource(); 

                            if(api.getService(PartyService.class).find(player) != null) throw new REPLACEME();

                            api.getService(PartyService.class).create(player);
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("accept")
                        .executes(context -> {
                            if(!(context.getSource() instanceof Player player)) {
                                logger.log("/tpa must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            if(!CommandParty.tpaEnabled(player)) {
                                context.getSource().sendMessage(Lang.UNKNOWN_COMMAND);
                                return Command.SINGLE_SUCCESS;
                            }
                            if(!Permission.validate(player, "rustyconnector.command.tpa")) {
                                player.sendMessage(VelocityLang.TPA_NO_PERMISSION);
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

                                        String familyName = api.getService(ServerService.class).findServer(sendingServer).getFamilyName();
                                        BaseServerFamily family = api.getService(FamilyService.class).find(familyName);
                                        if(!(family instanceof PlayerFocusedServerFamily)) return builder.buildFuture();
                                        List<TPARequest> requests = ((PlayerFocusedServerFamily) family).getTPAHandler().findRequestsForTarget(player);

                                        if(requests.size() <= 0) {
                                            builder.suggest("You have no pending TPA requests!");
                                            return builder.buildFuture();
                                        }

                                        family.getTPAHandler().findRequestsForTarget(player).forEach(targetRequest -> builder.suggest(targetRequest.getSender().getUsername()));

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
                                        Player senderPlayer = api.getServer().getPlayer(username).orElseThrow();
                                        ServerInfo targetServerInfo = ((Player) context.getSource()).getCurrentServer().orElseThrow().getServerInfo();

                                        PlayerServer targetServer = virtualProcessor.findServer(targetServerInfo);
                                        String familyName = targetServer.getFamilyName();
                                        try {
                                            BaseServerFamily family = virtualProcessor.getService(FamilyService.class).find(familyName);
                                            if(family == null) throw new NullPointerException();

                                            TPARequest request = family.getTPAHandler().findRequest(senderPlayer, (Player) context.getSource());
                                            if(request == null) {
                                                context.getSource().sendMessage(VelocityLang.TPA_FAILURE_NO_REQUEST.build(username));
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            request.accept();
                                            family.getTPAHandler().remove(request);
                                            return Command.SINGLE_SUCCESS;
                                        } catch (NullPointerException e) {
                                            logger.send(Component.text("Player attempted to use /tpa accept from a family that doesn't exist! (How?)", NamedTextColor.RED));
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
                            if(!(context.getSource() instanceof Player)) return builder.buildFuture();

                            try {
                                ServerInfo sendingServer = ((Player) context.getSource()).getCurrentServer().orElseThrow().getServerInfo();

                                String familyName = virtualProcessor.findServer(sendingServer).getFamilyName();
                                BaseServerFamily family = virtualProcessor.getService(FamilyService.class).find(familyName);

                                family.getAllPlayers(100).forEach(player -> builder.suggest(player.getUsername()));

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

                            if(!CommandParty.tpaEnabled(player)) {
                                context.getSource().sendMessage(Lang.UNKNOWN_COMMAND);
                                return Command.SINGLE_SUCCESS;
                            }
                            if(!Permission.validate(player, "rustyconnector.command.tpa")) {
                                player.sendMessage(VelocityLang.TPA_NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }

                            String username = context.getArgument("username", String.class);

                            try {
                                Player targetPlayer = api.getServer().getPlayer(username).orElseThrow();

                                if(player.equals(targetPlayer)) {
                                    player.sendMessage(VelocityLang.TPA_FAILURE_SELF_TP);
                                    return Command.SINGLE_SUCCESS;
                                }

                                ServerInfo sendersServerInfo = ((Player) context.getSource()).getCurrentServer().orElseThrow().getServerInfo();

                                PlayerServer sendersServer = virtualProcessor.findServer(sendersServerInfo);
                                String familyName = sendersServer.getFamilyName();
                                try {
                                    BaseServerFamily family = virtualProcessor.getService(FamilyService.class).find(familyName);
                                    if(family == null) throw new NullPointerException();
                                    if(!family.getTPAHandler().getSettings().isEnabled()) throw new RuntimeException();

                                    if(family.getTPAHandler().findRequestSender((Player) context.getSource()) != null) {
                                        context.getSource().sendMessage(VelocityLang.TPA_REQUEST_DUPLICATE.build(targetPlayer.getUsername()));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    TPARequest request = family.getTPAHandler().newRequest((Player) context.getSource(), targetPlayer);
                                    request.submit();

                                    return Command.SINGLE_SUCCESS;
                                } catch (NullPointerException e) {
                                    logger.send(Component.text("Player attempted to use /tpa from a family that doesn't exist! (How?)", NamedTextColor.RED));
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