package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.hub.HubService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.RankedFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import group.aelysium.rustyconnector.toolkit.velocity.family.scalar_family.IRootFamily;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import io.fabric8.kubernetes.api.model.policy.v1beta1.HostPortRangeBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandLeave {
    public static BrigadierCommand create() {
        PluginLogger logger = Tinder.get().logger();

        LiteralCommandNode<CommandSource> hub = LiteralArgumentBuilder
                .<CommandSource>literal("leave")
                .requires(source -> source instanceof com.velocitypowered.api.proxy.Player)
                .executes(context -> {
                    if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player eventPlayer)) {
                        logger.log("/leave must be sent as a player!");
                        return Command.SINGLE_SUCCESS;
                    }
                    try {
                        Player player = Player.from(eventPlayer);

                        Family familyGeneric = player.server().orElseThrow().family();
                        if(!(familyGeneric instanceof RankedFamily family)) {
                            eventPlayer.sendMessage(ProxyLang.UNKNOWN_COMMAND);
                            return Command.SINGLE_SUCCESS;
                        }
                        if(!family.matchmaker().settings().queue().leaving().command()) {
                            eventPlayer.sendMessage(ProxyLang.UNKNOWN_COMMAND);
                            return Command.SINGLE_SUCCESS;
                        }

                        family.matchmaker().remove(player);
                        eventPlayer.sendMessage(Component.text("You successfully left matchmaking!"));

                        if(family.matchmaker().settings().queue().leaving().boot()) {
                            family.parent().connect(player);
                        }
                    } catch (Exception ignore) {
                        eventPlayer.sendMessage(Component.text("There was an issue trying to leave matchmaking"));
                    }

                    return Command.SINGLE_SUCCESS;
                })
                .build();

        return new BrigadierCommand(hub);
    }
}