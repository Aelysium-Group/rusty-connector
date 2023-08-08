package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.Permission;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors.AnchorService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Objects;

public class CommandAnchor {
    public static BrigadierCommand create(String anchor) {
        VelocityAPI api = VelocityAPI.get();
        PluginLogger logger = api.logger();

        DynamicTeleportService dynamicTeleportService = api.services().dynamicTeleportService().orElseThrow();
        AnchorService anchorService = dynamicTeleportService.services().anchorService().orElseThrow();
        ServerService serverService = api.services().serverService();


        LiteralCommandNode<CommandSource> anchorCommand = LiteralArgumentBuilder
                .<CommandSource>literal(anchor)
                .requires(source -> source instanceof Player)
                .executes(context -> {
                    if(!(context.getSource() instanceof Player player)) {
                        logger.log("/"+anchor+" must be sent as a player!");
                        return Command.SINGLE_SUCCESS;
                    }
                    if(!Permission.validate(player, "rustyconnector.command.anchor", "rustyconnector.command.anchor."+anchor)) {
                        player.sendMessage(VelocityLang.COMMAND_NO_PERMISSION);
                        return Command.SINGLE_SUCCESS;
                    }

                    try {
                        PlayerFocusedServerFamily family = ((PlayerFocusedServerFamily) anchorService.family(anchor).orElseThrow());

                        // If the attempt to check player's family fails, just ignore it and try to connect.
                        // If there's actually an issue it'll be caught further down.
                        try {
                            PlayerServer server = serverService.search(Objects.requireNonNull(player.getCurrentServer().orElse(null)).getServerInfo());
                            if(family.equals(server.family()))
                                return closeMessage(player, Component.text("You're already connected to this server.", NamedTextColor.RED));
                        } catch (Exception ignore) {}

                        family.connect(player);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return closeMessage(player, Component.text("There was a fatal error while trying to complete your request.", NamedTextColor.RED));
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