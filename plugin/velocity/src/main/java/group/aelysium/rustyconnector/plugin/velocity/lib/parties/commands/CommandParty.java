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
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.Permission;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
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
    public static BrigadierCommand create(PartyService partyService) {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();

        LiteralCommandNode<CommandSource> partyCommand = LiteralArgumentBuilder
                .<CommandSource>literal("party")
                .requires(source -> source instanceof Player)
                .executes(context -> {
                    if(!(context.getSource() instanceof Player player)) {
                        logger.log("/party must be sent as a player!");
                        return Command.SINGLE_SUCCESS;
                    }

                    if(!Permission.validate(player, "rustyconnector.command.party")) {
                        player.sendMessage(VelocityLang.NO_PERMISSION);
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
                                player.sendMessage(VelocityLang.NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }

                            context.getSource().sendMessage(VelocityLang.PARTY_USAGE_INVITES);
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
                                            builder.suggest(invite.sender().username());
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
                                        player.sendMessage(VelocityLang.NO_PERMISSION);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    context.getSource().sendMessage(VelocityLang.PARTY_USAGE_INVITES);
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(LiteralArgumentBuilder.<CommandSource>literal("ignore")
                                        .executes(context -> {
                                            if(!(context.getSource() instanceof Player player)) {
                                                logger.log("/party must be sent as a player!");
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            if(!Permission.validate(player, "rustyconnector.command.party")) {
                                                player.sendMessage(VelocityLang.NO_PERMISSION);
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            String username = context.getArgument("username", String.class);
                                            Player senderPlayer = api.velocityServer().getPlayer(username).orElse(null);

                                            if(senderPlayer == null)
                                                return closeMessage(player, VelocityLang.NO_PLAYER.build(username));

                                            try {
                                                PartyInvite invite = partyService.findInvite(player, senderPlayer).orElse(null);
                                                if(invite == null) throw new NoOutputException();

                                                try {
                                                    invite.ignore();
                                                } catch (Exception ignore) {
                                                    partyService.closeInvite(invite);
                                                }
                                            } catch (Exception ignore) {
                                                return closeMessage(player, VelocityLang.INTERNAL_ERROR);
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
                                                player.sendMessage(VelocityLang.NO_PERMISSION);
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            if(partyService.find(player).orElse(null) != null)
                                                return closeMessage(player, VelocityLang.PARTY_INVITE_NO_DOUBLE_DIPPING);

                                            String username = context.getArgument("username", String.class);
                                            Player senderPlayer = api.velocityServer().getPlayer(username).orElse(null);
                                            if(senderPlayer == null || !senderPlayer.isActive())
                                                return closeMessage(player, VelocityLang.PARTY_INVITE_TARGET_NOT_ONLINE.build(username));

                                            PartyInvite invite = partyService.findInvite(player, senderPlayer).orElse(null);
                                            if(invite == null)
                                                return closeMessage(player, VelocityLang.PARTY_INVITE_EXPIRED);

                                            try {
                                                invite.accept();
                                            } catch (IllegalStateException e) {
                                                return closeMessage(player, Component.text(e.getMessage(), NamedTextColor.RED));
                                            } catch (Exception ignore) {
                                                return closeMessage(player, VelocityLang.INTERNAL_ERROR);
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
                                player.sendMessage(VelocityLang.NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }

                            if(partyService.find(player).orElse(null) != null)
                                return closeMessage(player, VelocityLang.PARTY_CREATE_ALREADY_IN_PARTY);

                            if(player.getCurrentServer().orElse(null) == null)
                                return closeMessage(player, VelocityLang.PARTY_CREATE_NO_SERVER);

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
                                player.sendMessage(VelocityLang.NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }

                            Party party = partyService.find(player).orElse(null);
                            if(party == null) return closeMessage(player, VelocityLang.NO_PARTY);

                            if(!party.leader().equals(player))
                                return closeMessage(player, VelocityLang.PARTY_ONLY_LEADER_CAN_DISBAND);

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
                                player.sendMessage(VelocityLang.NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }

                            Party party = partyService.find(player).orElse(null);
                            if(party == null) return closeMessage(player, VelocityLang.NO_PARTY);

                            party.leave(player);

                            return closeMessage(player, VelocityLang.PARTY_LEFT_SELF);
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("invite")
                        .executes(context -> {
                            if(!(context.getSource() instanceof Player player)) {
                                logger.log("/party must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            if(!Permission.validate(player, "rustyconnector.command.party")) {
                                player.sendMessage(VelocityLang.NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }

                            context.getSource().sendMessage(VelocityLang.PARTY_USAGE_INVITE);
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
                                        List<FakePlayer> friends = friendsService.findFriends(player).orElseThrow();
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
                                        player.sendMessage(VelocityLang.NO_PERMISSION);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    Party party = partyService.find(player).orElse(null);
                                    if(party == null) {
                                        if(player.getCurrentServer().orElse(null) == null)
                                            return closeMessage(player, VelocityLang.PARTY_CREATE_NO_SERVER);

                                        PlayerServer server = api.services().serverService().search(player.getCurrentServer().orElse(null).getServerInfo());
                                        Party newParty = partyService.create(player, server);
                                        player.sendMessage(VelocityLang.PARTY_CREATED);

                                        party = newParty;
                                    }

                                    if(partyService.settings().onlyLeaderCanInvite())
                                        if(!party.leader().equals(player))
                                            return closeMessage(player, VelocityLang.PARTY_INVITE_ONLY_LEADER_CAN_SEND);

                                    String username = context.getArgument("username", String.class);
                                    FakePlayer targetPlayerResolvable = api.services().playerService().fetch(username).orElse(null);
                                    if(targetPlayerResolvable == null || targetPlayerResolvable.resolve().isEmpty())
                                        return closeMessage(player, VelocityLang.NO_PLAYER.build(username));

                                    Player targetPlayer = targetPlayerResolvable.resolve().orElseThrow();

                                    try {

                                        Collection<Player> connectedPlayers = targetPlayer.getCurrentServer().orElseThrow().getServer().getPlayersConnected();
                                        if (partyService.settings().localOnly())
                                            if (!connectedPlayers.contains(targetPlayer))
                                                return closeMessage(player, VelocityLang.PARTY_INVITE_NOT_ONLINE);
                                    } catch (Exception ignore) {}
                                    try {
                                        if (partyService.settings().friendsOnly())
                                            if (!api.services().friendsService().orElseThrow().areFriends(
                                                    FakePlayer.from(player),
                                                    FakePlayer.from(targetPlayer)
                                            ))
                                                return closeMessage(player, VelocityLang.PARTY_INVITE_FRIENDS_ONLY);
                                    } catch (Exception ignore) {}
                                    if(targetPlayer.equals(player))
                                        return closeMessage(player, VelocityLang.PARTY_INVITE_SELF_INVITE);
                                    if(party.contains(targetPlayer))
                                        return closeMessage(player, VelocityLang.PARTY_INVITE_ALREADY_A_MEMBER.build(targetPlayer.getUsername()));

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
                                player.sendMessage(VelocityLang.NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }

                            context.getSource().sendMessage(VelocityLang.PARTY_USAGE_KICK);
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
                                        player.sendMessage(VelocityLang.NO_PERMISSION);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    Party party = partyService.find(player).orElse(null);
                                    if(party == null) return closeMessage(player, VelocityLang.NO_PARTY);

                                    if(partyService.settings().onlyLeaderCanKick())
                                        if(!party.leader().equals(player))
                                            return closeMessage(player, VelocityLang.PARTY_ONLY_LEADER_CAN_KICK);

                                    String username = context.getArgument("username", String.class);
                                    Player targetPlayer = api.velocityServer().getPlayer(username).orElse(null);
                                    if(targetPlayer == null)
                                        return closeMessage(player, VelocityLang.NO_PLAYER.build(username));
                                    if(targetPlayer.equals(player))
                                        return closeMessage(player, VelocityLang.PARTY_SELF_KICK);
                                    if(!party.contains(targetPlayer))
                                        return closeMessage(player, VelocityLang.PARTY_NO_MEMBER.build(username));

                                    party.leave(targetPlayer);

                                    context.getSource().sendMessage(VelocityLang.PARTY_BOARD.build(party, player));
                                    targetPlayer.sendMessage(VelocityLang.PARTY_KICKED);
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
                                player.sendMessage(VelocityLang.NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }

                            context.getSource().sendMessage(VelocityLang.PARTY_USAGE_PROMOTE);
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
                                        player.sendMessage(VelocityLang.NO_PERMISSION);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    Party party = partyService.find(player).orElse(null);
                                    if(party == null) return closeMessage(player, VelocityLang.NO_PARTY);

                                    if(partyService.settings().onlyLeaderCanKick())
                                        if(!party.leader().equals(player))
                                            return closeMessage(player, VelocityLang.PARTY_ONLY_LEADER_CAN_PROMOTE);

                                    String username = context.getArgument("username", String.class);
                                    Player targetPlayer = api.velocityServer().getPlayer(username).orElse(null);
                                    if(targetPlayer == null)
                                        return closeMessage(player, VelocityLang.NO_PLAYER.build(username));
                                    if(targetPlayer.equals(player))
                                        return closeMessage(player, VelocityLang.PARTY_ALREADY_LEADER);
                                    if(!party.contains(targetPlayer))
                                        return closeMessage(player, VelocityLang.PARTY_NO_MEMBER.build(username));

                                    try {
                                        party.setLeader(targetPlayer);
                                        targetPlayer.sendMessage(VelocityLang.PARTY_PROMOTED);
                                        player.sendMessage(VelocityLang.PARTY_DEMOTED);
                                        party.players().forEach(partyMember -> {
                                            if(partyMember.equals(player)) return;
                                            if(partyMember.equals(targetPlayer)) return;

                                            partyMember.sendMessage(VelocityLang.PARTY_STATUS_PROMOTED.build(targetPlayer));
                                        });

                                        context.getSource().sendMessage(VelocityLang.PARTY_BOARD.build(party, player));
                                    } catch (IllegalStateException e) {
                                        return closeMessage(player, VelocityLang.PARTY_NO_MEMBER.build(targetPlayer.getUsername()));
                                    } catch (Exception e) {
                                        return closeMessage(player, VelocityLang.INTERNAL_ERROR);
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