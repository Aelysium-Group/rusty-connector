package group.aelysium.rustyconnector.plugin.velocity.central;

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
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.CacheableMessage;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.config.DefaultConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ScalarServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.StaticServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.MessageCacheService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.util.List;

public final class CommandRusty {
    public static BrigadierCommand create() {
        VelocityAPI api = VelocityAPI.get();
        PluginLogger logger = api.logger();

        MessageCacheService messageCacheService = api.services().messageCacheService();

        LiteralCommandNode<CommandSource> rusty = LiteralArgumentBuilder
            .<CommandSource>literal("rc")
            .requires(source -> source instanceof ConsoleCommandSource)
            .executes(context -> {
                VelocityLang.RC_ROOT_USAGE.send(logger);
                return Command.SINGLE_SUCCESS;
            })
            .then(LiteralArgumentBuilder.<CommandSource>literal("message")
                    .executes(context -> {
                        VelocityLang.RC_MESSAGE_ROOT_USAGE.send(logger);
                        return Command.SINGLE_SUCCESS;
                    })
                    .then(LiteralArgumentBuilder.<CommandSource>literal("list")
                            .executes(context -> {
                                new Thread(() -> {
                                    try {
                                        if(messageCacheService.size() > 10) {
                                            int numberOfPages = Math.floorDiv(messageCacheService.size(),10) + 1;

                                            List<CacheableMessage> messagesPage = messageCacheService.fetchMessagesPage(1);

                                            VelocityLang.RC_MESSAGE_PAGE.send(logger,messagesPage,1,numberOfPages);

                                            return;
                                        }

                                        List<CacheableMessage> messages = messageCacheService.messages();

                                        VelocityLang.RC_MESSAGE_PAGE.send(logger,messages,1,1);

                                    } catch (Exception e) {
                                        VelocityLang.RC_MESSAGE_ERROR.send(logger,"There was an issue getting those messages!\n"+e.getMessage());
                                    }
                                }).start();

                                return Command.SINGLE_SUCCESS;
                            })
                            .then(RequiredArgumentBuilder.<CommandSource, Integer>argument("page-number", IntegerArgumentType.integer())
                                    .executes(context -> {
                                        new Thread(() -> {
                                            try {
                                                int pageNumber = context.getArgument("page-number", Integer.class);

                                                List<CacheableMessage> messages = messageCacheService.fetchMessagesPage(pageNumber);

                                                int numberOfPages = Math.floorDiv(messageCacheService.size(),10) + 1;

                                                VelocityLang.RC_MESSAGE_PAGE.send(logger,messages,pageNumber,numberOfPages);
                                            } catch (Exception e) {
                                                VelocityLang.RC_MESSAGE_ERROR.send(logger,"There was an issue getting that page!\n"+e.getMessage());
                                            }

                                        }).start();
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
                    .then(LiteralArgumentBuilder.<CommandSource>literal("get")
                            .executes(context -> {
                                VelocityLang.RC_MESSAGE_GET_USAGE.send(logger);

                                return Command.SINGLE_SUCCESS;
                            })
                            .then(RequiredArgumentBuilder.<CommandSource, Long>argument("snowflake", LongArgumentType.longArg())
                                    .executes(context -> {
                                        try {
                                            Long snowflake = context.getArgument("snowflake", Long.class);

                                            CacheableMessage message = messageCacheService.findMessage(snowflake);

                                            VelocityLang.RC_MESSAGE_GET_MESSAGE.send(logger, message);
                                        } catch (Exception e) {
                                            VelocityLang.RC_MESSAGE_ERROR.send(logger,"There's no saved message with that ID!");
                                        }

                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
            )
            .then(LiteralArgumentBuilder.<CommandSource>literal("family")
                .executes(context -> {
                    try {
                        VelocityLang.RC_FAMILY.send(logger);
                    } catch (Exception e) {
                        VelocityLang.RC_FAMILY_ERROR.send(logger,"Something prevented us from getting the families!\n"+e.getMessage());
                    }

                    return Command.SINGLE_SUCCESS;
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("familyName", StringArgumentType.string())
                        .executes(context -> {
                            try {
                                String familyName = context.getArgument("familyName", String.class);
                                BaseServerFamily family = api.services().familyService().find(familyName);
                                if(family == null) throw new NullPointerException();

                                if(family instanceof ScalarServerFamily)
                                    VelocityLang.RC_SCALAR_FAMILY_INFO.send(logger, (ScalarServerFamily) family);
                                if(family instanceof StaticServerFamily)
                                    VelocityLang.RC_STATIC_FAMILY_INFO.send(logger, (StaticServerFamily) family);
                            } catch (NullPointerException e) {
                                VelocityLang.RC_FAMILY_ERROR.send(logger,"A family with that name doesn't exist!");
                            } catch (Exception e) {
                                VelocityLang.RC_FAMILY_ERROR.send(logger,"Something prevented us from getting that family!\n"+e.getMessage());
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(LiteralArgumentBuilder.<CommandSource>literal("resetIndex")
                                .executes(context -> {
                                    try {
                                        String familyName = context.getArgument("familyName", String.class);
                                        BaseServerFamily family = api.services().familyService().find(familyName);
                                        if(family == null) throw new NullPointerException();
                                        if(!(family instanceof PlayerFocusedServerFamily)) {
                                            VelocityLang.RC_FAMILY_ERROR.send(logger,"You can only resetIndex on scalar and static families!");
                                            return Command.SINGLE_SUCCESS;
                                        }

                                        ((PlayerFocusedServerFamily) family).loadBalancer().resetIndex();

                                        if(family instanceof ScalarServerFamily)
                                            VelocityLang.RC_SCALAR_FAMILY_INFO.send(logger, (ScalarServerFamily) family);
                                        if(family instanceof StaticServerFamily)
                                            VelocityLang.RC_STATIC_FAMILY_INFO.send(logger, (StaticServerFamily) family);
                                    } catch (NullPointerException e) {
                                        VelocityLang.RC_FAMILY_ERROR.send(logger,"A family with that name doesn't exist!");
                                    } catch (Exception e) {
                                        VelocityLang.RC_FAMILY_ERROR.send(logger,"Something prevented us from doing that!\n"+e.getMessage());
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .then(LiteralArgumentBuilder.<CommandSource>literal("sort")
                                .executes(context -> {
                                    try {
                                        String familyName = context.getArgument("familyName", String.class);
                                        BaseServerFamily family = api.services().familyService().find(familyName);
                                        if(family == null) throw new NullPointerException();
                                        if(!(family instanceof PlayerFocusedServerFamily)) {
                                            VelocityLang.RC_FAMILY_ERROR.send(logger,"You can only use sort on scalar and static families!");
                                            return Command.SINGLE_SUCCESS;
                                        }

                                        ((PlayerFocusedServerFamily) family).loadBalancer().completeSort();

                                        if(family instanceof ScalarServerFamily)
                                            VelocityLang.RC_SCALAR_FAMILY_INFO.send(logger, (ScalarServerFamily) family);
                                        if(family instanceof StaticServerFamily)
                                            VelocityLang.RC_STATIC_FAMILY_INFO.send(logger, (StaticServerFamily) family);
                                    } catch (NullPointerException e) {
                                        VelocityLang.RC_FAMILY_ERROR.send(logger,"A family with that name doesn't exist!");
                                    } catch (Exception e) {
                                        VelocityLang.RC_FAMILY_ERROR.send(logger,"Something prevented us from doing that!\n"+e.getMessage());
                                    }
                                    return 1;
                                })
                        )
                )
            )
            .then(LiteralArgumentBuilder.<CommandSource>literal("parties")
                    .executes(context -> {
                        try {
                            api.services().partyService().orElseThrow().dump().forEach(party -> {
                                logger.log(party.toString());
                            });
                        } catch (Exception e) {
                            VelocityLang.RC_FAMILY_ERROR.send(logger,"Something prevented us from getting the parties!\n"+e.getMessage());
                        }

                        return Command.SINGLE_SUCCESS;
                    })
            )
            .then(LiteralArgumentBuilder.<CommandSource>literal("reload")
                    .executes(context -> {
                        logger.log("Reloading the proxy...");
                        try {
                            DefaultConfig defaultConfig = DefaultConfig.newConfig(new File(api.dataFolder(), "config.yml"), "velocity_config_template.yml");
                            if(!defaultConfig.generate()) {
                                throw new IllegalStateException("Unable to load or create config.yml!");
                            }
                            defaultConfig.register();

                            api.reloadServices();
                            logger.log("Done reloading!");

                            VelocityLang.RC_ROOT_USAGE.send(logger);
                            return 1;
                        } catch (Exception e) {
                            logger.error(e.getMessage(),e);
                        }
                        return 0;
                    })
            )
            .then(LiteralArgumentBuilder.<CommandSource>literal("send")
                    .executes(context -> {
                        VelocityLang.RC_SEND_USAGE.send(logger);
                        return Command.SINGLE_SUCCESS;
                    })
                    .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                            .executes(context -> {
                                VelocityLang.RC_SEND_USAGE.send(logger);
                                return Command.SINGLE_SUCCESS;
                            })
                            .then(RequiredArgumentBuilder.<CommandSource, String>argument("familyName", StringArgumentType.greedyString())
                                    .executes(context -> {
                                        try {
                                            String familyName = context.getArgument("familyName", String.class);
                                            String username = context.getArgument("username", String.class);

                                            Player player = api.velocityServer().getPlayer(username).orElse(null);
                                            if(player == null) {
                                                logger.send(VelocityLang.RC_SEND_NO_PLAYER.build(username));
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            BaseServerFamily family = api.services().familyService().find(familyName);
                                            if(family == null) {
                                                logger.send(VelocityLang.RC_SEND_NO_FAMILY.build(familyName));
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            if(!(family instanceof PlayerFocusedServerFamily)) {
                                                VelocityLang.RC_FAMILY_ERROR.send(logger,"You can only directly send player to scalar and static families!");
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            ((PlayerFocusedServerFamily) family).connect(player);
                                        } catch (Exception e) {
                                            logger.send(VelocityLang.BOXED_MESSAGE_COLORED.build(Component.text("There was an issue using that command! "+e.getMessage()), NamedTextColor.RED));
                                        }
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
                    .then(LiteralArgumentBuilder.<CommandSource>literal("server")
                            .executes(context -> {
                                VelocityLang.RC_SEND_USAGE.send(logger);
                                return Command.SINGLE_SUCCESS;
                            })
                            .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                                    .executes(context -> {
                                        VelocityLang.RC_SEND_USAGE.send(logger);
                                        return Command.SINGLE_SUCCESS;
                                    })
                                    .then(RequiredArgumentBuilder.<CommandSource, String>argument("serverName", StringArgumentType.greedyString())
                                            .executes(context -> {
                                                try {
                                                    String serverName = context.getArgument("serverName", String.class);
                                                    String username = context.getArgument("username", String.class);

                                                    Player player = api.velocityServer().getPlayer(username).orElse(null);
                                                    if (player == null) {
                                                        logger.send(VelocityLang.RC_SEND_NO_PLAYER.build(username));
                                                        return Command.SINGLE_SUCCESS;
                                                    }

                                                    RegisteredServer registeredServer = api.velocityServer().getServer(serverName).orElse(null);
                                                    if (registeredServer == null) {
                                                        logger.send(VelocityLang.RC_SEND_NO_SERVER.build(serverName));
                                                        return Command.SINGLE_SUCCESS;
                                                    }

                                                    PlayerServer server = api.services().serverService().search(registeredServer.getServerInfo());
                                                    if (server == null) {
                                                        logger.send(VelocityLang.RC_SEND_NO_SERVER.build(serverName));
                                                        return Command.SINGLE_SUCCESS;
                                                    }

                                                    server.connect(player);
                                                } catch (Exception e) {
                                                    logger.send(VelocityLang.BOXED_MESSAGE_COLORED.build(Component.text("There was an issue using that command! "+e.getMessage()), NamedTextColor.RED));
                                                }

                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                    )
            )
            .build();

        // BrigadierCommand implements Command
        return new BrigadierCommand(rusty);
    }
}