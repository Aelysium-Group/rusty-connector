package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.connection.ConnectionResult;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.Permission;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors.AnchorService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import net.kyori.adventure.text.Component;

import java.util.concurrent.TimeUnit;

public class CommandAnchor {
    public static BrigadierCommand create(DependencyInjector.DI3<DynamicTeleportService, ServerService, AnchorService> dependencies, String anchor) {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();

        AnchorService anchorService = dependencies.d3();


        LiteralCommandNode<CommandSource> anchorCommand = LiteralArgumentBuilder
                .<CommandSource>literal(anchor)
                .requires(source -> source instanceof com.velocitypowered.api.proxy.Player)
                .executes(context -> {
                    if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player velocityPlayer)) {
                        logger.log("/"+anchor+" must be sent as a player!");
                        return Command.SINGLE_SUCCESS;
                    }
                    if(!Permission.validate(velocityPlayer, "rustyconnector.command.anchor", "rustyconnector.command.anchor."+anchor)) {
                        velocityPlayer.sendMessage(ProxyLang.NO_PERMISSION);
                        return Command.SINGLE_SUCCESS;
                    }

                    try {
                        Player player = new Player(velocityPlayer);

                        IFamily family = anchorService.familyOf(anchor).orElseThrow();

                        // If the attempt to check player's family fails, just ignore it and try to connect.
                        // If there's actually an issue it'll be caught further down.
                        try {
                            if(family.equals(player.server().orElseThrow().family()))
                                return closeMessage(velocityPlayer, ProxyLang.SERVER_ALREADY_CONNECTED);
                        } catch (Exception ignore) {}

                        ConnectionResult result = family.connect(player).result().get(50, TimeUnit.SECONDS);

                        if(!result.connected()) return closeMessage(player.resolve().orElseThrow(), result.message());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return closeMessage(velocityPlayer, ProxyLang.INTERNAL_ERROR);
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .build();

        return new BrigadierCommand(anchorCommand);
    }

    public static int closeMessage(com.velocitypowered.api.proxy.Player player, Component message) {
        player.sendMessage(message);
        return Command.SINGLE_SUCCESS;
    }
}