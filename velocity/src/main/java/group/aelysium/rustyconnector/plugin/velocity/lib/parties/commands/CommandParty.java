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
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
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

                                        if(invites.size() == 0) {
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
                                .then(LiteralArgumentBuilder.<CommandSource>literal("deny")
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

                                            if(senderPlayer == null)
                                                return closeMessage(player, Component.text(username + " doesn't seem to exist on this server! (How did this happen?)", NamedTextColor.RED));

                                            try {
                                                PartyInvite invite = partyService.findInvite(player, senderPlayer).orElse(null);
                                                if(invite == null) throw new NoOutputException();

                                                try {
                                                    invite.deny();
                                                } catch (Exception ignore) {
                                                    partyService.closeInvite(invite);
                                                }
                                            } catch (Exception ignore) {}

                                            return closeMessage(player, Component.text("There was an issue accepting that invite!", NamedTextColor.RED));
                                        })
                                )
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
                                            if(senderPlayer == null || !senderPlayer.isActive())
                                                return closeMessage(player, Component.text(senderPlayer + " isn't online for you to join their party!", NamedTextColor.RED));

                                            PartyInvite invite = partyService.findInvite(player, senderPlayer).orElse(null);
                                            if(invite == null)
                                                return closeMessage(player, Component.text("The invite from " + senderPlayer + " has expired!", NamedTextColor.RED));

                                            try {
                                                invite.accept();
                                            } catch (IllegalStateException e) {
                                                return closeMessage(player, Component.text(e.getMessage(), NamedTextColor.RED));
                                            } catch (Exception ignore) {
                                                return closeMessage(player, Component.text("There was an issue accepting that invite!", NamedTextColor.RED));
                                            }

                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("create")
                        .executes(context -> {
                            if(!(context.getSource() instanceof Player player)) {
                                logger.log("/party must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            if(!Permission.validate(player, "rustyconnector.command.party")) {
                                player.sendMessage(VelocityLang.COMMAND_NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }

                            if(partyService.find(player).orElse(null) == null)
                                return closeMessage(player, Component.text("You can't start a party if you're already in one!", NamedTextColor.RED));

                            partyService.create(player);
                            player.sendMessage(Component.text("You created a new party!",NamedTextColor.GREEN));

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("disband")
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
                            if(party == null) return closeMessage(player, Component.text("You aren't in a party!", NamedTextColor.RED));

                            if(party.getLeader().equals(player))
                                return closeMessage(player, Component.text("Only the party leader can disband the party!", NamedTextColor.RED));

                            partyService.disband(party);
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("leave")
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
                            if(party == null) return closeMessage(player, Component.text("You aren't in a party!", NamedTextColor.RED));

                            party.leave(player);

                            return closeMessage(player, Component.text("You left the party.", NamedTextColor.GREEN));
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("invite")
                        .executes(context -> {
                            if(!(context.getSource() instanceof Player player)) {
                                logger.log("/party must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            if(!Permission.validate(player, "rustyconnector.command.party")) {
                                player.sendMessage(VelocityLang.COMMAND_NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }

                            context.getSource().sendMessage(VelocityLang.PARTY_USAGE_INVITE.build());
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
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
                                    if(party == null) return closeMessage(player, Component.text("You aren't in a party!", NamedTextColor.RED));

                                    if(partyService.getSettings().onlyLeaderCanInvite())
                                        if(!party.getLeader().equals(player))
                                            return closeMessage(player, Component.text("Only the party leader can invite people!", NamedTextColor.RED));

                                    String username = context.getArgument("username", String.class);
                                    Player targetPlayer = api.getServer().getPlayer(username).orElse(null);
                                    if(targetPlayer == null || !targetPlayer.isActive()) return closeMessage(player, Component.text(username + " isn't available to send an invite to!", NamedTextColor.RED));

                                    partyService.invitePlayer(party, player, targetPlayer);
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("kick")
                        .executes(context -> {
                            if(!(context.getSource() instanceof Player player)) {
                                logger.log("/party must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            if(!Permission.validate(player, "rustyconnector.command.party")) {
                                player.sendMessage(VelocityLang.COMMAND_NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }

                            context.getSource().sendMessage(VelocityLang.PARTY_USAGE_KICK.build());
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    if(!(context.getSource() instanceof Player player)) return builder.buildFuture();

                                    try {
                                        Party party = partyService.find(player).orElse(null);
                                        if(party == null) {
                                            builder.suggest("You aren't in a party!");
                                            return builder.buildFuture();
                                        }

                                        party.players().forEach(partyMember -> {
                                            if(partyMember.equals(player)) return;
                                            builder.suggest(partyMember.getUsername());
                                        });

                                        return builder.buildFuture();
                                    } catch (Exception ignored) {}

                                    builder.suggest("Searching for players...");
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

                                    Party party = partyService.find(player).orElse(null);
                                    if(party == null) return closeMessage(player, Component.text("You aren't in a party!", NamedTextColor.RED));

                                    if(partyService.getSettings().onlyLeaderCanKick())
                                        if(!party.getLeader().equals(player))
                                            return closeMessage(player, Component.text("Only the party leader can kick people!", NamedTextColor.RED));

                                    String username = context.getArgument("username", String.class);
                                    Player targetPlayer = api.getServer().getPlayer(username).orElse(null);
                                    if(targetPlayer == null)
                                        return closeMessage(player, Component.text(username + " hasn't played on the server!", NamedTextColor.RED));
                                    if(!party.contains(targetPlayer))
                                        return closeMessage(player, Component.text(username + " isn't in your party!", NamedTextColor.RED));

                                    party.leave(targetPlayer);
                                    targetPlayer.sendMessage(Component.text("You were kicked from your party.",NamedTextColor.YELLOW));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .build();

        // BrigadierCommand implements Command
        return new BrigadierCommand(tpa);
    }

    public static int closeMessage(Player player, Component message) {
        player.sendMessage(message);
        return Command.SINGLE_SUCCESS;
    }
}