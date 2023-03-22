package group.aelysium.rustyconnector.plugin.velocity.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.VelocityBrigadierMessage;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.PaperServerLoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PaperServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.ServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.tpa.TPARequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.NoSuchElementException;

public final class CommandTPA {
    public static BrigadierCommand create() {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        LiteralCommandNode<CommandSource> tpa = LiteralArgumentBuilder
                .<CommandSource>literal("tpa")
                .requires(source -> source instanceof Player)
                .executes(context -> 0)
                .then(LiteralArgumentBuilder.<CommandSource>literal("deny")
                        .executes(context -> Command.SINGLE_SUCCESS)
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                                .executes(context -> {
                                    if(!(context.getSource() instanceof Player)) {
                                        plugin.logger().log("/tpa must be sent as a player!");
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    String username = context.getArgument("username", String.class);

                                    try {
                                        Player senderPlayer = plugin.getVelocityServer().getPlayer(username).orElseThrow();
                                        ServerInfo targetServerInfo = ((Player) context.getSource()).getCurrentServer().orElseThrow().getServerInfo();

                                        PaperServer targetServer = plugin.getProxy().findServer(targetServerInfo);
                                        String familyName = targetServer.getFamilyName();
                                        try {
                                            ServerFamily<? extends PaperServerLoadBalancer> family = plugin.getProxy().getFamilyManager().find(familyName);
                                            if(family == null) throw new NullPointerException();
                                            if(!family.getTPAHandler().getSettings().isEnabled()) throw new RuntimeException();

                                            try {
                                                TPARequest request = family.getTPAHandler().findRequest(senderPlayer, (Player) context.getSource());
                                                if(request == null) throw new NullPointerException();

                                                request.deny();
                                                family.getTPAHandler().remove(request);
                                                return Command.SINGLE_SUCCESS;
                                            } catch (NullPointerException ignore) {}
                                        } catch (NullPointerException e) {
                                            plugin.logger().send(Component.text("Player attempted to use /tpa deny from a family that doesn't exist! (How?)", NamedTextColor.RED));
                                            context.getSource().sendMessage(VelocityLang.TPA_FAILURE.build(username));
                                        } catch (RuntimeException e) {
                                            context.getSource().sendMessage(VelocityLang.TPA_NOT_ENABLED.build());
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
                        .executes(context -> Command.SINGLE_SUCCESS)
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                                .executes(context -> {
                                    if(!(context.getSource() instanceof Player)) {
                                        plugin.logger().log("/tpa must be sent as a player!");
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    String username = context.getArgument("username", String.class);

                                    try {
                                        Player senderPlayer = plugin.getVelocityServer().getPlayer(username).orElseThrow();
                                        ServerInfo targetServerInfo = ((Player) context.getSource()).getCurrentServer().orElseThrow().getServerInfo();

                                        PaperServer targetServer = plugin.getProxy().findServer(targetServerInfo);
                                        String familyName = targetServer.getFamilyName();
                                        try {
                                            ServerFamily<? extends PaperServerLoadBalancer> family = plugin.getProxy().getFamilyManager().find(familyName);
                                            if(family == null) throw new NullPointerException();
                                            if(!family.getTPAHandler().getSettings().isEnabled()) throw new RuntimeException();

                                            try {
                                                TPARequest request = family.getTPAHandler().findRequest(senderPlayer, (Player) context.getSource());
                                                if(request == null) throw new NullPointerException();

                                                request.accept();
                                                context.getSource().sendMessage(VelocityLang.TPA_COMPLETE.build());
                                                return Command.SINGLE_SUCCESS;
                                            } catch (NullPointerException ignore) {}
                                        } catch (NullPointerException e) {
                                            plugin.logger().send(Component.text("Player attempted to use /tpa accept from a family that doesn't exist! (How?)", NamedTextColor.RED));
                                            context.getSource().sendMessage(VelocityLang.TPA_FAILURE.build(username));
                                        } catch (RuntimeException e) {
                                            context.getSource().sendMessage(VelocityLang.TPA_NOT_ENABLED.build());
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

                                String familyName = plugin.getProxy().findServer(sendingServer).getFamilyName();
                                ServerFamily<? extends PaperServerLoadBalancer> family = plugin.getProxy().getFamilyManager().find(familyName);

                                family.getAllPlayers(100).forEach(player -> builder.suggest(
                                        player.getUsername(),
                                        VelocityBrigadierMessage.tooltip(
                                                Component.text(player.getUsername())
                                        )
                                ));

                                return builder.buildFuture();
                            } catch (Exception ignored) {}

                            builder.suggest("Searching for players...");
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            if(!(context.getSource() instanceof Player)) {
                                plugin.logger().log("/tpa must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            String username = context.getArgument("username", String.class);

                            try {
                                Player targetPlayer = plugin.getVelocityServer().getPlayer(username).orElseThrow();
                                ServerInfo sendersServerInfo = ((Player) context.getSource()).getCurrentServer().orElseThrow().getServerInfo();

                                PaperServer sendersServer = plugin.getProxy().findServer(sendersServerInfo);
                                String familyName = sendersServer.getFamilyName();
                                try {
                                    ServerFamily<? extends PaperServerLoadBalancer> family = plugin.getProxy().getFamilyManager().find(familyName);
                                    if(family == null) throw new NullPointerException();
                                    if(!family.getTPAHandler().getSettings().isEnabled()) throw new RuntimeException();

                                    TPARequest request = family.getTPAHandler().newRequest((Player) context.getSource(), targetPlayer);
                                    request.submit();
                                    context.getSource().sendMessage(VelocityLang.TPA_REQUEST_SUBMISSION.build(username));

                                    return Command.SINGLE_SUCCESS;
                                } catch (NullPointerException e) {
                                    plugin.logger().send(Component.text("Player attempted to use /tpa from a family that doesn't exist! (How?)", NamedTextColor.RED));
                                    context.getSource().sendMessage(VelocityLang.TPA_FAILURE.build(username));
                                } catch (RuntimeException e) {
                                    context.getSource().sendMessage(VelocityLang.TPA_NOT_ENABLED.build());
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