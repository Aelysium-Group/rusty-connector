package group.aelysium.rustyconnector.plugin.velocity.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import group.aelysium.rustyconnector.core.lib.message.cache.CacheableMessage;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import group.aelysium.rustyconnector.core.lib.util.logger.LangMessage;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PaperServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.ServerFamily;
import group.aelysium.rustyconnector.core.lib.message.cache.MessageCache;
import group.aelysium.rustyconnector.core.lib.util.logger.Lang;

import java.net.InetSocketAddress;
import java.util.List;

@Plugin(id = "rustyconnector-velocity")
public final class CommandRusty {
    public static BrigadierCommand create() {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        LiteralCommandNode<CommandSource> rusty = LiteralArgumentBuilder
            .<CommandSource>literal("rc")
            .requires(source -> source instanceof ConsoleCommandSource)
            .executes(context -> {
                (new LangMessage(plugin.logger()))
                        .insert(Lang.commandUsage())
                        .insert(
                                Lang.boxedMessage(
                                    "/rc family",
                                    "Used to access the family controls for this plugin.",
                                    Lang.spacing(),
                                    "/rc message",
                                    "Access recently sent rusty-connector messages.",
                                    Lang.spacing(),
                                    "/rc player",
                                    "Used to access the player controls for this plugin.",
                                    Lang.spacing(),
                                    "/rc registerAll",
                                    "Request that all servers listening to the datachannel attempt to register themselves",
                                    Lang.spacing(),
                                    "/rc reload",
                                    "Reloads the RustyConnector plugin.",
                                    "This command should really only be used if the network is down for maintenance or if nobody is online!",
                                    "This command will kick EVERYONE off of this proxy!"
                                )
                        )
                        .print();

                return 1;
            })
            .then(LiteralArgumentBuilder.<CommandSource>literal("message")
                    .executes(context -> {
                        CommandSource source = context.getSource();

                        (new LangMessage(plugin.logger()))
                                .insert(Lang.commandUsage())
                                .insert(
                                        Lang.boxedMessage(
                                                "/rc message get <Message ID>",
                                                "Pulls a message out of the message cache. If a message is to old it might not be available anymore!",
                                                Lang.spacing(),
                                                "/rc message list <page number>",
                                                "Lists all currently cached messages! As new messages get cached, older ones will be pushed out of the cache."
                                        )
                                )
                                .print();

                        return 1;
                    })
                    .then(LiteralArgumentBuilder.<CommandSource>literal("list")
                            .executes(context -> {
                                new Thread(() -> {
                                    try {
                                        if(plugin.getProxy().getMessageCache().getSize() > 10) {
                                            double numberOfPages = Math.floorDiv(plugin.getProxy().getMessageCache().getSize(),10) + 1;

                                            List<CacheableMessage> messagesPage = plugin.getProxy().getMessageCache().getMessagesPage(1);

                                            LangMessage langMessage = (new LangMessage(plugin.logger()))
                                                    .insert(Lang.spacing())
                                                    .insert(Lang.spacing())
                                                    .insert(Lang.spacing());
                                            messagesPage.forEach(message -> {
                                                langMessage.insert(
                                                    Lang.boxedMessage(
                                                        "ID: "+message.getSnowflake(),
                                                        "Date: "+message.getDate().toString(),
                                                        "Contents: "+message.getContents()
                                                    )
                                                );
                                            });

                                            langMessage
                                                    .insert(Lang.spacing())
                                                    .insert("Showing page 1 out of "+ Math.floor(numberOfPages))
                                                    .insert(Lang.spacing())
                                                    .insert(Lang.border())
                                                    .print();

                                            return;
                                        }

                                        List<CacheableMessage> messages = plugin.getProxy().getMessageCache().getMessages();

                                        LangMessage langMessage = (new LangMessage(plugin.logger()))
                                                .insert(Lang.spacing())
                                                .insert(Lang.spacing())
                                                .insert(Lang.spacing());
                                        messages.forEach(message -> {
                                            langMessage.insert(
                                                    Lang.boxedMessage(
                                                            "ID: "+message.getSnowflake(),
                                                            "Date: "+message.getDate().toString(),
                                                            "Contents: "+message.getContents()
                                                    )
                                            );
                                        });

                                        langMessage.print();
                                    } catch (Exception e) {
                                        plugin.logger().error("There was an issue getting those messages!");
                                    }
                                }).start();

                                return 1;
                            })
                            .then(RequiredArgumentBuilder.<CommandSource, Integer>argument("page-number", IntegerArgumentType.integer())
                                    .executes(context -> {
                                        new Thread(() -> {
                                            try {
                                                Integer pageNumber = context.getArgument("page-number", Integer.class);

                                                List<CacheableMessage> messagesPage = plugin.getProxy().getMessageCache().getMessagesPage(pageNumber);

                                                double numberOfPages = Math.floorDiv(plugin.getProxy().getMessageCache().getSize(),10) + 1;


                                                LangMessage langMessage = (new LangMessage(plugin.logger()))
                                                        .insert(Lang.spacing())
                                                        .insert(Lang.spacing())
                                                        .insert(Lang.spacing());
                                                messagesPage.forEach(message -> {
                                                    langMessage.insert(
                                                            Lang.boxedMessage(
                                                                    "ID: "+message.getSnowflake(),
                                                                    "Date: "+message.getDate().toString(),
                                                                    "Contents: "+message.getContents()
                                                            )
                                                    );
                                                });

                                                langMessage
                                                        .insert(Lang.spacing())
                                                        .insert("Showing page 1 out of "+ Math.floor(numberOfPages))
                                                        .insert(Lang.spacing())
                                                        .insert(Lang.border())
                                                        .print();

                                                return;
                                            } catch (Exception e) {
                                                plugin.logger().error("There was an issue getting those messages!");
                                            }

                                        }).start();
                                        return 1;
                                    })
                            )
                    )
                    .then(LiteralArgumentBuilder.<CommandSource>literal("get")
                            .executes(context -> {
                                CommandSource source = context.getSource();

                                (new LangMessage(plugin.logger()))
                                        .insert(Lang.commandUsage())
                                        .insert("/rc message get <Message ID>")
                                        .insert("Pulls a message out of the message cache. If a message is to old it might not be available anymore!")
                                        .insert(Lang.spacing())
                                        .insert(Lang.border())
                                        .print();

                                return 1;
                            })
                            .then(RequiredArgumentBuilder.<CommandSource, Long>argument("snowflake", LongArgumentType.longArg())
                                    .executes(context -> {
                                        try {
                                            Long snowflake = context.getArgument("snowflake", Long.class);
                                            MessageCache messageCache = VelocityRustyConnector.getInstance().getProxy().getMessageCache();

                                            CacheableMessage message = messageCache.getMessage(snowflake);

                                            (new LangMessage(plugin.logger()))
                                                .insert(
                                                    Lang.boxedMessage(
                                                        "Found message with ID "+snowflake.toString(),
                                                        Lang.spacing(),
                                                        "ID: "+message.getSnowflake(),
                                                        "Date: "+message.getDate().toString(),
                                                        "Contents: "+message.getContents()
                                                    ))
                                                .print();
                                        } catch (NullPointerException e) {
                                            VelocityRustyConnector.getInstance().logger().log("That message either doesn't exist or is no-longer available in the cache!");
                                        } catch (Exception e) {
                                            VelocityRustyConnector.getInstance().logger().log("An error stopped us from getting that message!", e);
                                        }

                                        return 1;
                                    })
                            )
                    )
            )
            .then(LiteralArgumentBuilder.<CommandSource>literal("family")
                .executes(context -> {
                    (new LangMessage(plugin.logger()))
                            .insert(Lang.commandUsage())
                            .insert(
                                    Lang.boxedMessage(
                                            "/rc family list",
                                            "Gets a list of all registered families.",
                                            Lang.spacing(),
                                            "/rc family info <family name>",
                                            "Gets info about a particular family",
                                            Lang.spacing(),
                                            "/rc family reload all",
                                            "Reloads all families, this also unregisters all servers that are saved.",
                                            "This command will kick EVERYONE off of this proxy!",
                                            Lang.spacing(),
                                            "/rc family reload <family name>",
                                            "Reload a specific family, this also unregisters all servers that are saved to this family.",
                                            "This command will kick EVERYONE off of this specific family!"
                                    )
                            )
                            .print();

                    return 1;
                })
                .then(LiteralArgumentBuilder.<CommandSource>literal("list")
                    .executes(context -> {
                        plugin.getProxy().getFamilyManager().printFamilies();
                        return 1;
                    })
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("info")
                    .executes(context -> {
                        (new LangMessage(plugin.logger()))
                                .insert(Lang.commandUsage())
                                .insert(
                                        Lang.boxedMessage(
                                                "/rc family info <family name>",
                                                "Get info for this family",
                                                Lang.spacing(),
                                                "/rc family info <family name> servers",
                                                "Lists all servers that are registered to this family",
                                                Lang.spacing(),
                                                "/rc family reload all",
                                                "Reloads all families, this also unregisters all servers that are saved.",
                                                "This command will kick EVERYONE off of this proxy!",
                                                Lang.spacing(),
                                                "/rc family reload <family name>",
                                                "Reload a specific family, this also unregisters all servers that are saved to this family.",
                                                "This command will kick EVERYONE off of this specific family!"
                                        )
                                )
                                .print();
                        return 1;
                    })
                    .then(RequiredArgumentBuilder.<CommandSource, String>argument("familyName", StringArgumentType.string())
                            .executes(context -> {
                                String familyName = context.getArgument("familyName", String.class);
                                ServerFamily family = VelocityRustyConnector.getInstance().getProxy().getFamilyManager().find(familyName);

                                family.printInfo();
                                return 1;
                            })
                            .then(RequiredArgumentBuilder.<CommandSource, String>argument("servers", StringArgumentType.word())
                                    .executes(context -> {
                                        String familyName = context.getArgument("familyName", String.class);
                                        ServerFamily family = VelocityRustyConnector.getInstance().getProxy().getFamilyManager().find(familyName);

                                        family.printServers();
                                        return 1;
                                    })
                            )
                    )
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("reload")
                    .executes(context -> {
                        (new LangMessage(plugin.logger()))
                                .insert(Lang.commandUsage())
                                .insert(
                                        Lang.boxedMessage(
                                                "/rc family reload all",
                                                "Reloads all families, this also unregisters all servers that are saved.",
                                                "This command will kick EVERYONE off of this proxy!",
                                                Lang.spacing(),
                                                "/rc family reload <family name>",
                                                "Reload a specific family, this also unregisters all servers that are saved to this family.",
                                                "This command will kick EVERYONE off of this specific family!"
                                        )
                                )
                                .print();
                        return 1;
                    })
                    .then(LiteralArgumentBuilder.<CommandSource>literal("all")
                            .executes(context -> {

                                // TODO: Reload all families

                                return 1;
                            })
                    )
                    .then(LiteralArgumentBuilder.<CommandSource>literal("familyName")
                            .executes(context -> {
                                String familyName = context.getArgument("familyName", String.class);
                                ServerFamily family = VelocityRustyConnector.getInstance().getProxy().getFamilyManager().find(familyName);

                                // TODO: Reload a specific family

                                return 1;
                            })
                    )
                )
            )
            .then(LiteralArgumentBuilder.<CommandSource>literal("registerAll")
                    .executes(context -> {
                        VelocityRustyConnector.getInstance().getProxy().registerAllServers();

                        return 1;
                    })
            )
            /*.then(LiteralArgumentBuilder.<CommandSource>literal("reload")
                    .executes(context -> {
                        VelocityRustyConnector.getInstance().reload();

                        return 1;
                    })
            )*/
            .then(LiteralArgumentBuilder.<CommandSource>literal("debug")
                    .then(LiteralArgumentBuilder.<CommandSource>literal("setPlayerCount")
                            .then(RequiredArgumentBuilder.<CommandSource, String>argument("ip", StringArgumentType.string())
                            .then(RequiredArgumentBuilder.<CommandSource, String>argument("family-name", StringArgumentType.string())
                            .then(RequiredArgumentBuilder.<CommandSource, Integer>argument("player-count", IntegerArgumentType.integer())
                            .executes(context -> {
                                String ip = context.getArgument("ip", String.class);
                                String familyName = context.getArgument("family-name", String.class);
                                Integer playerCount = context.getArgument("player-count", Integer.class);

                                InetSocketAddress address = AddressUtil.parseAddress(ip);

                                ServerFamily family = VelocityRustyConnector.getInstance().getProxy().getFamilyManager().find(familyName);
                                PaperServer server = family.getServer(address);

                                family.setServerPlayerCount(playerCount,server);

                                return 1;
                            })
                            )))
                    )
            )
            .build();

        // BrigadierCommand implements Command
        return new BrigadierCommand(rusty);
    }
}