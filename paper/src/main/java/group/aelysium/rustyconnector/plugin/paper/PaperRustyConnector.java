package group.aelysium.rustyconnector.plugin.paper;

import group.aelysium.declarative_yaml.DeclarativeYAML;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.RustyConnector;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.lang.LangLibrary;
import group.aelysium.rustyconnector.plugin.common.command.CommonCommands;
import group.aelysium.rustyconnector.plugin.common.command.ValidateClient;
import group.aelysium.rustyconnector.plugin.common.config.GitOpsConfig;
import group.aelysium.rustyconnector.plugin.paper.command.CommandRusty;
import group.aelysium.rustyconnector.plugin.paper.command.PaperClient;
import group.aelysium.rustyconnector.plugin.paper.config.DefaultConfig;
import group.aelysium.rustyconnector.plugin.paper.lang.PaperLang;
import group.aelysium.rustyconnector.server.ServerKernel;
import group.aelysium.rustyconnector.server.magic_link.WebSocketMagicLink;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.paper.PaperCommandManager;

import java.util.concurrent.ForkJoinPool;

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
                    p.fetchPlugin(LangLibrary.class).onStart(l -> l.registerLangNodes(PaperLang.class));
                } catch (Exception e) {
                    RC.Error(Error.from(e));
                }
                try {
                    p.fetchPlugin("MagicLink").onStart(l -> ((WebSocketMagicLink) l).connect());
                } catch (Exception e) {
                    RC.Error(Error.from(e));
                }
            });

            LegacyPaperCommandManager<PaperClient> commandManager = new LegacyPaperCommandManager<>(
                    this,
                    ExecutionCoordinator.asyncCoordinator(),
                    SenderMapper.create(
                            sender -> new PaperClient(sender),
                            client -> client.toSender()
                    )
            );
            commandManager.registerCommandPreProcessor(new ValidateClient<>());

            AnnotationParser<PaperClient> annotationParser = new AnnotationParser<>(commandManager, PaperClient.class);
            annotationParser.parse(new CommonCommands());
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