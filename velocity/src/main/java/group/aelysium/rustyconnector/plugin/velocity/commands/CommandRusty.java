package group.aelysium.rustyconnector.plugin.velocity.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import group.aelysium.rustyconnector.core.lib.data_messaging.cache.CacheableMessage;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.DefaultConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.LoggerConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.PaperServerLoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.ServerFamily;
import group.aelysium.rustyconnector.core.lib.data_messaging.cache.MessageCache;

import java.io.File;
import java.util.List;

public final class CommandRusty {
    public static BrigadierCommand create() {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        LiteralCommandNode<CommandSource> rusty = LiteralArgumentBuilder
            .<CommandSource>literal("rc")
            .requires(source -> source instanceof ConsoleCommandSource)
            .executes(context -> {
                VelocityLang.RC_ROOT_USAGE.send(plugin.logger());
                return Command.SINGLE_SUCCESS;
            })
            .then(LiteralArgumentBuilder.<CommandSource>literal("message")
                    .executes(context -> {
                        VelocityLang.RC_MESSAGE_ROOT_USAGE.send(plugin.logger());
                        return Command.SINGLE_SUCCESS;
                    })
                    .then(LiteralArgumentBuilder.<CommandSource>literal("list")
                            .executes(context -> {
                                new Thread(() -> {
                                    try {
                                        if(plugin.getVirtualServer().getMessageCache().getSize() > 10) {
                                            int numberOfPages = Math.floorDiv(plugin.getVirtualServer().getMessageCache().getSize(),10) + 1;

                                            List<CacheableMessage> messagesPage = plugin.getVirtualServer().getMessageCache().getMessagesPage(1);

                                            VelocityLang.RC_MESSAGE_PAGE.send(plugin.logger(),messagesPage,1,numberOfPages);

                                            return;
                                        }

                                        List<CacheableMessage> messages = plugin.getVirtualServer().getMessageCache().getMessages();

                                        VelocityLang.RC_MESSAGE_PAGE.send(plugin.logger(),messages,1,1);

                                    } catch (Exception e) {
                                        VelocityLang.RC_MESSAGE_ERROR.send(plugin.logger(),"There was an issue getting those messages!");
                                    }
                                }).start();

                                return Command.SINGLE_SUCCESS;
                            })
                            .then(RequiredArgumentBuilder.<CommandSource, Integer>argument("page-number", IntegerArgumentType.integer())
                                    .executes(context -> {
                                        new Thread(() -> {
                                            try {
                                                int pageNumber = context.getArgument("page-number", Integer.class);

                                                List<CacheableMessage> messages = plugin.getVirtualServer().getMessageCache().getMessagesPage(pageNumber);

                                                int numberOfPages = Math.floorDiv(plugin.getVirtualServer().getMessageCache().getSize(),10) + 1;

                                                VelocityLang.RC_MESSAGE_PAGE.send(plugin.logger(),messages,pageNumber,numberOfPages);
                                            } catch (Exception e) {
                                                VelocityLang.RC_MESSAGE_ERROR.send(plugin.logger(),"There was an issue getting that page!");
                                            }

                                        }).start();
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
                    .then(LiteralArgumentBuilder.<CommandSource>literal("get")
                            .executes(context -> {
                                VelocityLang.RC_MESSAGE_GET_USAGE.send(plugin.logger());

                                return Command.SINGLE_SUCCESS;
                            })
                            .then(RequiredArgumentBuilder.<CommandSource, Long>argument("snowflake", LongArgumentType.longArg())
                                    .executes(context -> {
                                        try {
                                            Long snowflake = context.getArgument("snowflake", Long.class);
                                            MessageCache messageCache = VelocityRustyConnector.getInstance().getVirtualServer().getMessageCache();

                                            CacheableMessage message = messageCache.getMessage(snowflake);

                                            VelocityLang.RC_MESSAGE_GET_MESSAGE.send(plugin.logger(),message.getSnowflake(),message.getDate(),message.getContents());
                                        } catch (Exception e) {
                                            VelocityLang.RC_MESSAGE_ERROR.send(plugin.logger(),"There's no saved message with that ID!");
                                        }

                                        return Command.SINGLE_SUCCESS;
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

                    return Command.SINGLE_SUCCESS;
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("familyName", StringArgumentType.string())
                        .executes(context -> {
                            try {
                                String familyName = context.getArgument("familyName", String.class);
                                ServerFamily<? extends PaperServerLoadBalancer> family = VelocityRustyConnector.getInstance().getVirtualServer().getFamilyManager().find(familyName);
                                if(family == null) throw new NullPointerException();

                                VelocityLang.RC_FAMILY_INFO.send(plugin.logger(), family);
                            } catch (NullPointerException e) {
                                VelocityLang.RC_FAMILY_ERROR.send(plugin.logger(),"A family with that name doesn't exist!");
                            } catch (Exception e) {
                                VelocityLang.RC_FAMILY_ERROR.send(plugin.logger(),"Something prevented us from getting that family!");
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(LiteralArgumentBuilder.<CommandSource>literal("resetIndex")
                                .executes(context -> {
                                    try {
                                        String familyName = context.getArgument("familyName", String.class);
                                        ServerFamily<? extends PaperServerLoadBalancer> family = VelocityRustyConnector.getInstance().getVirtualServer().getFamilyManager().find(familyName);
                                        if(family == null) throw new NullPointerException();

                                        family.getLoadBalancer().resetIndex();

                                        VelocityLang.RC_FAMILY_INFO.send(plugin.logger(), family);
                                    } catch (NullPointerException e) {
                                        VelocityLang.RC_FAMILY_ERROR.send(plugin.logger(),"A family with that name doesn't exist!");
                                    } catch (Exception e) {
                                        VelocityLang.RC_FAMILY_ERROR.send(plugin.logger(),"Something prevented us from doing that!");
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .then(LiteralArgumentBuilder.<CommandSource>literal("sort")
                                .executes(context -> {
                                    try {
                                        String familyName = context.getArgument("familyName", String.class);
                                        ServerFamily<? extends PaperServerLoadBalancer> family = VelocityRustyConnector.getInstance().getVirtualServer().getFamilyManager().find(familyName);
                                        if(family == null) throw new NullPointerException();

                                        family.getLoadBalancer().completeSort();

                                        VelocityLang.RC_FAMILY_INFO.send(plugin.logger(), family);
                                    } catch (NullPointerException e) {
                                        VelocityLang.RC_FAMILY_ERROR.send(plugin.logger(),"A family with that name doesn't exist!");
                                    } catch (Exception e) {
                                        VelocityLang.RC_FAMILY_ERROR.send(plugin.logger(),"Something prevented us from doing that!"+e.getMessage());
                                    }
                                    return 1;
                                })
                        )
                )
            )
            .then(LiteralArgumentBuilder.<CommandSource>literal("register")
                    .executes(context -> {
                        try {
                            VelocityLang.RC_REGISTER_USAGE.send(plugin.logger());
                        } catch (Exception e) {
                            VelocityLang.RC_REGISTER_ERROR.send(plugin.logger(), "Something prevented us from sending a request for registration!");
                        }

                        return 1;
                    })
                    .then(LiteralArgumentBuilder.<CommandSource>literal("all")
                            .executes(context -> {
                                try {
                                    plugin.getVirtualServer().registerAllServers();
                                    return 1;
                                } catch (Exception e) {
                                    VelocityLang.RC_REGISTER_ERROR.send(plugin.logger(), e.getMessage());
                                }
                                return 0;
                            })
                    )
                    .then(LiteralArgumentBuilder.<CommandSource>literal("family")
                            .executes(context -> {
                                VelocityLang.RC_REGISTER_USAGE.send(plugin.logger());
                                return 1;
                            }).then(RequiredArgumentBuilder.<CommandSource, String>argument("familyName", StringArgumentType.string())
                                    .executes(context -> {
                                        try {
                                            String familyName = context.getArgument("familyName", String.class);
                                            ServerFamily<? extends PaperServerLoadBalancer> family = VelocityRustyConnector.getInstance().getVirtualServer().getFamilyManager().find(familyName);
                                            if(family == null) throw new NullPointerException();

                                            VelocityRustyConnector.getInstance().getVirtualServer().registerAllServers(familyName);
                                        } catch (NullPointerException e) {
                                            VelocityLang.RC_REGISTER_ERROR.send(plugin.logger(),"A family with that name doesn't exist!");
                                        } catch (Exception e) {
                                            VelocityLang.RC_REGISTER_ERROR.send(plugin.logger(),"Something prevented us from reloading that family!");
                                        }
                                        return 1;
                                    })
                            )
                    )
            )
            .then(LiteralArgumentBuilder.<CommandSource>literal("reload")
                    .executes(context -> {
                        VelocityLang.RC_RELOAD_USAGE.send(plugin.logger());
                        return 1;
                    })
                    .then(LiteralArgumentBuilder.<CommandSource>literal("proxy")
                            .executes(context -> {
                                plugin.logger().log("Reloading the proxy...");
                                try {
                                    DefaultConfig defaultConfig = DefaultConfig.newConfig(new File(plugin.getDataFolder(), "config.yml"), "velocity_config_template.yml");
                                    if(!defaultConfig.generate()) {
                                        throw new IllegalStateException("Unable to load or create config.yml!");
                                    }
                                    defaultConfig.register();

                                    plugin.getVirtualServer().reload(defaultConfig);
                                    plugin.logger().log("Done reloading!");

                                    VelocityLang.RC_ROOT_USAGE.send(plugin.logger());
                                    return 1;
                                } catch (Exception e) {
                                    VelocityRustyConnector.getInstance().logger().error(e.getMessage(),e);
                                }
                                return 0;
                            })
                    )
                    .then(LiteralArgumentBuilder.<CommandSource>literal("family")
                            .executes(context -> {
                                VelocityLang.RC_RELOAD_USAGE.send(plugin.logger());
                                return 1;
                            }).then(RequiredArgumentBuilder.<CommandSource, String>argument("familyName", StringArgumentType.string())
                                    .executes(context -> {
                                        try {
                                            String familyName = context.getArgument("familyName", String.class);
                                            plugin.logger().log("Reloading the family: "+familyName+"...");
                                            ServerFamily<? extends PaperServerLoadBalancer> oldFamily = VelocityRustyConnector.getInstance().getVirtualServer().getFamilyManager().find(familyName);
                                            if(oldFamily == null) {
                                                VelocityLang.RC_FAMILY_ERROR.send(plugin.logger(),"A family with that name doesn't exist!");
                                                return 1;
                                            }

                                            ServerFamily<? extends PaperServerLoadBalancer> newFamily = ServerFamily.init(plugin.getVirtualServer(), familyName);

                                            oldFamily.unregisterServers();

                                            plugin.getVirtualServer().getFamilyManager().remove(oldFamily);
                                            plugin.getVirtualServer().getFamilyManager().add(newFamily);

                                            plugin.logger().log("Done reloading!");

                                            VelocityLang.RC_FAMILY_INFO.send(plugin.logger(), newFamily);
                                            return 1;
                                        } catch (Exception e) {
                                            VelocityLang.RC_FAMILY_ERROR.send(plugin.logger(),"Something prevented us from reloading that family!\n"+e.getMessage());
                                        }
                                        return 0;
                                    })
                            )
                    )
                    .then(LiteralArgumentBuilder.<CommandSource>literal("logger")
                            .executes(context -> {
                                try {
                                    plugin.logger().log("Reloading plugin logger...");
                                    LoggerConfig loggerConfig = LoggerConfig.newConfig(new File(plugin.getDataFolder(), "logger.yml"), "velocity_logger_template.yml");
                                    if (!loggerConfig.generate()) {
                                        throw new IllegalStateException("Unable to load or create logger.yml!");
                                    }
                                    loggerConfig.register();
                                    PluginLogger.init(loggerConfig);
                                    plugin.logger().log("Done reloading!");

                                    return 1;
                                } catch (Exception e) {
                                    VelocityRustyConnector.getInstance().logger().error(e.getMessage(),e);
                                }
                                return 0;
                            })
                    )
                    .then(LiteralArgumentBuilder.<CommandSource>literal("whitelists")
                            .executes(context -> {
                                plugin.logger().log("Reloading whitelists...");
                                try {
                                    DefaultConfig defaultConfig = DefaultConfig.newConfig(new File(plugin.getDataFolder(), "config.yml"), "velocity_config_template.yml");
                                    if(!defaultConfig.generate()) {
                                        throw new IllegalStateException("Unable to load or create config.yml!");
                                    }
                                    defaultConfig.register();

                                    plugin.getVirtualServer().reloadWhitelists(defaultConfig);
                                    plugin.logger().log("Done reloading!");
                                    return 1;
                                } catch (Exception e) {
                                    VelocityRustyConnector.getInstance().logger().error(e.getMessage(),e);
                                }
                                return 0;
                            })
                    )
            )
            .build();

        // BrigadierCommand implements Command
        return new BrigadierCommand(rusty);
    }
}