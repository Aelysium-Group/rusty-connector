package group.aelysium.rustyconnector.plugin.velocity.lib.parties.commands;

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
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.Party;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.PartyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.parties.IParty;
import group.aelysium.rustyconnector.toolkit.velocity.parties.IPartyInvite;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class CommandParty {
    public static BrigadierCommand create(PartyService partyService) {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();

        LiteralCommandNode<CommandSource> partyCommand = LiteralArgumentBuilder
                .<CommandSource>literal("party")
                .requires(source -> source instanceof com.velocitypowered.api.proxy.Player)
                .executes(context -> {
                    if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) {
                        logger.log("/party must be sent as a player!");
                        return Command.SINGLE_SUCCESS;
                    }

                    Player player = new Player(velocityPlayer);

                    if(!Permission.validate(velocityPlayer, "rustyconnector.command.party")) return closeMessage(player, ProxyLang.NO_PERMISSION);

                    IParty party = partyService.find(player).orElse(null);

                    context.getSource().sendMessage(ProxyLang.PARTY_BOARD.build(party, player));
                    return Command.SINGLE_SUCCESS;
                })
                .then(LiteralArgumentBuilder.<CommandSource>literal("invites")
                        .executes(context -> {
                            if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) {
                                logger.log("/party must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            Player player = new Player(velocityPlayer);

                            if(!Permission.validate(velocityPlayer, "rustyconnector.command.party")) return closeMessage(player, ProxyLang.NO_PERMISSION);

                            velocityPlayer.sendMessage(ProxyLang.PARTY_USAGE_INVITES);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) return builder.buildFuture();

                                    try {
                                        Player player = new Player(velocityPlayer);

                                        List<IPartyInvite> invites = partyService.findInvitesToTarget(player);

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
                                    if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) {
                                        logger.log("/party must be sent as a player!");
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    if(!Permission.validate(velocityPlayer, "rustyconnector.command.party")) {
                                        velocityPlayer.sendMessage(ProxyLang.NO_PERMISSION);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    velocityPlayer.sendMessage(ProxyLang.PARTY_USAGE_INVITES);
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(LiteralArgumentBuilder.<CommandSource>literal("ignore")
                                        .executes(context -> {
                                            if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) {
                                                logger.log("/party must be sent as a player!");
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            Player player = new Player(velocityPlayer);

                                            if(!Permission.validate(velocityPlayer, "rustyconnector.command.party")) return closeMessage(player, ProxyLang.NO_PERMISSION);

                                            String username = context.getArgument("username", String.class);
                                            com.velocitypowered.api.proxy.Player senderVelocityPlayer = api.velocityServer().getPlayer(username).orElse(null);
                                            if(senderVelocityPlayer == null)
                                                return closeMessage(player, ProxyLang.NO_PLAYER.build(username));

                                            Player sender = new Player(senderVelocityPlayer);

                                            IPartyInvite invite = partyService.findInvite(player, sender).orElse(null);
                                            if(invite == null) return closeMessage(player, ProxyLang.PARTY_NO_INVITE.build(sender.username()));

                                            try {
                                                invite.ignore();
                                            } catch (Exception ignore) {
                                                partyService.closeInvite(invite);
                                            }
                                            return closeMessage(player, ProxyLang.PARTY_IGNORE_INVITE.build(sender.username()));
                                        })
                                )
                                .then(LiteralArgumentBuilder.<CommandSource>literal("accept")
                                        .executes(context -> {
                                            if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) {
                                                logger.log("/party must be sent as a player!");
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            Player player = new Player(velocityPlayer);

                                            if(!Permission.validate(velocityPlayer, "rustyconnector.command.party")) return closeMessage(player, ProxyLang.NO_PERMISSION);

                                            if(partyService.find(player).orElse(null) != null)
                                                return closeMessage(player, ProxyLang.PARTY_INVITE_NO_DOUBLE_DIPPING);

                                            String username = context.getArgument("username", String.class);
                                            com.velocitypowered.api.proxy.Player senderPlayer = api.velocityServer().getPlayer(username).orElse(null);
                                            if(senderPlayer == null || !senderPlayer.isActive())
                                                return closeMessage(player, ProxyLang.PARTY_INVITE_TARGET_NOT_ONLINE.build(username));
                                            Player sender = new Player(senderPlayer);

                                            IPartyInvite invite = partyService.findInvite(player, sender).orElse(null);
                                            if(invite == null)
                                                return closeMessage(player, ProxyLang.PARTY_INVITE_EXPIRED);

                                            try {
                                                invite.accept();
                                            } catch (IllegalStateException e) {
                                                return closeMessage(player, Component.text(e.getMessage(), NamedTextColor.RED));
                                            } catch (Exception ignore) {
                                                return closeMessage(player, ProxyLang.INTERNAL_ERROR);
                                            }

                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("create")
                        .executes(context -> {
                            if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) {
                                logger.log("/party must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            Player player = new Player(velocityPlayer);

                            if(!Permission.validate(velocityPlayer, "rustyconnector.command.party")) return closeMessage(player, ProxyLang.NO_PERMISSION);

                            if(partyService.find(player).isPresent()) return closeMessage(player, ProxyLang.PARTY_CREATE_ALREADY_IN_PARTY);
                            if(player.server().isEmpty()) return closeMessage(player, ProxyLang.PARTY_CREATE_NO_SERVER);

                            Party party = partyService.create(player, player.server().orElseThrow());

                            context.getSource().sendMessage(ProxyLang.PARTY_BOARD.build(party, player));

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("disband")
                        .executes(context -> {
                            if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) {
                                logger.log("/party must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            Player player = new Player(velocityPlayer);

                            if(!Permission.validate(velocityPlayer, "rustyconnector.command.party")) return closeMessage(player, ProxyLang.NO_PERMISSION);

                            IParty party = partyService.find(player).orElse(null);
                            if(party == null) return closeMessage(player, ProxyLang.NO_PARTY);

                            if(!party.leader().equals(player))
                                return closeMessage(player, ProxyLang.PARTY_ONLY_LEADER_CAN_DISBAND);

                            partyService.disband(party);
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("leave")
                        .executes(context -> {
                            if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) {
                                logger.log("/party must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            Player player = new Player(velocityPlayer);

                            if(!Permission.validate(velocityPlayer, "rustyconnector.command.party")) return closeMessage(player, ProxyLang.NO_PERMISSION);

                            IParty party = partyService.find(player).orElse(null);
                            if(party == null) return closeMessage(player, ProxyLang.NO_PARTY);

                            party.leave(player);

                            return closeMessage(player, ProxyLang.PARTY_LEFT_SELF);
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("invite")
                        .executes(context -> {
                            if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) {
                                logger.log("/party must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            Player player = new Player(velocityPlayer);

                            if(!Permission.validate(velocityPlayer, "rustyconnector.command.party")) return closeMessage(player, ProxyLang.NO_PERMISSION);

                            context.getSource().sendMessage(ProxyLang.PARTY_USAGE_INVITE);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) return builder.buildFuture();
                                    Player player = new Player(velocityPlayer);

                                    try {
                                        if(!partyService.settings().friendsOnly()) {
                                            player.server().orElseThrow().registeredServer().getPlayersConnected().forEach(nearbyPlayer -> {
                                                if(nearbyPlayer.getUniqueId().equals(player.uuid())) return;

                                                builder.suggest(nearbyPlayer.getUsername());
                                            });

                                            return builder.buildFuture();
                                        }

                                        FriendsService friendsService = api.services().friends().orElseThrow();
                                        List<IPlayer> friends = friendsService.friendStorage().get(player).orElseThrow();
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
                                    if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) {
                                        logger.log("/party must be sent as a player!");
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    Player player = new Player(velocityPlayer);

                                    if(!Permission.validate(velocityPlayer, "rustyconnector.command.party")) return closeMessage(player, ProxyLang.NO_PERMISSION);

                                    IParty party = partyService.find(player).orElse(null);
                                    if(party == null) {
                                        if(player.server().isEmpty()) return closeMessage(player, ProxyLang.PARTY_CREATE_NO_SERVER);

                                        Party newParty = partyService.create(player, player.server().orElseThrow());
                                        velocityPlayer.sendMessage(ProxyLang.PARTY_CREATED);

                                        party = newParty;
                                    }

                                    String username = context.getArgument("username", String.class);
                                    Player target = new IPlayer.UsernameReference(username).get();

                                    if(!target.online())
                                        return closeMessage(player, ProxyLang.NO_PLAYER.build(username));
                                    if(target.equals(player))
                                        return closeMessage(player, ProxyLang.PARTY_INVITE_SELF_INVITE);
                                    if(party.contains(target))
                                        return closeMessage(player, ProxyLang.PARTY_INVITE_ALREADY_A_MEMBER.build(target.username()));

                                    try {
                                        Collection<com.velocitypowered.api.proxy.Player> connectedPlayers = target.resolve().orElseThrow().getCurrentServer().orElseThrow().getServer().getPlayersConnected();
                                        if (partyService.settings().localOnly())
                                            if (!connectedPlayers.contains(player.resolve().orElseThrow()))
                                                return closeMessage(player, ProxyLang.PARTY_INVITE_NOT_ONLINE);
                                    } catch (Exception ignore) {}
                                    try {
                                        if (partyService.settings().friendsOnly()) {
                                            Optional<Boolean> contains = api.services().friends().orElseThrow().friendStorage().contains(player, target);
                                            if(contains.isEmpty())
                                                return closeMessage(player, ProxyLang.INTERNAL_ERROR);
                                            if(!contains.get())
                                                return closeMessage(player, ProxyLang.PARTY_INVITE_FRIENDS_ONLY);
                                        }
                                    } catch (Exception ignore) {}

                                    try {
                                        partyService.invitePlayer(party, player, target);
                                    } catch (IllegalStateException e) {
                                        return closeMessage(player, Component.text(e.getMessage(), NamedTextColor.RED));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("kick")
                        .executes(context -> {
                            if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) {
                                logger.log("/party must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            Player player = new Player(velocityPlayer);

                            if(!Permission.validate(velocityPlayer, "rustyconnector.command.party")) return closeMessage(player, ProxyLang.NO_PERMISSION);

                            context.getSource().sendMessage(ProxyLang.PARTY_USAGE_KICK);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) return builder.buildFuture();

                                    Player player = new Player(velocityPlayer);

                                    try {
                                        IParty party = partyService.find(player).orElse(null);
                                        if(party == null) {
                                            builder.suggest("You aren't in a party!");
                                            return builder.buildFuture();
                                        }

                                        party.players().forEach(partyMember -> {
                                            if(partyMember.equals(player)) return;
                                            builder.suggest(partyMember.username());
                                        });

                                        return builder.buildFuture();
                                    } catch (Exception ignored) {}

                                    builder.suggest("Searching for players...");
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) {
                                        logger.log("/party must be sent as a player!");
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    Player player = new Player(velocityPlayer);

                                    if(!Permission.validate(velocityPlayer, "rustyconnector.command.party")) return closeMessage(player, ProxyLang.NO_PERMISSION);

                                    IParty party = partyService.find(player).orElse(null);
                                    if(party == null) return closeMessage(player, ProxyLang.NO_PARTY);

                                    if(partyService.settings().onlyLeaderCanKick())
                                        if(!party.leader().equals(player))
                                            return closeMessage(player, ProxyLang.PARTY_ONLY_LEADER_CAN_KICK);

                                    String username = context.getArgument("username", String.class);
                                    com.velocitypowered.api.proxy.Player targetPlayer = api.velocityServer().getPlayer(username).orElse(null);
                                    if(targetPlayer == null)
                                        return closeMessage(player, ProxyLang.NO_PLAYER.build(username));

                                    Player target = new Player(targetPlayer);

                                    if(target.equals(player))
                                        return closeMessage(player, ProxyLang.PARTY_SELF_KICK);
                                    if(!party.contains(target))
                                        return closeMessage(player, ProxyLang.PARTY_NO_MEMBER.build(username));

                                    party.leave(target);

                                    context.getSource().sendMessage(ProxyLang.PARTY_BOARD.build(party, player));
                                    targetPlayer.sendMessage(ProxyLang.PARTY_KICKED);
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("promote")
                        .executes(context -> {
                            if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) {
                                logger.log("/party must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            Player player = new Player(velocityPlayer);

                            if(!Permission.validate(velocityPlayer, "rustyconnector.command.party")) return closeMessage(player, ProxyLang.NO_PERMISSION);

                            context.getSource().sendMessage(ProxyLang.PARTY_USAGE_PROMOTE);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) return builder.buildFuture();

                                    Player player = new Player(velocityPlayer);

                                    try {
                                        IParty party = partyService.find(player).orElse(null);
                                        if(party == null) {
                                            builder.suggest("You aren't in a party!");
                                            return builder.buildFuture();
                                        }

                                        party.players().forEach(partyMember -> {
                                            if(partyMember.equals(player)) return;
                                            builder.suggest(partyMember.username());
                                        });

                                        return builder.buildFuture();
                                    } catch (Exception ignored) {}

                                    builder.suggest("Searching for players...");
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) {
                                        logger.log("/party must be sent as a player!");
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    Player player = new Player(velocityPlayer);

                                    if(!Permission.validate(velocityPlayer, "rustyconnector.command.party")) return closeMessage(player, ProxyLang.NO_PERMISSION);

                                    IParty party = partyService.find(player).orElse(null);
                                    if(party == null) return closeMessage(player, ProxyLang.NO_PARTY);

                                    if(!party.leader().equals(player)) return closeMessage(player, ProxyLang.PARTY_ONLY_LEADER_CAN_PROMOTE);

                                    String username = context.getArgument("username", String.class);
                                    com.velocitypowered.api.proxy.Player targetPlayer = api.velocityServer().getPlayer(username).orElse(null);
                                    if(targetPlayer == null) return closeMessage(player, ProxyLang.NO_PLAYER.build(username));

                                    Player target = new Player(targetPlayer);

                                    if(target.equals(player))
                                        return closeMessage(player, ProxyLang.PARTY_ALREADY_LEADER);
                                    if(!party.contains(target))
                                        return closeMessage(player, ProxyLang.PARTY_NO_MEMBER.build(username));

                                    try {
                                        party.setLeader(target);
                                        targetPlayer.sendMessage(ProxyLang.PARTY_PROMOTED);
                                        velocityPlayer.sendMessage(ProxyLang.PARTY_DEMOTED);
                                        party.players().forEach(partyMember -> {
                                            if(partyMember.equals(player)) return;
                                            if(partyMember.equals(target)) return;

                                            partyMember.sendMessage(ProxyLang.PARTY_STATUS_PROMOTED.build(targetPlayer));
                                        });

                                        velocityPlayer.sendMessage(ProxyLang.PARTY_BOARD.build(party, player));
                                    } catch (IllegalStateException e) {
                                        e.printStackTrace();
                                        return closeMessage(player, ProxyLang.PARTY_NO_MEMBER.build(targetPlayer.getUsername()));
                                    } catch (Exception e) {
                                        return closeMessage(player, ProxyLang.INTERNAL_ERROR);
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