package group.aelysium.rustyconnector.plugin.paper;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.RustyConnector;
import group.aelysium.rustyconnector.plugin.paper.config.DefaultConfig;
import group.aelysium.rustyconnector.proxy.util.Version;
import group.aelysium.rustyconnector.server.ServerFlame;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

public final class PaperRustyConnector extends JavaPlugin implements Listener {
    private final PluginLogger logger = new PluginLogger(this.getSLF4JLogger(), this.getServer());
    private final AnnotationParser<CommandSender> annotationParser;

    public PaperRustyConnector() {
        LegacyPaperCommandManager<CommandSender> commandManager = LegacyPaperCommandManager.createNative(
                this,
                ExecutionCoordinator.asyncCoordinator()
        );
        this.annotationParser = new AnnotationParser<>(commandManager, CommandSender.class);
    }

    @Override
    public void onEnable() {
        this.logger.log("Initializing RustyConnector...");

        try {
            //metricsFactory.make(this, 17972);
            this.logger.log("Registered to bstats!");
        } catch (Exception e) {
            e.printStackTrace();
            this.logger.log("Failed to register to bstats!");
        }

        //RootLanguageConfig config = RootLanguageConfig.construct(dataFolder);

        try {
            ServerFlame.Tinder tinder = DefaultConfig.New().data(this.getServer(), this.logger);
            RustyConnector.Toolkit.registerAndIgnite(tinder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            RustyConnector.Toolkit.Proxy().orElseThrow().access().get();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        RC.S.Adapter().log(RC.S.Lang().lang().RUSTY_CONNECTOR(new Version("0.0.0")));

        this.annotationParser.parse(new CommandRusty());
    }

    @Override
    public void onDisable() {
        try {
            RustyConnector.Toolkit.unregister();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}