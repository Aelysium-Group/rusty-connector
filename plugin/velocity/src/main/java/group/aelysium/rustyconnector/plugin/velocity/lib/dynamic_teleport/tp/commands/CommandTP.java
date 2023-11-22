package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tp.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.util.DependencyInjector;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.Permission;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tp.TPService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import net.kyori.adventure.text.Component;

public final class CommandTP {
    public static BrigadierCommand create(DependencyInjector.DI3<FamilyService, ServerService, TPService> dependencies) {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();

        FamilyService familyService = dependencies.d1();
        ServerService serverService = dependencies.d2();
        TPService tpService = dependencies.d3();

        LiteralCommandNode<CommandSource> tp = LiteralArgumentBuilder
                .<CommandSource>literal("tp")
                .requires(source -> source instanceof Player)
                .executes(context -> {
                    if (!(context.getSource() instanceof Player player)) {
                        logger.log("/tp must be sent as a player!");
                        return Command.SINGLE_SUCCESS;
                    }

                    if (!Permission.validate(player, "rustyconnector.command.tp")) {
                        player.sendMessage(VelocityLang.NO_PERMISSION);
                        return Command.SINGLE_SUCCESS;
                    }

                    ((Player) context.getSource()).sendMessage(Component.text("/tp PLAYER"));
                    return Command.SINGLE_SUCCESS;
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            if(!(context.getSource() instanceof Player player)) return builder.buildFuture();

                            try {
                                ServerInfo sendingServer = ((Player) context.getSource()).getCurrentServer().orElseThrow().getServerInfo();

                                String familyName = serverService.search(sendingServer).family().name();
                                BaseServerFamily<?> family = familyService.find(familyName);

                                family.allPlayers(50).forEach(nearbyPlayer -> {
                                    if (nearbyPlayer.equals(player)) return;

                                    builder.suggest((nearbyPlayer.getUsername()));
                                });

                                return builder.buildFuture();
                            } catch (Exception ignored) {}

                            builder.suggest("Searching for players...");
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            if(!(context.getSource() instanceof Player player)) {
                                logger.log("/tp must be sent as a player!");
                                return Command.SINGLE_SUCCESS;
                            }

                            if (!Permission.validate(player, "rustyconnector.command.tp")) {
                                player.sendMessage(VelocityLang.NO_PERMISSION);
                                return Command.SINGLE_SUCCESS;
                            }

                            String username = context.getArgument("username", String.class);

                            try {
                                Player targetPlayer = api.velocityServer().getPlayer(username).orElseThrow();

                                if (player.equals(targetPlayer)) {
                                    player.sendMessage(VelocityLang.TPA_FAILURE_SELF_TP);
                                    return Command.SINGLE_SUCCESS;
                                }

                                ServerInfo recieverServerInfo = targetPlayer.getCurrentServer().orElseThrow().getServerInfo();
                                PlayerServer targetServer = serverService.search(recieverServerInfo);

                                tpService.tpaSendPlayer(player, targetPlayer, targetServer);

                                return Command.SINGLE_SUCCESS;
                            } catch (Exception ignored) {}

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .build();

        return new BrigadierCommand(tp);
    }
}
