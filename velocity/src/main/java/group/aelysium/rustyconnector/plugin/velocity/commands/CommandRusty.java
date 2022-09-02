package group.aelysium.rustyconnector.plugin.velocity.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.commands.rusty.CommandRustyFamily;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import rustyconnector.generic.lib.hash.MD5;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Plugin(id = "rustyconnector-velocity")
public final class CommandRusty {
    private VelocityRustyConnector plugin;
    public CommandRusty(VelocityRustyConnector plugin) {
        this.plugin = plugin;
    }

    public void createCommand(VelocityRustyConnector plugin) {
        LiteralCommandNode<CommandSource> rusty = LiteralArgumentBuilder
                .<CommandSource>literal("rc")
                .requires(source -> source instanceof ConsoleCommandSource)
                .executes(context -> {
                    CommandSource source = context.getSource();
                    // Get the arguments after the command alias
                    String[] args = context.getArguments();


                    if(args.length > 0) // /rc [[[xxx]]] xxx xxx xxx
                        switch (args[0].toLowerCase()) {
                            case "family":
                            case "families":
                            case "familys":
                            case "groups":
                            case "groupings":
                            case "group":
                            case "grouping":
                                CommandRustyFamily.execute(source, plugin, args);
                                return;
                            case "reload":
                            case "relod":
                            case "relad":
                            case "rload":
                                return;
                        }

                    source.sendMessage(Component.text("Usage:").color(NamedTextColor.RED));
                    source.sendMessage(Component.text("/rc family").color(NamedTextColor.AQUA));
                    source.sendMessage(Component.text("/rc reload").color(NamedTextColor.AQUA));
                    return 1;
                })
                .build();

        // BrigadierCommand implements Command
        BrigadierCommand command = new BrigadierCommand(rusty);
    }
}