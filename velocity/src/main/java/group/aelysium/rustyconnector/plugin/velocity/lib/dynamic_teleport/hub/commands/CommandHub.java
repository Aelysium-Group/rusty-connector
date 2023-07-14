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
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

import static group.aelysium.rustyconnector.plugin.velocity.central.Processor.ValidServices.*;
import static group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.DynamicTeleportService.ValidServices.HUB_SERVICE;

public class CommandHub {
    public static BrigadierCommand create() {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();

        FamilyService familyService = api.getService(FAMILY_SERVICE).orElseThrow();
        HubService hubService = api.getService(DYNAMIC_TELEPORT_SERVICE).orElseThrow().getService(HUB_SERVICE).orElseThrow();

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
                            VelocityLang.RC_SEND_NO_SERVER.send(logger, "Failed to connect player to parent family " + rootFamily.getName() + "!");
                            context.getSource().sendMessage(Component.text("Failed to connect you to any parent servers!"));
                        }

                        return Command.SINGLE_SUCCESS;
                    }

                    // Compile a list of parent families in-case a connect attempt fails
                    List<BaseServerFamily> parentFamilies = new ArrayList<>();
                    {
                        BaseServerFamily currentFamily = ((PlayerFocusedServerFamily) family).getParent().get();

                        int maxDepth = 10;
                        while (maxDepth > 0) {
                            BaseServerFamily parentFamily;

                            if (currentFamily == null) break;
                            else parentFamily = ((PlayerFocusedServerFamily) currentFamily).getParent().get();

                            if (!(parentFamily instanceof PlayerFocusedServerFamily)) break;

                            if (parentFamilies.contains(parentFamily)) break;
                            else parentFamilies.add(parentFamily);

                            currentFamily = ((PlayerFocusedServerFamily) parentFamily).getParent().get();

                            maxDepth--;
                        }

                        parentFamilies.add(rootFamily);
                    }

                    boolean playerSent = false;

                    for (BaseServerFamily currentFamily : parentFamilies) {
                        try {
                            ((PlayerFocusedServerFamily) currentFamily).connect(player);
                            playerSent = true;
                            break;
                        } catch (RuntimeException e) {
                            VelocityLang.RC_SEND_NO_SERVER.send(logger, "Failed to connect player to parent family " + family.getName() + "!");
                        }
                    }

                    if (!playerSent)
                        player.sendMessage(Component.text("Failed to connect you to any parent servers!"));

                    return Command.SINGLE_SUCCESS;
                })
                .build();

        return new BrigadierCommand(hub);
    }
}