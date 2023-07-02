package group.aelysium.rustyconnector.plugin.velocity.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ScalarServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import net.kyori.adventure.text.Component;

import java.util.Objects;

public class CommandHub {
    public static BrigadierCommand create() {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();

        LiteralCommandNode<CommandSource> hub = LiteralArgumentBuilder
                .<CommandSource>literal("hub")
                .requires(source -> source instanceof Player)
                .executes(context -> {
                    if(!(context.getSource() instanceof Player player)) {
                        logger.log("/hub must be sent as a player!");
                        return Command.SINGLE_SUCCESS;
                    }

                    ServerInfo sendersServerInfo = ((Player) context.getSource()).getCurrentServer().orElseThrow().getServerInfo();

                    PlayerServer sendersServer = api.getService(ServerService.class).findServer(sendersServerInfo);

                    BaseServerFamily senderFamily = sendersServer.getFamily();
                    ScalarServerFamily rootFamily = api.getService(FamilyService.class).getRootFamily();

                    if (!(senderFamily instanceof PlayerFocusedServerFamily)) {
                        // Attempt to connect to root family if we're not in a PlayerFocusedServerFamily
                        if (rootFamily != null) {
                            try {
                                ((PlayerFocusedServerFamily) rootFamily).connect(player);
                                return Command.SINGLE_SUCCESS;
                            } catch (RuntimeException err) {
                                VelocityLang.RC_FAMILY_ERROR.send(logger, "Failed to connect player to parent family!");
                                context.getSource().sendMessage(Component.text("Failed to connect to root family!"));
                            }
                        } else {
                            VelocityLang.RC_FAMILY_ERROR.send(logger, "You can only scalar and static families for family parents!");
                            context.getSource().sendMessage(Component.text("Failed to connect to root family!"));
                        }

                        return Command.SINGLE_SUCCESS;
                    }

                    String parentFamilyName = ((PlayerFocusedServerFamily) senderFamily).getParentFamily();
                    BaseServerFamily parentFamily;

                    if (Objects.equals(parentFamilyName, "")) parentFamily = rootFamily;
                    else parentFamily = api.getService(FamilyService.class).find(parentFamilyName);

                    // Attempt to connect to parent family.
                    if (parentFamily instanceof PlayerFocusedServerFamily) {
                        try {
                            ((PlayerFocusedServerFamily) parentFamily).connect(player);
                            return Command.SINGLE_SUCCESS;
                        } catch (RuntimeException err) {
                            VelocityLang.RC_FAMILY_ERROR.send(logger, "Failed to connect player to parent family!");
                        }
                    } else VelocityLang.RC_FAMILY_ERROR.send(logger,"You can only set scalar and static families for family parents!");

                    // Attempt to connect to root family if that fails.
                    if (rootFamily != null) {
                        try {
                            ((PlayerFocusedServerFamily) rootFamily).connect(player);
                            return Command.SINGLE_SUCCESS;
                        } catch (RuntimeException err) {
                            VelocityLang.RC_FAMILY_ERROR.send(logger, "Failed to connect player to parent family!");
                            context.getSource().sendMessage(Component.text("Failed to connect to both parent and root family!"));
                        }
                    } else {
                        VelocityLang.RC_FAMILY_ERROR.send(logger, "You can only scalar and static families for family parents!");
                        context.getSource().sendMessage(Component.text("Failed to connect to both parent and root family!"));
                    }

                    return Command.SINGLE_SUCCESS;
                })
                .build();

        return new BrigadierCommand(hub);
    }
}