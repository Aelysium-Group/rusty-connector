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
import group.aelysium.rustyconnector.core.lib.data_messaging.cache.CacheableMessage;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.PaperServerLoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.ServerFamily;
import group.aelysium.rustyconnector.core.lib.data_messaging.cache.MessageCache;

import java.util.List;


@Plugin(id = "rustyconnector-velocity")
public final class CommandRusty {
    public static BrigadierCommand create() {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        LiteralCommandNode<CommandSource> rusty = LiteralArgumentBuilder
            .<CommandSource>literal("rc")
            .requires(source -> source instanceof ConsoleCommandSource)
            .executes(context -> {
                VelocityLang.RC_ROOT_USAGE.send(plugin.logger());
                return 1;
            })
            .then(LiteralArgumentBuilder.<CommandSource>literal("message")
                    .executes(context -> {
                        VelocityLang.RC_MESSAGE_ROOT_USAGE.send(plugin.logger());
                        return 1;
                    })
                    .then(LiteralArgumentBuilder.<CommandSource>literal("list")
                            .executes(context -> {
                                new Thread(() -> {
                                    try {
                                        if(plugin.getProxy().getMessageCache().getSize() > 10) {
                                            int numberOfPages = Math.floorDiv(plugin.getProxy().getMessageCache().getSize(),10) + 1;

                                            List<CacheableMessage> messagesPage = plugin.getProxy().getMessageCache().getMessagesPage(1);

                                            VelocityLang.RC_MESSAGE_PAGE.send(plugin.logger(),messagesPage,1,numberOfPages);

                                            return;
                                        }

                                        List<CacheableMessage> messages = plugin.getProxy().getMessageCache().getMessages();

                                        VelocityLang.RC_MESSAGE_PAGE.send(plugin.logger(),messages,1,1);

                                        return;
                                    } catch (Exception e) {
                                        VelocityLang.RC_MESSAGE_ERROR.send(plugin.logger(),"There was an issue getting those messages!");
                                    }
                                }).start();

                                return 1;
                            })
                            .then(RequiredArgumentBuilder.<CommandSource, Integer>argument("page-number", IntegerArgumentType.integer())
                                    .executes(context -> {
                                        new Thread(() -> {
                                            try {
                                                int pageNumber = context.getArgument("page-number", Integer.class);

                                                List<CacheableMessage> messages = plugin.getProxy().getMessageCache().getMessagesPage(pageNumber);

                                                int numberOfPages = Math.floorDiv(plugin.getProxy().getMessageCache().getSize(),10) + 1;

                                                VelocityLang.RC_MESSAGE_PAGE.send(plugin.logger(),messages,pageNumber,numberOfPages);
                                            } catch (Exception e) {
                                                VelocityLang.RC_MESSAGE_ERROR.send(plugin.logger(),"There was an issue getting that page!");
                                            }

                                        }).start();
                                        return 1;
                                    })
                            )
                    )
                    .then(LiteralArgumentBuilder.<CommandSource>literal("get")
                            .executes(context -> {
                                VelocityLang.RC_MESSAGE_GET_USAGE.send(plugin.logger());

                                return 1;
                            })
                            .then(RequiredArgumentBuilder.<CommandSource, Long>argument("snowflake", LongArgumentType.longArg())
                                    .executes(context -> {
                                        try {
                                            Long snowflake = context.getArgument("snowflake", Long.class);
                                            MessageCache messageCache = VelocityRustyConnector.getInstance().getProxy().getMessageCache();

                                            CacheableMessage message = messageCache.getMessage(snowflake);

                                            VelocityLang.RC_MESSAGE_GET_MESSAGE.send(plugin.logger(),message.getSnowflake(),message.getDate(),message.getContents());
                                        } catch (Exception e) {
                                            VelocityLang.RC_MESSAGE_ERROR.send(plugin.logger(),"There's no saved message with that ID!");
                                        }

                                        return 1;
                                    })
                            )
                    )
            )
            .then(LiteralArgumentBuilder.<CommandSource>literal("family")
                .executes(context -> {
                    try {
                        VelocityLang.RC_FAMILY.send(plugin.logger());
                    } catch (Exception e) {
                        VelocityLang.RC_FAMILY_ERROR.send(plugin.logger(),"Something prevented us from getting the families!");
                    }

                    return 1;
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("familyName", StringArgumentType.string())
                        .executes(context -> {
                            try {
                                String familyName = context.getArgument("familyName", String.class);
                                ServerFamily<? extends PaperServerLoadBalancer> family = VelocityRustyConnector.getInstance().getProxy().getFamilyManager().find(familyName);
                                if(family == null) throw new NullPointerException();

                                VelocityLang.RC_FAMILY_INFO.send(plugin.logger(), family);
                            } catch (NullPointerException e) {
                                VelocityLang.RC_FAMILY_ERROR.send(plugin.logger(),"A family with that name doesn't exist!");
                            } catch (Exception e) {
                                VelocityLang.RC_FAMILY_ERROR.send(plugin.logger(),"Something prevented us from getting that family!");
                            }
                            return 1;
                        })
                )
            )
            .then(LiteralArgumentBuilder.<CommandSource>literal("registerAll")
                    .executes(context -> {
                        try {
                            VelocityRustyConnector.getInstance().getProxy().registerAllServers();
                        } catch (Exception e) {
                            VelocityLang.RC_REGISTERALL_ERROR.send(plugin.logger(), "Something prevented us from sending a request for registration!");
                        }

                        return 1;
                    })
            )
            /*.then(LiteralArgumentBuilder.<CommandSource>literal("reload")
                    .executes(context -> {
                        VelocityRustyConnector.getInstance().reload();

                        return 1;
                    })
            )*/
            .build();

        // BrigadierCommand implements Command
        return new BrigadierCommand(rusty);
    }
}