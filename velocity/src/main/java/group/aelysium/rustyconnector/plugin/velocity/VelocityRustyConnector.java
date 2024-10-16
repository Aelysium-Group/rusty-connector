package group.aelysium.rustyconnector.plugin.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import group.aelysium.declarative_yaml.DeclarativeYAML;
import group.aelysium.declarative_yaml.GitOperator;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.RustyConnector;
import group.aelysium.rustyconnector.plugin.common.config.GitOpsConfig;
import group.aelysium.rustyconnector.plugin.common.config.ServerUUIDConfig;
import group.aelysium.rustyconnector.plugin.velocity.commands.CommandRusty;
import group.aelysium.rustyconnector.plugin.velocity.commands.CommandServer;
import group.aelysium.rustyconnector.plugin.velocity.config.*;
import group.aelysium.rustyconnector.plugin.velocity.lang.VelocityLang;
import group.aelysium.rustyconnector.proxy.ProxyKernel;
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
    private final AnnotationParser<CommandSource> annotationParser;

    @Inject
    public VelocityRustyConnector(ProxyServer server, Logger logger, @DataDirectory Path dataFolder, Metrics.Factory metricsFactory) {
        this.logger = new PluginLogger(logger, server);
        this.server = server;
        this.metricsFactory = metricsFactory;
        this.dataFolder = dataFolder;

        CommandManager<CommandSource> commandManager = new VelocityCommandManager<>(
                this,
                server,
                ExecutionCoordinator.asyncCoordinator(),
                SenderMapper.identity()
        );
        this.annotationParser = new AnnotationParser<>(
                commandManager,
                CommandSource.class
        );
        this.annotationParser.parse(new CommandRusty());
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
                    ServerUUIDConfig.New().uuid(),
                    new VelocityProxyAdapter(server, logger),
                    MagicLinkConfig.New().tinder()
            );

            DefaultConfig config = DefaultConfig.New();
            FamilyRegistry.Tinder families = new FamilyRegistry.Tinder();
            ScalarFamilyConfig.New(config.rootFamily()); // Literally just exists to ensure the root family exists and then generate the scalar family folder
            for (File file : Objects.requireNonNull((new File("plugins/rustyconnector/scalar_families")).listFiles())) {
                if(!(file.getName().endsWith(".yml") || file.getName().endsWith(".yaml"))) continue;
                int extensionIndex = file.getName().lastIndexOf(".");
                String name = file.getName().substring(0, extensionIndex);
                ScalarFamily.Tinder family = ScalarFamilyConfig.New(name).tinder();
                families.addFamily(name, family.flux());
                if(name.equalsIgnoreCase(config.rootFamily())) families.setRootFamily(name, family.flux());
            }
            tinder.familyRegistry(families);

            ProxyKernel kernel = RustyConnector.Toolkit.registerAndIgnite(tinder);
            kernel.Lang().onStart(p -> p.registerLangNodes(VelocityLang.class));

            kernel.EventManager().orElseThrow().listen(OnMCLoaderRegister.class);

            RC.Lang("rustyconnector-wordmark").send(kernel.version());
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
    }

    @Subscribe
    public void onUnload(ProxyShutdownEvent event) {
        try {
            RustyConnector.Toolkit.unregister();
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
