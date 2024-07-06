package group.aelysium.rustyconnector.plugin.velocity;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import group.aelysium.rustyconnector.toolkit.common.cache.CacheableMessage;
import group.aelysium.rustyconnector.proxy.family.Family;
import group.aelysium.rustyconnector.proxy.family.ranked_family.RankedFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.remote_storage.Storage;
import group.aelysium.rustyconnector.toolkit.proxy.connection.ConnectionResult;
import group.aelysium.rustyconnector.toolkit.proxy.connection.IPlayerConnectable;
import group.aelysium.rustyconnector.toolkit.proxy.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.proxy.util.DependencyInjector;
import group.aelysium.rustyconnector.proxy.family.scalar_family.ScalarFamily;
import group.aelysium.rustyconnector.proxy.family.static_family.StaticFamily;
import group.aelysium.rustyconnector.proxy.family.dynamic_scale.K8Service;
import group.aelysium.rustyconnector.toolkit.proxy.lang.ProxyLang;
import group.aelysium.rustyconnector.proxy.family.mcloader.MCLoader;
import group.aelysium.rustyconnector.toolkit.common.cache.MessageCache;
import io.fabric8.kubernetes.api.model.Pod;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class CommandRusty {
    public static BrigadierCommand create(DependencyInjector.DI3<Flame, PluginLogger, MessageCache> dependencies) {
        Flame flame = dependencies.d1();
        PluginLogger logger = dependencies.d2();
        MessageCache messageCacheService = dependencies.d3();

        LiteralCommandNode<CommandSource> rusty = LiteralArgumentBuilder
            .<CommandSource>literal("rc")
            .requires(source -> source instanceof ConsoleCommandSource)
            .executes(context -> {
                logger.send(ProxyLang.RC_ROOT_USAGE);
                return Command.SINGLE_SUCCESS;
            })
            .then(Message.build(flame, logger, messageCacheService))
            .then(FamilyC.build(flame, logger, messageCacheService))
            .then(Send.build(flame, logger, messageCacheService))
            .then(Debug.build(flame, logger, messageCacheService))
            .then(Reload.build(flame, logger, messageCacheService))
            .then(K8.build(flame, logger, messageCacheService))
            .then(Database.build(flame, logger, messageCacheService))
            .then(LiteralArgumentBuilder.<CommandSource>literal("hug")
                    .executes(context -> {
                        logger.send(Component.text("Awwwwww! Hug <3", NamedTextColor.LIGHT_PURPLE));
                        return Command.SINGLE_SUCCESS;
                    }))
            .build();

        // BrigadierCommand implements Command
        return new BrigadierCommand(rusty);
    }
}

class Message {
    public static ArgumentBuilder<CommandSource, ?> build(Flame flame, PluginLogger logger, MessageCache messageCacheService) {
        return LiteralArgumentBuilder.<CommandSource>literal("message")
                .executes(context -> {
                    logger.send(ProxyLang.RC_MESSAGE_ROOT_USAGE);
                    return Command.SINGLE_SUCCESS;
                })
                .then(listMessages(flame, logger, messageCacheService))
                .then(getMessage(flame, logger, messageCacheService));
    }

    private static ArgumentBuilder<CommandSource, ?> listMessages(Flame flame, PluginLogger logger, MessageCache messageCacheService) {
        return LiteralArgumentBuilder.<CommandSource>literal("list")
                .executes(context -> {
                    new Thread(() -> {
                        try {
                            if(messageCacheService.size() > 10) {
                                int numberOfPages = Math.floorDiv(messageCacheService.size(),10) + 1;

                                List<CacheableMessage> messagesPage = messageCacheService.fetchMessagesPage(1);

                                ProxyLang.RC_MESSAGE_PAGE.send(logger,messagesPage,1,numberOfPages);

                                return;
                            }

                            List<CacheableMessage> messages = messageCacheService.messages();

                            ProxyLang.RC_MESSAGE_PAGE.send(logger,messages,1,1);

                        } catch (Exception e) {
                            logger.send(Component.text("There was an issue getting those messages!\n"+e.getMessage(), NamedTextColor.RED));
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

                                    ProxyLang.RC_MESSAGE_PAGE.send(logger,messages,pageNumber,numberOfPages);
                                } catch (Exception e) {
                                    logger.send(Component.text("There was an issue getting those messages!\n"+e.getMessage(), NamedTextColor.RED));
                                }

                            }).start();
                            return Command.SINGLE_SUCCESS;
                        })
                );
    }

    private static ArgumentBuilder<CommandSource, ?> getMessage(Flame flame, PluginLogger logger, MessageCache messageCacheService) {
        return LiteralArgumentBuilder.<CommandSource>literal("get")
                .executes(context -> {
                    logger.send(ProxyLang.RC_MESSAGE_GET_USAGE);

                    return Command.SINGLE_SUCCESS;
                })
                .then(RequiredArgumentBuilder.<CommandSource, Long>argument("snowflake", LongArgumentType.longArg())
                        .executes(context -> {
                            try {
                                Long snowflake = context.getArgument("snowflake", Long.class);

                                CacheableMessage message = messageCacheService.findMessage(snowflake);

                                ProxyLang.RC_MESSAGE_GET_MESSAGE.send(logger, message);
                            } catch (Exception e) {
                                ProxyLang.RC_MESSAGE_ERROR.send(logger,"There's no saved message with that ID!");
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                );
    }
}
class FamilyC {
    public static ArgumentBuilder<CommandSource, ?> build(Flame flame, PluginLogger logger, MessageCache messageCacheService) {
        return LiteralArgumentBuilder.<CommandSource>literal("family")
                .executes(context -> {
                    try {
                        ProxyLang.RC_FAMILY.send(logger);
                    } catch (Exception e) {
                        logger.send(Component.text("Something prevented us from getting the families!\n"+e.getMessage(), NamedTextColor.RED));
                    }

                    return Command.SINGLE_SUCCESS;
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("familyName", StringArgumentType.string())
                        .executes(context -> {
                            try {
                                String familyName = context.getArgument("familyName", String.class);
                                Family family = new Family.Reference(familyName).get();

                                if(family instanceof ScalarFamily)
                                    ProxyLang.RC_SCALAR_FAMILY_INFO.send(logger, (ScalarFamily) family, false);
                                if(family instanceof StaticFamily)
                                    ProxyLang.RC_STATIC_FAMILY_INFO.send(logger, (StaticFamily) family, false);
                                if(family instanceof RankedFamily)
                                    ProxyLang.RC_RANKED_FAMILY_INFO.send(logger, (RankedFamily) family, false);
                            } catch (NoSuchElementException e) {
                                logger.send(Component.text("A family with that word_id doesn't exist!", NamedTextColor.RED));
                            } catch (Exception e) {
                                logger.send(Component.text("Something prevented us from getting that family!\n"+e.getMessage(), NamedTextColor.RED));
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(resetIndex(flame, logger, messageCacheService))
                        .then(sort(flame, logger, messageCacheService))
                        .then(locked(flame, logger, messageCacheService))
                );
    }

    private static ArgumentBuilder<CommandSource, ?> resetIndex(Flame flame, PluginLogger logger, MessageCache messageCacheService) {
        return LiteralArgumentBuilder.<CommandSource>literal("resetIndex")
                .executes(context -> {
                    try {
                        String familyName = context.getArgument("familyName", String.class);
                        Family family = new Family.Reference(familyName).get();


                        family.loadBalancer().executeNow(lb->{
                            lb.resetIndex();

                            if(family instanceof ScalarFamily)
                                ProxyLang.RC_SCALAR_FAMILY_INFO.send(logger, (ScalarFamily) family, false);
                            if(family instanceof StaticFamily)
                                ProxyLang.RC_STATIC_FAMILY_INFO.send(logger, (StaticFamily) family, false);
                            if(family instanceof RankedFamily)
                                ProxyLang.RC_RANKED_FAMILY_INFO.send(logger, (RankedFamily) family, false);
                        }, () -> {throw new NullPointerException("This family is currently not available. Try again later!");});
                    } catch (NoSuchElementException e) {
                        logger.send(Component.text("A family with that word_id doesn't exist!", NamedTextColor.RED));
                    } catch (Exception e) {
                        logger.send(Component.text("Something prevented us from doing that!\n"+e.getMessage(), NamedTextColor.RED));
                    }
                    return Command.SINGLE_SUCCESS;
                });
    }

    private static ArgumentBuilder<CommandSource, ?> sort(Flame flame, PluginLogger logger, MessageCache messageCacheService) {
        return LiteralArgumentBuilder.<CommandSource>literal("sort")
                .executes(context -> {
                    try {
                        String familyName = context.getArgument("familyName", String.class);
                        Family family = new Family.Reference(familyName).get();

                        family.loadBalancer().executeNow(lb->{
                            lb.completeSort();

                            if(family instanceof ScalarFamily)
                                ProxyLang.RC_SCALAR_FAMILY_INFO.send(logger, (ScalarFamily) family, false);
                            if(family instanceof StaticFamily)
                                ProxyLang.RC_STATIC_FAMILY_INFO.send(logger, (StaticFamily) family, false);
                            if(family instanceof RankedFamily)
                                ProxyLang.RC_RANKED_FAMILY_INFO.send(logger, (RankedFamily) family, false);
                        }, () -> {throw new NullPointerException("This family is currently not available. Try again later!");});

                    } catch (NoSuchElementException e) {
                        logger.send(Component.text("A family with that word_id doesn't exist!", NamedTextColor.RED));
                    } catch (Exception e) {
                        logger.send(Component.text("Something prevented us from doing that!\n"+e.getMessage(), NamedTextColor.RED));
                    }
                    return 1;
                });
    }

    private static ArgumentBuilder<CommandSource, ?> locked(Flame flame, PluginLogger logger, MessageCache messageCacheService) {
        return LiteralArgumentBuilder.<CommandSource>literal("locked")
                .executes(context -> {
                    try {
                        String familyName = context.getArgument("familyName", String.class);
                        Family family = new Family.Reference(familyName).get();

                        if(family instanceof ScalarFamily)
                            ProxyLang.RC_SCALAR_FAMILY_INFO.send(logger, (ScalarFamily) family, true);
                        if(family instanceof StaticFamily)
                            ProxyLang.RC_STATIC_FAMILY_INFO.send(logger, (StaticFamily) family, true);
                        if(family instanceof RankedFamily)
                            ProxyLang.RC_RANKED_FAMILY_INFO.send(logger, (RankedFamily) family, true);
                    } catch (NoSuchElementException e) {
                        logger.send(Component.text("A family with that word_id doesn't exist!", NamedTextColor.RED));
                    } catch (Exception e) {
                        logger.send(Component.text("Something prevented us from doing that!\n"+e.getMessage(), NamedTextColor.RED));
                    }
                    return 1;
                });
    }

    private static ArgumentBuilder<CommandSource, ?> players(Flame flame, PluginLogger logger, MessageCache messageCacheService) {
        return LiteralArgumentBuilder.<CommandSource>literal("players")
                .executes(context -> {
                    try {
                        String familyName = context.getArgument("familyName", String.class);
                        Family family = new Family.Reference(familyName).get();

                        String playerNames = "";
                        List<com.velocitypowered.api.proxy.Player> players = family.players();
                        com.velocitypowered.api.proxy.Player lastPlayer = players.get(players.size() - 1);
                        for (com.velocitypowered.api.proxy.Player player : players) {
                            if(player.equals(lastPlayer)) {
                                playerNames = playerNames + player.getUsername();
                                break;
                            }
                            playerNames = playerNames + player.getUsername() + ", ";
                        }

                        logger.send(Component.text(playerNames));
                    } catch (NoSuchElementException e) {
                        ProxyLang.RC_FAMILY.send(logger);
                        logger.send(Component.text("A family with that word_id doesn't exist!", NamedTextColor.RED));
                    } catch (Exception e) {
                        ProxyLang.RC_FAMILY.send(logger);
                        logger.send(Component.text("Something prevented us from doing that!\n"+e.getMessage(), NamedTextColor.RED));
                    }
                    return 1;
                });
    }
}
class Send {
    public static ArgumentBuilder<CommandSource, ?> build(Flame flame, PluginLogger logger, MessageCache messageCacheService) {
        return LiteralArgumentBuilder.<CommandSource>literal("send")
                .executes(context -> {
                    logger.send(ProxyLang.RC_SEND_USAGE);
                    return Command.SINGLE_SUCCESS;
                })
                .then(defaultSender(flame, logger, messageCacheService))
                .then(serverSender(flame, logger, messageCacheService));
    }

    private static ArgumentBuilder<CommandSource, ?> defaultSender(Flame flame, PluginLogger logger, MessageCache messageCacheService) {
        return RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                .executes(context -> {
                    logger.send(ProxyLang.RC_SEND_USAGE);
                    return Command.SINGLE_SUCCESS;
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("familyName", StringArgumentType.greedyString())
                        .executes(context -> {
                            String familyName = context.getArgument("familyName", String.class);
                            String username = context.getArgument("username", String.class);

                            try {
                                com.velocitypowered.api.proxy.Player fetchedPlayer = Tinder.get().velocityServer().getPlayer(username).orElse(null);
                                if(fetchedPlayer == null) {
                                    logger.send(ProxyLang.RC_SEND_NO_PLAYER.build(username));
                                    return Command.SINGLE_SUCCESS;
                                }
                                Player player = new Player(fetchedPlayer);

                                Family family = new Family.Reference(familyName).get();

                                if(player.server().orElseThrow().family().equals(family)) {
                                    logger.send(ProxyLang.RC_SEND_NO_FAMILY.build(familyName));
                                    return Command.SINGLE_SUCCESS;
                                }

                                IPlayerConnectable.Request request = family.connect(player);
                                ConnectionResult result = request.result().get(30, TimeUnit.SECONDS);

                                if(result.connected()) return Command.SINGLE_SUCCESS;

                                player.sendMessage(result.message());
                            } catch (NoSuchElementException e) {
                                logger.send(ProxyLang.RC_SEND_NO_FAMILY.build(familyName));
                            } catch (Exception e) {
                                e.printStackTrace();
                                logger.send(Component.text("There was an issue using that command! "+e.getMessage(), NamedTextColor.RED));
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                );
    }

    private static ArgumentBuilder<CommandSource, ?> serverSender(Flame flame, PluginLogger logger, MessageCache messageCacheService) {
        return LiteralArgumentBuilder.<CommandSource>literal("server")
                .executes(context -> {
                    logger.send(ProxyLang.RC_SEND_USAGE);
                    return Command.SINGLE_SUCCESS;
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                        .executes(context -> {
                            logger.send(ProxyLang.RC_SEND_USAGE);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("serverUUID", StringArgumentType.greedyString())
                                .executes(context -> {
                                    try {
                                        UUID serverUUID = UUID.fromString(context.getArgument("serverUUID", String.class));
                                        String username = context.getArgument("username", String.class);

                                        // Uses this first so that we can start by checking if the player is online.
                                        Player player = new IPlayer.UsernameReference(username).get();
                                        if (!player.online()) {
                                            logger.send(ProxyLang.RC_SEND_NO_PLAYER.build(username));
                                            return Command.SINGLE_SUCCESS;
                                        }

                                        MCLoader server;
                                        try {
                                            server = new MCLoader.Reference(serverUUID).get();
                                        } catch (Exception ignore) {
                                            logger.send(ProxyLang.RC_SEND_NO_SERVER.build(serverUUID.toString()));
                                            return Command.SINGLE_SUCCESS;
                                        }

                                        server.connect(player);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        logger.send(Component.text("There was an issue using that command! "+e.getMessage(), NamedTextColor.RED));
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                );
    }
}
class Debug {
    public static ArgumentBuilder<CommandSource, ?> build(Flame flame, PluginLogger logger, MessageCache messageCacheService) {
        return LiteralArgumentBuilder.<CommandSource>literal("debug")
                .executes(context -> {
                    flame.bootLog().forEach(logger::send);
                    return Command.SINGLE_SUCCESS;
                });
    }
}
class Reload {
    public static ArgumentBuilder<CommandSource, ?> build(Flame flame, PluginLogger logger, MessageCache messageCacheService) {
        return LiteralArgumentBuilder.<CommandSource>literal("reload")
                .executes(context -> {
                    logger.log("Reloading the proxy...");
                    try {
                        Tinder.get().rekindle();
                        logger.log("Done reloading!");
                        return 1;
                    } catch (Exception e) {
                        logger.error(e.getMessage(),e);
                    }
                    return 0;
                });
    }
}
class K8 {
    public static ArgumentBuilder<CommandSource, ?> build(Flame flame, PluginLogger logger, MessageCache messageCacheService) {
        return LiteralArgumentBuilder.<CommandSource>literal("k8") // k8 createPod <familyName> <containerName> <containerPort>
                .then(LiteralArgumentBuilder.<CommandSource>literal("createPod")
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("familyName", StringArgumentType.string())
                                .then(RequiredArgumentBuilder.<CommandSource, String>argument("containerName", StringArgumentType.string())
                                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("containerPort", StringArgumentType.string())
                                                .then(RequiredArgumentBuilder.<CommandSource, String>argument("containerImage", StringArgumentType.greedyString())
                                                        .executes(context -> {
                                                            try {
                                                                String familyName = context.getArgument("familyName", String.class);
                                                                String containerImage = context.getArgument("containerImage", String.class);

                                                                K8Service k8 = new K8Service();
                                                                k8.createPod(familyName, containerImage);
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }

                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                )
                                        )
                                )
                        ))
                .then(LiteralArgumentBuilder.<CommandSource>literal("deletePod") // k8 deletePod <podName> <familyName>
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("podName", StringArgumentType.string())
                                .then(RequiredArgumentBuilder.<CommandSource, String>argument("familyName", StringArgumentType.string())
                                        .executes(context -> {
                                            try {
                                                String podName = context.getArgument("podName", String.class);
                                                String familyName = context.getArgument("familyName", String.class);

                                                K8Service k8 = new K8Service();
                                                k8.deletePod(podName, familyName);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        ))
                .then(LiteralArgumentBuilder.<CommandSource>literal("listPods") // k8 deletePod <podName> <familyName>
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("familyName", StringArgumentType.string())
                                .executes(context -> {
                                    try {
                                        String familyName = context.getArgument("familyName", String.class);

                                        K8Service k8 = new K8Service();
                                        List<Pod> pods = k8.familyPods(familyName);

                                        pods.forEach(p -> System.out.println(p.getMetadata().getName()));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                );
    }
}
class Database {
    public static ArgumentBuilder<CommandSource, ?> build(Flame flame, PluginLogger logger, MessageCache messageCacheService) {
        return LiteralArgumentBuilder.<CommandSource>literal("database") // k8 createPod <familyName> <containerName> <containerPort>
                .then(bulk(flame, logger, messageCacheService))
                .then(LiteralArgumentBuilder.literal("players"))
                .then(LiteralArgumentBuilder.literal("residence"))
                .then(LiteralArgumentBuilder.literal("friends"))
                ;
    }

    private static ArgumentBuilder<CommandSource, ?> bulk(Flame flame, PluginLogger logger, MessageCache messageCacheService) {
        return LiteralArgumentBuilder.<CommandSource>literal("bulk")
                .then(LiteralArgumentBuilder.<CommandSource>literal("purgeGameRecords")
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("gameId", StringArgumentType.string())
                                .executes(context -> {
                                    String gameId = context.getArgument("gameId", String.class);

                                    Storage storage = flame.services().storage();
                                    storage.database().ranks().deleteGame(gameId);
                                    logger.log("Successfully purged all rank records from "+gameId);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(LiteralArgumentBuilder.<CommandSource>literal("purgeInvalidGameSchemas")
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("gameId", StringArgumentType.string())
                                .executes(context -> {
                                    String gameId = context.getArgument("gameId", String.class);

                                    Storage storage = flame.services().storage();
                                    storage.database().ranks().deleteGame(gameId);
                                    logger.log("Successfully purged all rank records from "+gameId);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(LiteralArgumentBuilder.literal("players"))
                .then(LiteralArgumentBuilder.literal("residence"))
                .then(LiteralArgumentBuilder.literal("friends"))
                ;
    }
}