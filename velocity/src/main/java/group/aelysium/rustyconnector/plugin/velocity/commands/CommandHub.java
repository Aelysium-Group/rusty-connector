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
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;
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
                        VelocityLang.BOXED_MESSAGE_COLORED.send(
                                logger,
                                Component.text("/hub must be sent as a player!"),
                                NamedTextColor.RED
                        );

                        return Command.SINGLE_SUCCESS;
                    }

                    ServerInfo sendersServerInfo = ((Player) context.getSource()).getCurrentServer().orElseThrow().getServerInfo();

                    PlayerServer sendersServer = api.getService(ServerService.class).findServer(sendersServerInfo);
                    BaseServerFamily senderFamily = sendersServer.getFamily();
                    ScalarServerFamily rootFamily = api.getService(FamilyService.class).getRootFamily();

                    if (!(senderFamily instanceof PlayerFocusedServerFamily)) {
                        // Attempt to connect to root family if we're not in a PlayerFocusedServerFamily
                        try {
                            ((PlayerFocusedServerFamily) rootFamily).connect(player);
                            return Command.SINGLE_SUCCESS;
                        } catch (RuntimeException err) {
                            VelocityLang.RC_SEND_NO_SERVER.send(logger, "Failed to connect player to parent family " + rootFamily.getName() + "!");
                            context.getSource().sendMessage(Component.text("Failed to connect you to any parent servers!"));
                        }

                        return Command.SINGLE_SUCCESS;
                    }

                    // Compile a list of parent families, going up the chain until
                    //  it either loops, or there are no more parents.
                    // Mostly just because it's a bit nicer than just being sent
                    // to the root family if the first parent is down.
                    List<PlayerFocusedServerFamily> parentFamilies = new ArrayList<>();

                    String currentFamilyName = ((PlayerFocusedServerFamily) senderFamily).getParentFamily();

                    int maxDepth = 10;
                    while (maxDepth > 0) {
                        BaseServerFamily parentFamily;

                        // If there's no parent, we're done. Otherwise, fetch the parent.
                        if (Objects.equals(currentFamilyName, "")) break;
                        else parentFamily = api.getService(FamilyService.class).find(currentFamilyName);

                        // If the parent is not a PlayerFocusedServerFamily, we can't go any further.
                        if (!(parentFamily instanceof PlayerFocusedServerFamily)) break;

                        // If we've already added this parent to the list we're in a loop, stop here.
                        // Otherwise, add it to the list and move on.
                        if (parentFamilies.contains(parentFamily)) break;
                        else parentFamilies.add((PlayerFocusedServerFamily) parentFamily);

                        // Update the current family name for the next iteration.
                        currentFamilyName = ((PlayerFocusedServerFamily) parentFamily).getParentFamily();

                        maxDepth--;
                    }

                    // Add root family as last resort.
                    parentFamilies.add(rootFamily);

                    boolean playerSent = false;

                    for (PlayerFocusedServerFamily family : parentFamilies) {
                        try {
                            family.connect(player);
                            playerSent = true;
                            break;
                        } catch (RuntimeException err) {
                            VelocityLang.RC_SEND_NO_SERVER.send(logger, "Failed to connect player to parent family " + family.getName() + "!");
                        }
                    }

                    // May as well let the player know if the send failed.
                    if (!playerSent) {
                        player.sendMessage(Component.text("Failed to connect you to any parent servers!"));
                    }

                    return Command.SINGLE_SUCCESS;
                })
                .build();

        return new BrigadierCommand(hub);
    }
}