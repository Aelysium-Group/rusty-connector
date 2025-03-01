package group.aelysium.rustyconnector.plugin.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import group.aelysium.declarative_yaml.DeclarativeYAML;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.RustyConnector;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.errors.ErrorRegistry;
import group.aelysium.rustyconnector.common.events.EventManager;
import group.aelysium.rustyconnector.common.lang.EnglishAlphabet;
import group.aelysium.rustyconnector.common.lang.LangLibrary;
import group.aelysium.rustyconnector.common.modules.ModuleBuilder;
import group.aelysium.rustyconnector.common.modules.ModuleLoader;
import group.aelysium.rustyconnector.common.modules.ModuleParticle;
import group.aelysium.rustyconnector.plugin.common.command.Client;
import group.aelysium.rustyconnector.plugin.common.command.CommonCommands;
import group.aelysium.rustyconnector.plugin.common.config.ServerIDConfig;
import group.aelysium.rustyconnector.plugin.velocity.commands.CommandRusty;
import group.aelysium.rustyconnector.plugin.velocity.commands.CommandServer;
import group.aelysium.rustyconnector.plugin.velocity.config.*;
import group.aelysium.rustyconnector.plugin.velocity.event_handlers.rc.OnFamilyLifecycle;
import group.aelysium.rustyconnector.plugin.velocity.event_handlers.rc.OnServerRegister;
import group.aelysium.rustyconnector.plugin.velocity.event_handlers.rc.OnServerTimeout;
import group.aelysium.rustyconnector.plugin.velocity.event_handlers.rc.OnServerUnregister;
import group.aelysium.rustyconnector.plugin.velocity.event_handlers.velocity.*;
import group.aelysium.rustyconnector.plugin.velocity.lang.VelocityLang;
import group.aelysium.rustyconnector.proxy.ProxyKernel;
import group.aelysium.rustyconnector.proxy.family.Family;
import group.aelysium.rustyconnector.proxy.family.FamilyRegistry;
import group.aelysium.rustyconnector.proxy.family.load_balancing.*;
import group.aelysium.rustyconnector.proxy.family.scalar_family.ScalarFamily;
import group.aelysium.rustyconnector.proxy.magic_link.WebSocketMagicLink;
import group.aelysium.rustyconnector.proxy.player.PlayerRegistry;
import group.aelysium.rustyconnector.proxy.util.LiquidTimestamp;
import group.aelysium.rustyconnector.shaded.group.aelysium.ara.Flux;
import group.aelysium.rustyconnector.shaded.group.aelysium.ara.Flux;
import org.bstats.velocity.Metrics;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.velocity.VelocityCommandManager;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class VelocityRustyConnector implements PluginContainer {
    private final ExecutorService commandExecutor = Executors.newCachedThreadPool();
    private final Metrics.Factory metricsFactory;
    private final PluginLogger logger;
    private final ProxyServer server;
    private final Path dataFolder;
    private final AnnotationParser<Client> annotationParser;
    private final CommandManager<Client> commandManager;
    private final ModuleLoader loader = new ModuleLoader();

    @Inject
    public VelocityRustyConnector(ProxyServer server, Logger logger, @DataDirectory Path dataFolder, Metrics.Factory metricsFactory) {
        DeclarativeYAML.basePath("rustyconnector", "plugins/rustyconnector");
        DeclarativeYAML.basePath("rustyconnector-modules", "rc-modules");
        this.logger = new PluginLogger(logger, server);
        this.server = server;
        this.dataFolder = dataFolder;
        this.metricsFactory = metricsFactory;

        this.commandManager = new VelocityCommandManager<>(
                this,
                server,
                ExecutionCoordinator.asyncCoordinator(),
                SenderMapper.create(
                        sender -> {
                            if(sender instanceof ConsoleCommandSource console) return new VelocityClient.Console(console);
                            if(sender instanceof Player player) return new VelocityClient.Player(player);
                            return new VelocityClient.Other(sender);
                        },
                        Client::toSender
                )
        );
        this.annotationParser = new AnnotationParser<>(
                this.commandManager,
                Client.class
        );

        this.server.getCommandManager().unregister("server");
        this.annotationParser.parse(new CommonCommands());
        this.annotationParser.parse(new CommandRusty());
        this.annotationParser.parse(new CommandServer());
    }

    @Subscribe
    public void onLoad(ProxyInitializeEvent event) throws Exception {
        this.logger.log("Initializing RustyConnector...");

        if(!this.server.getConfiguration().isOnlineMode())
            logger.log("Offline mode detected");

        try {
            metricsFactory.make(this, 17972);
            this.logger.log("Registered to bstats!");
        } catch (Exception e) {
            e.printStackTrace();
            this.logger.log("Failed to register to bstats!");
        }

        try {
            this.logger.log("Building configuration...");
//            {
//                GitOpsConfig config = GitOpsConfig.New();
//                if(config != null) DeclarativeYAML.registerRepository("rustyconnector", config.config());
//            }

            RustyConnector.registerAndIgnite(Flux.using(()->{
                try {
                    return new ProxyKernel(
                        ServerIDConfig.Load(UUID.randomUUID().toString()).id(),
                        new VelocityProxyAdapter(server, logger, this.commandManager),
                        this.dataFolder,
                        this.dataFolder.resolve("../../rc-modules").normalize()
                    );
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));

            RustyConnector.Kernel(flux->{
                flux.onStart(kernel->{
                    try {
                        kernel.registerModule(new ModuleBuilder<>("ErrorRegistry", "Provides error handling services.") {
                            @Override
                            public ModuleParticle get() {
                                return new ErrorRegistry(false, 200);
                            }
                        });
                        
                        kernel.registerModule(new ModuleBuilder<>("LangLibrary", "Provides translatable lang messages that can be replaced and repurposed.") {
                            @Override
                            public ModuleParticle get() {
                                LangLibrary l = new LangLibrary(new EnglishAlphabet());
                                l.registerLangNodes(VelocityLang.class);
                                return l;
                            }
                        });
                        
                        kernel.registerModule(new ModuleBuilder<>("PlayerRegistry", "Provides uuid-username mappings for players connected to the network.") {
                            @Override
                            public ModuleParticle get() {
                                return new PlayerRegistry();
                            }
                        });
                        
                        kernel.registerModule(new ModuleBuilder<>("FamilyRegistry", "Provides itemized access for all families available on the RustyConnector kernel.") {
                            @Override
                            public ModuleParticle get() {
                                FamilyRegistry f = new FamilyRegistry();
                                try {
                                    DefaultConfig config = DefaultConfig.New();
                                    f.rootFamily(config.rootFamily());
                                    
                                    File directory = new File(DeclarativeYAML.basePath("rustyconnector")+"/scalar_families");
                                    if(!directory.exists()) directory.mkdirs();
                                    
                                    {
                                        File[] files = directory.listFiles();
                                        if (files == null || files.length == 0)
                                            ScalarFamilyConfig.New("lobby");
                                    }
                                    
                                    File[] files = directory.listFiles();
                                    if (files == null) return f;
                                    if (files.length == 0) return f;
                                    
                                    for (File file : files) {
                                        if(!(file.getName().endsWith(".yml") || file.getName().endsWith(".yaml"))) continue;
                                        int extensionIndex = file.getName().lastIndexOf(".");
                                        String name = file.getName().substring(0, extensionIndex);
                                        f.register(name, ScalarFamilyConfig.New(name).builder());
                                    }
                                } catch (Exception e) {
                                    RC.Error(Error.from(e).whileAttempting("To boot up the FamilyRegistry."));
                                }
                                return f;
                            }
                        });
                        
                        kernel.registerModule(new ModuleBuilder<>("EventManager", "Provides event handling services.") {
                            @Override
                            public ModuleParticle get() {
                                EventManager e = new EventManager();
                                e.listen(OnServerRegister.class);
                                e.listen(OnServerUnregister.class);
                                e.listen(OnServerTimeout.class);
                                e.listen(new OnFamilyLifecycle(VelocityRustyConnector.this.server));
                                return e;
                            }
                        });
                        
                        kernel.registerModule(new ModuleBuilder<>("MagicLink", "Provides cross-node packet communication via WebSockets.") {
                            @Override
                            public ModuleParticle get() {
                                try {
                                    return MagicLinkConfig.New().build();
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                        
                        LoadBalancerGeneratorExchange.registerBuilder("ROUND_ROBIN", config -> new ModuleBuilder<>("RR_LoadBalancer","Provides load balancing using the RoundRobin sorting algorithm.") {
                            @Override
                            public LoadBalancer get() {
                                return new RoundRobin(
                                    config.weighted(),
                                    config.persistence(),
                                    config.attempts()
                                );
                            }
                        });
                        LoadBalancerGeneratorExchange.registerBuilder("LEAST_CONNECTION", config -> new ModuleBuilder<>("LC_LoadBalancer","Provides load balancing using the LeastConnection sorting algorithm.") {
                            @Override
                            public LoadBalancer get() {
                                return new LeastConnection(
                                    config.weighted(),
                                    config.persistence(),
                                    config.attempts(),
                                    config.rebalance() == null ? LiquidTimestamp.from(15, TimeUnit.SECONDS) : config.rebalance()
                                );
                            }
                        });
                        LoadBalancerGeneratorExchange.registerBuilder("MOST_CONNECTION", config -> new ModuleBuilder<>("MC_LoadBalancer","Provides load balancing using the MostConnection sorting algorithm.") {
                            @Override
                            public LoadBalancer get() {
                                return new MostConnection(
                                    config.weighted(),
                                    config.persistence(),
                                    config.attempts(),
                                    config.rebalance() == null ? LiquidTimestamp.from(15, TimeUnit.SECONDS) : config.rebalance()
                                );
                            }
                        });
                    } catch (Exception e) {
                        RC.Error(Error.from(e).whileAttempting("To boot up the RustyConnnector Kernel."));
                    }
                });
                flux.onClose(()->{
                    LoadBalancerGeneratorExchange.clear();
                });

                loader.loadFromFolder(flux, "rc-modules");
            });

            RC.Lang("rustyconnector-wordmark").send(RC.Kernel().version());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if(!this.server.getConfiguration().isOnlineMode())
            RC.P.Lang().lang("rustyconnector-offlineMode").send();

        // Velocity requires that at least one server is always defined in velocity.toml
        if(this.server.getConfiguration().getServers().size() > 1)
            RC.P.Lang().lang("rustyconnector-hybrid").send();

        this.server.getEventManager().register(this, new OnPlayerChangeServer());
        this.server.getEventManager().register(this, new OnPlayerChooseInitialServer());
        this.server.getEventManager().register(this, new OnPlayerDisconnect());
        this.server.getEventManager().register(this, new OnPlayerKicked());
        this.server.getEventManager().register(this, new OnPlayerPreConnectServer(this.server));
    }

    @Subscribe
    public void onUnload(ProxyShutdownEvent event) {
        try {
            RustyConnector.unregister();
            loader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public PluginDescription getDescription() {
        return () -> "rustyconnector-velocity";
    }

    @Override
    public Optional<?> getInstance() {
        return Optional.of(this);
    }

    @Override
    public ExecutorService getExecutorService() {
        return this.commandExecutor;
    }
}
