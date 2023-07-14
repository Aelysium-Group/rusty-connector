package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.config.DynamicTeleportConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.Permission;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors.AnchorService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.commands.CommandTPA;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ScalarServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static group.aelysium.rustyconnector.plugin.velocity.central.Processor.ValidServices.*;
import static group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.DynamicTeleportService.ValidServices.ANCHOR_SERVICE;

public class CommandAnchor {
    public static BrigadierCommand create(String anchor) {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();

        DynamicTeleportService dynamicTeleportService = api.getService(DYNAMIC_TELEPORT_SERVICE).orElseThrow();
        AnchorService anchorService = dynamicTeleportService.getService(ANCHOR_SERVICE).orElseThrow();


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
                        ((PlayerFocusedServerFamily) anchorService.getFamily(anchor).orElseThrow()).connect(player);
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