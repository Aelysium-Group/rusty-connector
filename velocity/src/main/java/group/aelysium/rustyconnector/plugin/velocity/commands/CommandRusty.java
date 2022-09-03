package group.aelysium.rustyconnector.plugin.velocity.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerFamily;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import group.aelysium.rustyconnector.core.generic.lib.MessageCache;
import group.aelysium.rustyconnector.core.generic.lib.generic.Lang;

import java.util.List;

@Plugin(id = "rustyconnector-velocity")
public final class CommandRusty {
    public static BrigadierCommand create() {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        LiteralCommandNode<CommandSource> rusty = LiteralArgumentBuilder
            .<CommandSource>literal("rc")
            .requires(source -> source instanceof ConsoleCommandSource)
            .executes(context -> {
                CommandSource source = context.getSource();

                Lang.print(VelocityRustyConnector.getInstance().logger(), Lang.commandUsage());

                source.sendMessage(Component.text("/rc family").color(NamedTextColor.AQUA));
                source.sendMessage(Component.text("Used to access the family controls for this plugin.").color(NamedTextColor.GRAY));
                VelocityRustyConnector.getInstance().logger().log(Lang.spacing());

                source.sendMessage(Component.text("/rc retrieveMessage").color(NamedTextColor.AQUA));
                source.sendMessage(Component.text("Pulls a message out of the message cache. If a message is to old it might not be available anymore!").color(NamedTextColor.GRAY));
                VelocityRustyConnector.getInstance().logger().log(Lang.spacing());

                source.sendMessage(Component.text("/rc player").color(NamedTextColor.AQUA));
                source.sendMessage(Component.text("Used to access the player controls for this plugin.").color(NamedTextColor.GRAY));
                VelocityRustyConnector.getInstance().logger().log(Lang.spacing());

                source.sendMessage(Component.text("/rc reload").color(NamedTextColor.YELLOW));
                source.sendMessage(Component.text("Reloads the RustyConnector plugin.").color(NamedTextColor.GRAY));
                source.sendMessage(Component.text("This command should really only be used if the network is down for maintenance or if nobody is online!").color(NamedTextColor.GRAY));
                source.sendMessage(Component.text("This command will kick EVERYONE off of this proxy!").color(NamedTextColor.RED));
                VelocityRustyConnector.getInstance().logger().log(Lang.spacing());
                VelocityRustyConnector.getInstance().logger().log(Lang.border());

                return 1;
            })
            .then(RequiredArgumentBuilder.<CommandSource, String>argument("family", StringArgumentType.word())
                .executes(context -> {
                    CommandSource source = context.getSource();

                    Lang.print(VelocityRustyConnector.getInstance().logger(), Lang.commandUsage());

                    source.sendMessage(Component.text("/rc family list").color(NamedTextColor.AQUA));
                    source.sendMessage(Component.text("Gets a list of all registered families.").color(NamedTextColor.GRAY));
                    VelocityRustyConnector.getInstance().logger().log(Lang.spacing());

                    source.sendMessage(Component.text("/rc family reload all").color(NamedTextColor.AQUA));
                    source.sendMessage(Component.text("Reloads all families, this also unregisters all servers that are saved.").color(NamedTextColor.GRAY));
                    source.sendMessage(Component.text("This command will kick EVERYONE off of this proxy!").color(NamedTextColor.RED));
                    VelocityRustyConnector.getInstance().logger().log(Lang.spacing());

                    source.sendMessage(Component.text("/rc family reload <family name>").color(NamedTextColor.AQUA));
                    source.sendMessage(Component.text("Reload a specific family, this also unregisters all servers that are saved to this family.").color(NamedTextColor.GRAY));
                    source.sendMessage(Component.text("This command will kick EVERYONE off of this specific family!").color(NamedTextColor.RED));
                    VelocityRustyConnector.getInstance().logger().log(Lang.spacing());
                    VelocityRustyConnector.getInstance().logger().log(Lang.border());

                    return 1;
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("list", StringArgumentType.word())
                    .executes(context -> {
                        plugin.getProxy().printFamilies();
                        return 1;
                    })
                )
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("info", StringArgumentType.word())
                    .executes(context -> {
                        CommandSource source = context.getSource();

                        Lang.print(VelocityRustyConnector.getInstance().logger(), Lang.commandUsage());

                        source.sendMessage(Component.text("/rc family info <family name>").color(NamedTextColor.YELLOW));
                        source.sendMessage(Component.text("Get info for this family").color(NamedTextColor.GRAY));
                        VelocityRustyConnector.getInstance().logger().log(Lang.spacing());

                        source.sendMessage(Component.text("/rc family info <family name> servers").color(NamedTextColor.YELLOW));
                        source.sendMessage(Component.text("Lists all servers that are registered to this family").color(NamedTextColor.GRAY));
                        VelocityRustyConnector.getInstance().logger().log(Lang.spacing());
                        VelocityRustyConnector.getInstance().logger().log(Lang.border());

                        return 1;
                    })
                    .then(RequiredArgumentBuilder.<CommandSource, String>argument("familyName", StringArgumentType.string())
                            .executes(context -> {
                                String familyName = context.getArgument("familyName", String.class);
                                ServerFamily family = VelocityRustyConnector.getInstance().getProxy().findFamily(familyName);

                                family.printInfo();
                                return 1;
                            })
                            .then(RequiredArgumentBuilder.<CommandSource, String>argument("servers", StringArgumentType.word())
                                    .executes(context -> {
                                        String familyName = context.getArgument("familyName", String.class);
                                        ServerFamily family = VelocityRustyConnector.getInstance().getProxy().findFamily(familyName);

                                        family.printServers();
                                        return 1;
                                    })
                            )
                    )
                )
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("reload", StringArgumentType.word())
                    .executes(context -> {
                        CommandSource source = context.getSource();

                        Lang.print(VelocityRustyConnector.getInstance().logger(), Lang.commandUsage());

                        source.sendMessage(Component.text("/rc family reload all").color(NamedTextColor.AQUA));
                        source.sendMessage(Component.text("Reloads all families, this also unregisters all servers that are saved.").color(NamedTextColor.GRAY));
                        source.sendMessage(Component.text("This command will kick EVERYONE off of this proxy!").color(NamedTextColor.RED));
                        VelocityRustyConnector.getInstance().logger().log(Lang.spacing());

                        source.sendMessage(Component.text("/rc family reload <family name>").color(NamedTextColor.AQUA));
                        source.sendMessage(Component.text("Reload a specific family, this also unregisters all servers that are saved to this family.").color(NamedTextColor.GRAY));
                        source.sendMessage(Component.text("This command will kick EVERYONE off of this specific family!").color(NamedTextColor.RED));
                        VelocityRustyConnector.getInstance().logger().log(Lang.spacing());
                        VelocityRustyConnector.getInstance().logger().log(Lang.border());
                        return 1;
                    })
                    .then(RequiredArgumentBuilder.<CommandSource, String>argument("all", StringArgumentType.word())
                            .executes(context -> {
                                List<ServerFamily> families = VelocityRustyConnector.getInstance().getProxy().getRegisteredFamilies();

                                // TODO: Reload all families

                                return 1;
                            })
                    )
                    .then(RequiredArgumentBuilder.<CommandSource, String>argument("familyName", StringArgumentType.string())
                            .executes(context -> {
                                String familyName = context.getArgument("familyName", String.class);
                                ServerFamily family = VelocityRustyConnector.getInstance().getProxy().findFamily(familyName);

                                // TODO: Reload a specific family

                                return 1;
                            })
                    )
                )
            )
            .then(RequiredArgumentBuilder.<CommandSource, String>argument("reload", StringArgumentType.word())
                .executes(context -> {
                    VelocityRustyConnector.getInstance().reload();

                    return 1;
                })
            )
            .then(RequiredArgumentBuilder.<CommandSource, String>argument("retrieveMessage", StringArgumentType.word())
                .executes(context -> {
                    CommandSource source = context.getSource();

                    Lang.print(VelocityRustyConnector.getInstance().logger(), Lang.commandUsage());

                    source.sendMessage(Component.text("/rc retrieveMessage <Message ID>").color(NamedTextColor.AQUA));
                    source.sendMessage(Component.text("Pulls a message out of the message cache. If a message is to old it might not be available anymore!").color(NamedTextColor.GRAY));
                    VelocityRustyConnector.getInstance().logger().log(Lang.spacing());
                    VelocityRustyConnector.getInstance().logger().log(Lang.border());

                    return 1;
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("snowflake", StringArgumentType.string())
                    .executes(context -> {
                        try {
                            Long snowflake = context.getArgument("snowflake", Long.class);
                            MessageCache messageCache = VelocityRustyConnector.getInstance().getMessageCache();

                            String message = messageCache.getMessage(snowflake);

                            VelocityRustyConnector.getInstance().logger().log(Lang.border());
                            VelocityRustyConnector.getInstance().logger().log(Lang.spacing());
                            VelocityRustyConnector.getInstance().logger().log("Found message with ID "+snowflake.toString());
                            VelocityRustyConnector.getInstance().logger().log(Lang.spacing());
                            VelocityRustyConnector.getInstance().logger().log(message);
                            VelocityRustyConnector.getInstance().logger().log(Lang.spacing());
                            VelocityRustyConnector.getInstance().logger().log(Lang.border());
                        } catch (NullPointerException e) {
                            VelocityRustyConnector.getInstance().logger().log("That message either doesn't exist or is no-longer available in the cache!");
                        } catch (Exception e) {
                            VelocityRustyConnector.getInstance().logger().log("An error stopped us from getting that message!");
                        }

                        return 1;
                    })
                )
            )
            .build();

        // BrigadierCommand implements Command
        return new BrigadierCommand(rusty);
    }
}