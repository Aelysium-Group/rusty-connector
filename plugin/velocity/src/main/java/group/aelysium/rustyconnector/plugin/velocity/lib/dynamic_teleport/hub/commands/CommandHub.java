package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.hub.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.family.scalar_family.IRootFamily;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.hub.HubService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandHub {
    public static BrigadierCommand create(DependencyInjector.DI3<FamilyService, ServerService, HubService> dependencies) {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();

        FamilyService familyService = dependencies.d1();
        ServerService serverService = dependencies.d2();
        HubService hubService = dependencies.d3();

        LiteralCommandNode<CommandSource> hub = LiteralArgumentBuilder
                .<CommandSource>literal("hub")
                .requires(source -> source instanceof com.velocitypowered.api.proxy.Player)
                .executes(context -> {
                    if(!(context.getSource() instanceof com.velocitypowered.api.proxy.Player eventPlayer)) {
                        logger.log("/hub must be sent as a player!");
                        return Command.SINGLE_SUCCESS;
                    }
                    Player player = new Player(eventPlayer);

                    IFamily family = player.server().orElseThrow().family();
                    IRootFamily rootFamily = familyService.rootFamily();

                    if(!hubService.isEnabled(family.id())) {
                        context.getSource().sendMessage(ProxyLang.UNKNOWN_COMMAND);
                        return Command.SINGLE_SUCCESS;
                    }

                    if(!family.metadata().canBeAParentFamily()) {
                        // Attempt to connect to root family if the family isn't allowed to be a parent family.
                        try {
                            rootFamily.connect(player);
                            return Command.SINGLE_SUCCESS;
                        } catch (RuntimeException err) {
                            logger.send(Component.text("Failed to connect player to parent family " + rootFamily.id() + "!",NamedTextColor.RED));
                            context.getSource().sendMessage(ProxyLang.HUB_CONNECTION_FAILED);
                        }

                        return Command.SINGLE_SUCCESS;
                    }

                    try {
                        IFamily parent = family.parent();

                        if(parent != null) {
                            parent.connect(player);
                            return Command.SINGLE_SUCCESS;
                        }

                        rootFamily.connect(player);
                    } catch (RuntimeException err) {
                        logger.send(Component.text("Failed to connect player to parent family " + rootFamily.id() + "!",NamedTextColor.RED));
                        context.getSource().sendMessage(ProxyLang.HUB_CONNECTION_FAILED);
                    }

                    return Command.SINGLE_SUCCESS;
                })
                .build();

        return new BrigadierCommand(hub);
    }
}