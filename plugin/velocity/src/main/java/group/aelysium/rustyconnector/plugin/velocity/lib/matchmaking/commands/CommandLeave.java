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

                        RankedFamily foundFamily = null;
                        for (RankedFamily family : families)
                            if(family.matchmaker().contains(player)) foundFamily = family;

                        if(foundFamily == null) {
                            eventPlayer.sendMessage(Component.text("You have to be in matchmaking to use /leave"));
                            return Command.SINGLE_SUCCESS;
                        }

                        foundFamily.leave(player);

                        eventPlayer.sendMessage(Component.text("You successfully left matchmaking!"));

                        if(foundFamily.matchmaker().settings().parentFamilyOnLeave())
                            foundFamily.parent().connect(player);
                    } catch (Exception ignore) {
                        eventPlayer.sendMessage(Component.text("There was an issue trying to leave matchmaking"));
                    }

                    return Command.SINGLE_SUCCESS;
                })
                .build();

        return new BrigadierCommand(hub);
    }
}