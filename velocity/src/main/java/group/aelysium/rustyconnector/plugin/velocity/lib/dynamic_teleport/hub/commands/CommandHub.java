package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.hub.commands;

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
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.hub.HubService;
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
import static group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.DynamicTeleportService.ValidServices.HUB_SERVICE;

public class CommandHub {
    public static BrigadierCommand create() {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();

        FamilyService familyService = api.getService(FAMILY_SERVICE).orElseThrow();
        HubService hubService = api.getService(DYNAMIC_TELEPORT_SERVICE).orElseThrow().getService(HUB_SERVICE).orElseThrow();
        ServerService serverService = api.getService(SERVER_SERVICE).orElseThrow();

        LiteralCommandNode<CommandSource> hub = LiteralArgumentBuilder
                .<CommandSource>literal("hub")
                .requires(source -> source instanceof Player)
                .executes(context -> {
                    if(!(context.getSource() instanceof Player player)) {
                        logger.log("/hub must be sent as a player!");
                        return Command.SINGLE_SUCCESS;
                    }

                    ServerInfo serverInfo = ((Player) context.getSource()).getCurrentServer().orElseThrow().getServerInfo();

                    PlayerServer sendersServer = api.getService(SERVER_SERVICE).orElseThrow().findServer(serverInfo);
                    BaseServerFamily family = sendersServer.getFamily();
                    ScalarServerFamily rootFamily = familyService.getRootFamily();

                    if(!hubService.isEnabled(family.getName())) {
                        context.getSource().sendMessage(Lang.UNKNOWN_COMMAND);
                        return Command.SINGLE_SUCCESS;
                    }

                    if (!(family instanceof PlayerFocusedServerFamily)) {
                        // Attempt to connect to root family if we're not in a PlayerFocusedServerFamily
                        try {
                            rootFamily.connect(player);
                            return Command.SINGLE_SUCCESS;
                        } catch (RuntimeException err) {
                            logger.send(Component.text("Failed to connect player to parent family " + rootFamily.getName() + "!",NamedTextColor.RED));
                            context.getSource().sendMessage(Component.text("Failed to connect you to the hub!"));
                        }

                        return Command.SINGLE_SUCCESS;
                    }

                    try {
                        PlayerFocusedServerFamily parent = (PlayerFocusedServerFamily) ((PlayerFocusedServerFamily) family).getParent().get();

                        if(parent != null) {
                            parent.connect(player);
                            return Command.SINGLE_SUCCESS;
                        }

                        rootFamily.connect(player);
                    } catch (RuntimeException err) {
                        logger.send(Component.text("Failed to connect player to parent family " + rootFamily.getName() + "!",NamedTextColor.RED));
                        context.getSource().sendMessage(Component.text("Failed to connect you to the hub!", NamedTextColor.RED));
                    }

                    return Command.SINGLE_SUCCESS;
                })
                .build();

        return new BrigadierCommand(hub);
    }

    public static int closeMessage(Player player, Component message) {
        player.sendMessage(message);
        return Command.SINGLE_SUCCESS;
    }
}