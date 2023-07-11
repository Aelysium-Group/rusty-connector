package group.aelysium.rustyconnector.plugin.velocity.lib.parties.commands;

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
import group.aelysium.rustyconnector.plugin.velocity.lib.Permission;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.DynamicTeleport_TPARequest;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.Party;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.PartyInvite;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.PartyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.NoSuchElementException;

public final class CommandParty {

    public static BrigadierCommand create() {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();

        // If this command class loads, then PartyService MUST be set.
        PartyService partyService = api.getService(PartyService.class).orElseThrow();

        LiteralCommandNode<CommandSource> tpa = LiteralArgumentBuilder
                .<CommandSource>literal("party")
                .requires(source -> source instanceof Player)
                .executes(context -> {
                    if(!(context.getSource() instanceof Player player)) {
                        logger.log("/party must be sent as a player!");
                        return Command.SINGLE_SUCCESS;
                    }

                    if(!Permission.validate(player, "rustyconnector.command.party")) {
                        player.sendMessage(VelocityLang.COMMAND_NO_PERMISSION);
                        return Command.SINGLE_SUCCESS;
                    }

                    Party party = partyService.find(player).orElse(null);
                    if(party == null) {
                        context.getSource().sendMessage(VelocityLang.PARTY_USAGE_NO_PARTY.build());
                        return Command.SINGLE_SUCCESS;
                    }

                    if(party.getLeader() == player) {
                        context.getSource().sendMessage(VelocityLang.PARTY_USAGE_PARTY_LEADER.build());
                        return Command.SINGLE_SUCCESS;
                    } else {
                        context.getSource().sendMessage(VelocityLang.PARTY_USAGE_PARTY_MEMBER.build());
                        return Command.SINGLE_SUCCESS;
                    }
                })
                .then(LiteralArgumentBuilder.<CommandSource>literal("deny")
                        .executes(context -> {
                            if(!(context.getSource() instanceof Player)) {
                                logger.log("/tpa must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            if(!CommandParty.tpaEnabled((Player) context.getSource())) {
                                context.getSource().sendMessage(VelocityLang.UNKNOWN_COMMAND);
                                return Command.SINGLE_SUCCESS;
                            }


                            context.getSource().sendMessage(VelocityLang.TPA_DENY_USAGE.build());
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
                                        List<DynamicTeleport_TPARequest> requests = ((PlayerFocusedServerFamily) family).getTPAHandler().findRequestsForTarget(player);

                                        if(requests.size() <= 0) {
                                            builder.suggest("You have no pending TPA requests!");
                                            return builder.buildFuture();
                                        }

                                        ((PlayerFocusedServerFamily) family).getTPAHandler().findRequestsForTarget(player).forEach(targetRequest -> builder.suggest(targetRequest.getSender().getUsername()));

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
                                        player.sendMessage(VelocityLang.COMMAND_NO_PERMISSION);
                                        return 0;
                                    }

                                    String username = context.getArgument("username", String.class);

                                    try {
                                        Player senderPlayer = api.getServer().getPlayer(username).orElseThrow();
                                        ServerInfo targetServerInfo = ((Player) context.getSource()).getCurrentServer().orElseThrow().getServerInfo();

                                        PlayerServer targetServer = api.getService(ServerService.class).findServer(targetServerInfo);
                                        String familyName = targetServer.getFamilyName();
                                        try {
                                            BaseServerFamily family = api.getService(FamilyService.class).find(familyName);
                                            if(family == null) throw new NullPointerException();
                                            if(!(family instanceof PlayerFocusedServerFamily)) throw new NullPointerException();

                                            DynamicTeleport_TPARequest request = ((PlayerFocusedServerFamily) family).getTPAHandler().findRequest(senderPlayer, (Player) context.getSource());
                                            if(request == null) {
                                                context.getSource().sendMessage(VelocityLang.TPA_FAILURE_NO_REQUEST.build(username));
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            request.deny();
                                            ((PlayerFocusedServerFamily) family).getTPAHandler().remove(request);
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

                            if(!CommandParty.tpaEnabled(player)) {
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
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("invites")
                        .executes(context -> {
                            if(!(context.getSource() instanceof Player player)) {
                                logger.log("/party must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            if(!Permission.validate(player, "rustyconnector.command.party")) {
                                player.sendMessage(VelocityLang.COMMAND_NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }

                            context.getSource().sendMessage(VelocityLang.PARTY_USAGE_INVITES.build());
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    if(!(context.getSource() instanceof Player player)) return builder.buildFuture();

                                    try {
                                        List<PartyInvite> invites = partyService.findInvitesToTarget(player);

                                        if(invites.size() <= 0) {
                                            builder.suggest("You have no pending party invites!");
                                            return builder.buildFuture();
                                        }

                                        invites.forEach(invite -> {
                                            builder.suggest(invite.getSender().getUsername());
                                        });

                                        return builder.buildFuture();
                                    } catch (Exception ignored) {}

                                    builder.suggest("Searching for invites...");
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    if(!(context.getSource() instanceof Player player)) {
                                        logger.log("/party must be sent as a player!");
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    if(!Permission.validate(player, "rustyconnector.command.party")) {
                                        player.sendMessage(VelocityLang.COMMAND_NO_PERMISSION);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    context.getSource().sendMessage(VelocityLang.PARTY_USAGE_INVITES.build());
                                    return Command.SINGLE_SUCCESS;
                                })

                                .then(LiteralArgumentBuilder.<CommandSource>literal("accept")
                                        .executes(context -> {
                                            if(!(context.getSource() instanceof Player player)) {
                                                logger.log("/party must be sent as a player!");
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            if(!Permission.validate(player, "rustyconnector.command.party")) {
                                                player.sendMessage(VelocityLang.COMMAND_NO_PERMISSION);
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            String username = context.getArgument("username", String.class);
                                            Player senderPlayer = api.getServer().getPlayer(username).orElse(null);
                                            if(senderPlayer == null) {
                                                player.sendMessage(Component.text("Unable to find "+ senderPlayer));
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            if(!senderPlayer.isActive()) {
                                                player.sendMessage(Component.text(senderPlayer + " isn't online for you to join their party anymore!"));
                                                partyService.findInvitesToTarget(player)
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
                .build();

        // BrigadierCommand implements Command
        return new BrigadierCommand(tpa);
    }
}