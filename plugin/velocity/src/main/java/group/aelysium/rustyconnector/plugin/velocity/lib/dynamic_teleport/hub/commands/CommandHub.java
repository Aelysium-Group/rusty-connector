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
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.hub.HubService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.RootServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandHub {
    public static BrigadierCommand create() {
        VelocityAPI api = VelocityAPI.get();
        PluginLogger logger = api.logger();

        FamilyService familyService = api.services().familyService();
        HubService hubService = api.services().dynamicTeleportService().orElseThrow()
                                   .services().hubService().orElseThrow();
        ServerService serverService = api.services().serverService();

        LiteralCommandNode<CommandSource> hub = LiteralArgumentBuilder
                .<CommandSource>literal("hub")
                .requires(source -> source instanceof Player)
                .executes(context -> {
                    if(!(context.getSource() instanceof Player player)) {
                        logger.log("/hub must be sent as a player!");
                        return Command.SINGLE_SUCCESS;
                    }

                    ServerInfo serverInfo = ((Player) context.getSource()).getCurrentServer().orElseThrow().getServerInfo();

                    PlayerServer sendersServer = serverService.search(serverInfo);
                    BaseServerFamily family = sendersServer.family();
                    RootServerFamily rootFamily = familyService.rootFamily();

                    if(!hubService.isEnabled(family.name())) {
                        context.getSource().sendMessage(Lang.UNKNOWN_COMMAND);
                        return Command.SINGLE_SUCCESS;
                    }

                    if (!(family instanceof PlayerFocusedServerFamily)) {
                        // Attempt to connect to root family if we're not in a PlayerFocusedServerFamily
                        try {
                            rootFamily.connect(player);
                            return Command.SINGLE_SUCCESS;
                        } catch (RuntimeException err) {
                            logger.send(Component.text("Failed to connect player to parent family " + rootFamily.name() + "!",NamedTextColor.RED));
                            context.getSource().sendMessage(Component.text("Failed to connect you to the hub!"));
                        }

                        return Command.SINGLE_SUCCESS;
                    }

                    try {
                        PlayerFocusedServerFamily parent = (PlayerFocusedServerFamily) ((PlayerFocusedServerFamily) family).parent().get();

                        if(parent != null) {
                            parent.connect(player);
                            return Command.SINGLE_SUCCESS;
                        }

                        rootFamily.connect(player);
                    } catch (RuntimeException err) {
                        logger.send(Component.text("Failed to connect player to parent family " + rootFamily.name() + "!",NamedTextColor.RED));
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