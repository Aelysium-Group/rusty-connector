package group.aelysium.rustyconnector.plugin.velocity.lib.parties.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.Permission;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.Party;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.PartyInvite;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.PartyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.FakePlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Collection;
import java.util.List;

public final class CommandParty {
    public static BrigadierCommand create() {
        VelocityAPI api = VelocityAPI.get();
        PluginLogger logger = api.logger();

        // If this command class loads, then PartyService MUST be set.
        PartyService partyService = api.services().partyService().orElseThrow();

        LiteralCommandNode<CommandSource> partyCommand = LiteralArgumentBuilder
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

                    context.getSource().sendMessage(VelocityLang.PARTY_BOARD.build(party, player));
                    return Command.SINGLE_SUCCESS;
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
                                            builder.suggest(invite.sender().getUsername());
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
                                .then(LiteralArgumentBuilder.<CommandSource>literal("ignore")
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
                                            Player senderPlayer = api.velocityServer().getPlayer(username).orElse(null);

                                            if(senderPlayer == null)
                                                return closeMessage(player, Component.text(username + " doesn't seem to exist on this server!", NamedTextColor.RED));

                                            try {
                                                PartyInvite invite = partyService.findInvite(player, senderPlayer).orElse(null);
                                                if(invite == null) throw new NoOutputException();

                                                try {
                                                    invite.ignore();
                                                } catch (Exception ignore) {
                                                    partyService.closeInvite(invite);
                                                }
                                            } catch (Exception ignore) {
                                                return closeMessage(player, Component.text("There was an issue ignoring that invite!", NamedTextColor.RED));
                                            }
                                            return Command.SINGLE_SUCCESS;
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

                                            if(partyService.find(player).orElse(null) != null)
                                                return closeMessage(player, Component.text("You must leave your current party before joining another party.", NamedTextColor.RED));

                                            String username = context.getArgument("username", String.class);
                                            Player senderPlayer = api.velocityServer().getPlayer(username).orElse(null);
                                            if(senderPlayer == null || !senderPlayer.isActive())
                                                return closeMessage(player, Component.text(username + " isn't online for you to join their party!", NamedTextColor.RED));

                                            PartyInvite invite = partyService.findInvite(player, senderPlayer).orElse(null);
                                            if(invite == null)
                                                return closeMessage(player, Component.text("The invite from " + username + " has expired!", NamedTextColor.RED));

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

                            if(partyService.find(player).orElse(null) != null)
                                return closeMessage(player, Component.text("You can't start a party if you're already in one!", NamedTextColor.RED));

                            if(player.getCurrentServer().orElse(null) == null)
                                return closeMessage(player, Component.text("You have to be connected to a server in order to create a party!", NamedTextColor.RED));

                            PlayerServer server = api.services().serverService().search(player.getCurrentServer().orElse(null).getServerInfo());
                            Party party = partyService.create(player, server);

                            context.getSource().sendMessage(VelocityLang.PARTY_BOARD.build(party, player));

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

                            if(!party.leader().equals(player))
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
                                .suggests((context, builder) -> {
                                    if(!(context.getSource() instanceof Player player)) return builder.buildFuture();

                                    try {
                                        if(!partyService.settings().friendsOnly()) {
                                            PlayerServer server = api.services().serverService().search(player.getCurrentServer().orElseThrow().getServerInfo());

                                            server.registeredServer().getPlayersConnected().forEach(nearbyPlayer -> {
                                                if(nearbyPlayer.equals(player)) return;

                                                builder.suggest(nearbyPlayer.getUsername());
                                            });

                                            return builder.buildFuture();
                                        }

                                        FriendsService friendsService = api.services().friendsService().orElseThrow();
                                        List<FakePlayer> friends = friendsService.findFriends(player, false).orElseThrow();
                                        if(friends.size() == 0) {
                                            builder.suggest("You don't have any friends you can invite to your party!");
                                            return builder.buildFuture();
                                        }

                                        friends.forEach(friend -> {
                                            try {
                                                builder.suggest(friend.username());
                                            } catch (Exception ignore) {}
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
                                    if(party == null) {
                                        if(player.getCurrentServer().orElse(null) == null)
                                            return closeMessage(player, Component.text("You have to be connected to a server in order to create a party!", NamedTextColor.RED));

                                        PlayerServer server = api.services().serverService().search(player.getCurrentServer().orElse(null).getServerInfo());
                                        Party newParty = partyService.create(player, server);
                                        player.sendMessage(Component.text("You created a new party!",NamedTextColor.GREEN));

                                        party = newParty;
                                    }

                                    if(partyService.settings().onlyLeaderCanInvite())
                                        if(!party.leader().equals(player))
                                            return closeMessage(player, Component.text("Only the party leader can invite people!", NamedTextColor.RED));

                                    String username = context.getArgument("username", String.class);
                                    Player targetPlayer = api.velocityServer().getPlayer(username).orElse(null);
                                    if(targetPlayer == null || !targetPlayer.isActive())
                                        return closeMessage(player, Component.text(username + " isn't available to send an invite to!", NamedTextColor.RED));
                                    try {
                                        Collection<Player> connectedPlayers = targetPlayer.getCurrentServer().orElseThrow().getServer().getPlayersConnected();
                                        if (partyService.settings().localOnly())
                                            if (!connectedPlayers.contains(targetPlayer))
                                                return closeMessage(player, Component.text("You can only send invites to players that are in the server with you!", NamedTextColor.RED));
                                    } catch (Exception ignore) {}
                                    try {
                                        if (partyService.settings().friendsOnly())
                                            if (!api.services().friendsService().orElseThrow().areFriends(player, targetPlayer))
                                                return closeMessage(player, Component.text("You can only send invites to your friends!", NamedTextColor.RED));
                                    } catch (Exception ignore) {}
                                    if(targetPlayer.equals(player))
                                        return closeMessage(player, Component.text("You can't invite yourself to your own party!", NamedTextColor.RED));
                                    if(party.contains(targetPlayer))
                                        return closeMessage(player, Component.text(targetPlayer.getUsername()+" is already in your party!", NamedTextColor.RED));

                                    try {
                                        partyService.invitePlayer(party, player, targetPlayer);
                                    } catch (IllegalStateException e) {
                                        return closeMessage(player, Component.text(e.getMessage(), NamedTextColor.RED));
                                    }
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

                                    if(partyService.settings().onlyLeaderCanKick())
                                        if(!party.leader().equals(player))
                                            return closeMessage(player, Component.text("Only the party leader can kick people!", NamedTextColor.RED));

                                    String username = context.getArgument("username", String.class);
                                    Player targetPlayer = api.velocityServer().getPlayer(username).orElse(null);
                                    if(targetPlayer == null)
                                        return closeMessage(player, Component.text(username + " hasn't played on the server!", NamedTextColor.RED));
                                    if(targetPlayer.equals(player))
                                        return closeMessage(player, Component.text("You can't kick yourself! Use `/party leave` instead!", NamedTextColor.RED));
                                    if(!party.contains(targetPlayer))
                                        return closeMessage(player, Component.text(username + " isn't in your party!", NamedTextColor.RED));

                                    party.leave(targetPlayer);

                                    context.getSource().sendMessage(VelocityLang.PARTY_BOARD.build(party, player));
                                    targetPlayer.sendMessage(Component.text("You were kicked from your party.",NamedTextColor.YELLOW));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("promote")
                        .executes(context -> {
                            if(!(context.getSource() instanceof Player player)) {
                                logger.log("/party must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            if(!Permission.validate(player, "rustyconnector.command.party")) {
                                player.sendMessage(VelocityLang.COMMAND_NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }

                            context.getSource().sendMessage(VelocityLang.PARTY_USAGE_PROMOTE.build());
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

                                    if(partyService.settings().onlyLeaderCanKick())
                                        if(!party.leader().equals(player))
                                            return closeMessage(player, Component.text("Only the party leader can promote people!", NamedTextColor.RED));

                                    String username = context.getArgument("username", String.class);
                                    Player targetPlayer = api.velocityServer().getPlayer(username).orElse(null);
                                    if(targetPlayer == null)
                                        return closeMessage(player, Component.text(username + " hasn't played on the server!", NamedTextColor.RED));
                                    if(targetPlayer.equals(player))
                                        return closeMessage(player, Component.text("You can't promote yourself! You're already the leader.", NamedTextColor.RED));
                                    if(!party.contains(targetPlayer))
                                        return closeMessage(player, Component.text(username + " isn't in your party!", NamedTextColor.RED));

                                    try {
                                        party.setLeader(targetPlayer);
                                        targetPlayer.sendMessage(Component.text("You were promoted to party leader.",NamedTextColor.YELLOW));
                                        player.sendMessage(Component.text("You are no longer party leader.",NamedTextColor.YELLOW));
                                        party.players().forEach(partyMember -> {
                                            if(partyMember.equals(player)) return;
                                            if(partyMember.equals(targetPlayer)) return;

                                            partyMember.sendMessage(Component.text(targetPlayer.getUsername()+" was promoted to party leader.", NamedTextColor.YELLOW));
                                        });

                                        context.getSource().sendMessage(VelocityLang.PARTY_BOARD.build(party, player));
                                    } catch (Exception e) {
                                        return closeMessage(player, Component.text(username + "There was an issue doing that!", NamedTextColor.RED));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .build();

        // BrigadierCommand implements Command
        return new BrigadierCommand(partyCommand);
    }

    public static int closeMessage(Player player, Component message) {
        player.sendMessage(message);
        return Command.SINGLE_SUCCESS;
    }
}