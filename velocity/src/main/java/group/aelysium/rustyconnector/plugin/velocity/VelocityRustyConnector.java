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
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.RustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.config.*;
import group.aelysium.rustyconnector.proxy.ProxyKernel;
import group.aelysium.rustyconnector.proxy.family.FamilyRegistry;
import group.aelysium.rustyconnector.proxy.family.scalar_family.ScalarFamily;
import group.aelysium.rustyconnector.shaded.org.reflections.Reflections;
import group.aelysium.rustyconnector.shaded.org.reflections.util.ClasspathHelper;
import group.aelysium.rustyconnector.shaded.org.reflections.util.ConfigurationBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bstats.velocity.Metrics;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.velocity.VelocityCommandManager;
import org.slf4j.Logger;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
            ProxyKernel.Tinder tinder = new ProxyKernel.Tinder(
                    ServerUUIDConfig.New().uuid(),
                    new VelocityProxyAdapter(server, logger),
                    MagicLinkConfig.New().tinder()
            );
            tinder.whitelist(ProxyWhitelistConfig.New().tinder());

            DefaultConfig config = DefaultConfig.New();
            FamilyRegistry.Tinder families = new FamilyRegistry.Tinder();
            families.addFamily(ScalarFamilyConfig.New("lobby").tinder().flux());
            for (File file : Objects.requireNonNull((new File("plugins/rustyconnector/scalar_families")).listFiles())) {
                if(!(file.getName().endsWith(".yml") || file.getName().endsWith(".yaml"))) continue;
                int extensionIndex = file.getName().lastIndexOf(".");
                String name = file.getName().substring(0, extensionIndex);
                ScalarFamily.Tinder family = ScalarFamilyConfig.New(name).tinder();
                families.addFamily(family.flux());
                if(name.equalsIgnoreCase(config.rootFamily())) families.setRootFamily(family.flux());
            }
            tinder.familyRegistry(families);

            ProxyKernel kernel = RustyConnector.Toolkit.registerAndIgnite(tinder);
            RC.P.Adapter().log(RC.P.Lang().lang().RUSTY_CONNECTOR(kernel.version()));

            kernel.EventManager().orElseThrow().listen(OnMCLoaderRegister.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if(!this.server.getConfiguration().isOnlineMode())
            this.logger.send(RC.P.Lang().lang().boxed(Component.text("Your network is running in offline mode! YOU WILL RECEIVE NO SUPPORT AT ALL WITH RUSTYCONNECTOR!", NamedTextColor.RED)));

        // Velocity requires that at least one server is always defined in velocity.toml
        if(this.server.getConfiguration().getServers().size() > 1)
            this.logger.send(RC.P.Lang().lang().boxed(
                    Component.join(
                            JoinConfiguration.newlines(),
                            Component.text("Your network is identified as having multiple, pre-defined, non-RC servers in it!"),
                            Component.text("Please note that you will receive no help in regards to making RC work with predefined servers!")
                    )
                    , NamedTextColor.RED
            ));
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
