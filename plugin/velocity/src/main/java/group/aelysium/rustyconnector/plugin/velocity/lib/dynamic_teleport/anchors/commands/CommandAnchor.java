package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.util.DependencyInjector;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.Permission;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors.AnchorService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import net.kyori.adventure.text.Component;

import java.util.Objects;

public class CommandAnchor {
    public static BrigadierCommand create(DependencyInjector.DI3<DynamicTeleportService, ServerService, AnchorService> dependencies, String anchor) {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();

        DynamicTeleportService dynamicTeleportService = dependencies.d1();
        ServerService serverService = dependencies.d2();
        AnchorService anchorService = dependencies.d3();


        LiteralCommandNode<CommandSource> anchorCommand = LiteralArgumentBuilder
                .<CommandSource>literal(anchor)
                .requires(source -> source instanceof Player)
                .executes(context -> {
                    if(!(context.getSource() instanceof Player player)) {
                        logger.log("/"+anchor+" must be sent as a player!");
                        return Command.SINGLE_SUCCESS;
                    }
                    if(!Permission.validate(player, "rustyconnector.command.anchor", "rustyconnector.command.anchor."+anchor)) {
                        player.sendMessage(VelocityLang.NO_PERMISSION);
                        return Command.SINGLE_SUCCESS;
                    }

                    try {
                        PlayerFocusedServerFamily family = ((PlayerFocusedServerFamily) anchorService.family(anchor).orElseThrow());

                        // If the attempt to check player's family fails, just ignore it and try to connect.
                        // If there's actually an issue it'll be caught further down.
                        try {
                            PlayerServer server = serverService.search(Objects.requireNonNull(player.getCurrentServer().orElse(null)).getServerInfo());
                            if(family.equals(server.family()))
                                return closeMessage(player, VelocityLang.SERVER_ALREADY_CONNECTED);
                        } catch (Exception ignore) {}

                        family.connect(player);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return closeMessage(player, VelocityLang.INTERNAL_ERROR);
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .build();

        return new BrigadierCommand(anchorCommand);
    }

    public static int closeMessage(Player player, Component message) {
        player.sendMessage(message);
        return Command.SINGLE_SUCCESS;
    }
}