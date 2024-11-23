package group.aelysium.rustyconnector.plugin.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import group.aelysium.ara.Particle;
import group.aelysium.declarative_yaml.DeclarativeYAML;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.RustyConnector;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.events.EventManager;
import group.aelysium.rustyconnector.common.lang.LangLibrary;
import group.aelysium.rustyconnector.plugin.common.command.CommonCommands;
import group.aelysium.rustyconnector.plugin.common.config.GitOpsConfig;
import group.aelysium.rustyconnector.plugin.common.config.ServerIDConfig;
import group.aelysium.rustyconnector.plugin.velocity.commands.CommandRusty;
import group.aelysium.rustyconnector.plugin.velocity.commands.CommandServer;
import group.aelysium.rustyconnector.plugin.common.command.ValidateClient;
import group.aelysium.rustyconnector.plugin.velocity.commands.VelocityClient;
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
import group.aelysium.rustyconnector.proxy.family.scalar_family.ScalarFamily;
import org.bstats.velocity.Metrics;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.velocity.VelocityCommandManager;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VelocityRustyConnector implements PluginContainer {
    private final ExecutorService commandExecutor = Executors.newCachedThreadPool();
    private final Metrics.Factory metricsFactory;
    private final PluginLogger logger;
    private final ProxyServer server;
    private final Path dataFolder;
    private final AnnotationParser<VelocityClient> annotationParser;

    @Inject
    public VelocityRustyConnector(ProxyServer server, Logger logger, @DataDirectory Path dataFolder, Metrics.Factory metricsFactory) {
        this.logger = new PluginLogger(logger, server);
        this.server = server;
        this.metricsFactory = metricsFactory;
        this.dataFolder = dataFolder;

        CommandManager<VelocityClient> commandManager = new VelocityCommandManager<>(
                this,
                server,
                ExecutionCoordinator.asyncCoordinator(),
                SenderMapper.create(
                        sender -> new VelocityClient(sender),
                        client -> client.toSender()
                )
        );
        commandManager.registerCommandPreProcessor(new ValidateClient<>());
        this.annotationParser = new AnnotationParser<>(
                commandManager,
                VelocityClient.class
        );
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
            {
                GitOpsConfig config = GitOpsConfig.New();
                if(config != null) DeclarativeYAML.registerRepository("rustyconnector", config.config());
            }

            ProxyKernel.Tinder tinder = new ProxyKernel.Tinder(
                    ServerIDConfig.New().id(),
                    new VelocityProxyAdapter(server, logger),
                    MagicLinkConfig.New().tinder()
            );

            RustyConnector.registerAndIgnite(tinder.flux());

            Particle.Flux<? extends ProxyKernel> kernelFlux = RustyConnector.Proxy().orElseThrow();
            kernelFlux.onStart(p->{
                try {
                    p.fetchPlugin(LangLibrary.class).onStart(l -> l.registerLangNodes(VelocityLang.class));
                } catch (Exception e) {
                    RC.Error(Error.from(e));
                }
                try {
                    p.fetchPlugin(EventManager.class).onStart(m -> {
                        m.listen(OnServerRegister.class);
                        m.listen(OnServerUnregister.class);
                        m.listen(OnServerTimeout.class);
                        m.listen(new OnFamilyLifecycle(this.server));
                    });
                } catch (Exception e) {
                    RC.Error(Error.from(e));
                }
                try {
                    p.fetchPlugin(FamilyRegistry.class).onStart(f -> {
                        try {
                            DefaultConfig config = DefaultConfig.New();
                            ScalarFamilyConfig.New(config.rootFamily()); // Literally just exists to ensure the root family exists and then generate the scalar family folder
                            for (File file : Objects.requireNonNull((new File("plugins/rustyconnector/scalar_families")).listFiles())) {
                                if(!(file.getName().endsWith(".yml") || file.getName().endsWith(".yaml"))) continue;
                                int extensionIndex = file.getName().lastIndexOf(".");
                                String name = file.getName().substring(0, extensionIndex);
                                ScalarFamily.Tinder family = ScalarFamilyConfig.New(name).tinder();
                                Particle.Flux<? extends Family> flux = family.flux();
                                f.register(name, flux);
                                if(name.equalsIgnoreCase(config.rootFamily())) f.setRootFamily(name);
                                flux.observe();
                            }
                        } catch (Exception e) {
                            RC.Error(Error.from(e).whileAttempting("To boot up the FamilyRegistry."));
                        }
                    });
                } catch (Exception e) {
                    RC.Error(Error.from(e));
                }
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

        this.server.getCommandManager().unregister("server");
        this.annotationParser.parse(CommandServer.class);

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
