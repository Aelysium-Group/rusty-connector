package group.aelysium.rustyconnector.plugin.paper;

import group.aelysium.declarative_yaml.DeclarativeYAML;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.RustyConnector;
import group.aelysium.rustyconnector.common.lang.LangLibrary;
import group.aelysium.rustyconnector.plugin.common.config.GitOpsConfig;
import group.aelysium.rustyconnector.plugin.paper.config.DefaultConfig;
import group.aelysium.rustyconnector.plugin.paper.lang.PaperLang;
import group.aelysium.rustyconnector.server.ServerKernel;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

public final class PaperRustyConnector extends JavaPlugin {
    private final PluginLogger logger = new PluginLogger(this.getSLF4JLogger(), this.getServer());

    public PaperRustyConnector() {}

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

        try {
            {
                GitOpsConfig config = GitOpsConfig.New();
                if(config != null) DeclarativeYAML.registerRepository("rustyconnector", config.config());
            }

            ServerKernel.Tinder tinder = DefaultConfig.New().data(this.getServer(), this.logger);
            RustyConnector.Toolkit.registerAndIgnite(tinder.flux());
            RustyConnector.Toolkit.Server().orElseThrow().onStart(p->{
                try {
                    p.fetchPlugin(LangLibrary.class).onStart(l -> {
                        l.registerLangNodes(PaperLang.class);
                    });
                } catch (Exception ignore) {}
            });

            LegacyPaperCommandManager<CommandSender> commandManager = LegacyPaperCommandManager.createNative(
                    this,
                    ExecutionCoordinator.asyncCoordinator()
            );
            AnnotationParser<CommandSender> annotationParser = new AnnotationParser<>(commandManager, CommandSender.class);
            annotationParser.parse(new CommandRusty());
            RC.Lang("rustyconnector-wordmark").send(RC.Kernel().version());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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