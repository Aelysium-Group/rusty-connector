package group.aelysium.rustyconnector.plugin.paper;

import group.aelysium.declarative_yaml.DeclarativeYAML;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.RustyConnector;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.lang.LangLibrary;
import group.aelysium.rustyconnector.plugin.common.command.CommonCommands;
import group.aelysium.rustyconnector.plugin.common.command.ValidateClient;
import group.aelysium.rustyconnector.plugin.common.config.GitOpsConfig;
import group.aelysium.rustyconnector.plugin.common.config.PrivateKeyConfig;
import group.aelysium.rustyconnector.plugin.paper.command.CommandRusty;
import group.aelysium.rustyconnector.plugin.paper.command.PaperClient;
import group.aelysium.rustyconnector.plugin.paper.config.DefaultConfig;
import group.aelysium.rustyconnector.plugin.paper.lang.PaperLang;
import group.aelysium.rustyconnector.server.ServerKernel;
import group.aelysium.rustyconnector.server.magic_link.WebSocketMagicLink;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
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
            if(PrivateKeyConfig.Load().isEmpty()) {
                this.logger.send(Component.join(
                        JoinConfiguration.newlines(),
                        Component.empty(),
                        Component.empty(),
                        Component.empty(),
                        Component.empty(),
                        Component.text("Looks like I'm still waiting on a private.key from the proxy!", NamedTextColor.BLUE),
                        Component.text("You'll need to copy ", NamedTextColor.BLUE).append(Component.text("plugins/rustyconnector/metadata/private.key", NamedTextColor.YELLOW)).append(Component.text(" and paste it into this server in that same folder!", NamedTextColor.BLUE)),
                        Component.text("Both the proxy and I need to have the same private.key!", NamedTextColor.BLUE),
                        Component.empty(),
                        Component.empty(),
                        Component.empty()
                ));
                return;
            }

            {
                GitOpsConfig config = GitOpsConfig.New();
                if(config != null) DeclarativeYAML.registerRepository("rustyconnector", config.config());
            }

            ServerKernel.Tinder tinder = DefaultConfig.New().data(this.getServer(), this.logger);
            RustyConnector.registerAndIgnite(tinder.flux());
            RustyConnector.Server().orElseThrow().onStart(p->{
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
            RustyConnector.unregister();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}