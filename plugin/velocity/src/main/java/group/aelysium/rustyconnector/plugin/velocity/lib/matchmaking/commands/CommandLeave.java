package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.RankedFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;

public class CommandLeave {
    public static BrigadierCommand create(FamilyService service) {
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
                        Player player = new Player(eventPlayer);

                        List<RankedFamily> families = new ArrayList<>();
                        service.dump().forEach(family -> {
                            if(family instanceof RankedFamily) families.add((RankedFamily) family);
                        });

                        RankedFamily queuedFamily = null;
                        for (RankedFamily family : families) {
                            if(family.dequeue(player)) {
                                queuedFamily = family;
                                break;
                            }
                        }

                        if(queuedFamily == null) {
                            eventPlayer.sendMessage(Component.text("You must be in matchmaking to use the `/leave` command!", NamedTextColor.RED));
                            return Command.SINGLE_SUCCESS;
                        }
                        eventPlayer.sendMessage(Component.text("You successfully left matchmaking!"));

                        if(queuedFamily.matchmaker().settings().queue().leaving().boot())
                            queuedFamily.parent().connect(player);
                    } catch (Exception ignore) {
                        eventPlayer.sendMessage(Component.text("There was an issue trying to leave matchmaking"));
                    }

                    return Command.SINGLE_SUCCESS;
                })
                .build();

        return new BrigadierCommand(hub);
    }
}