package group.aelysium.rustyconnector.plugin.paper;

import group.aelysium.declarative_yaml.DeclarativeYAML;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.RustyConnector;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.lang.LangLibrary;
import group.aelysium.rustyconnector.common.plugins.PluginLoader;
import group.aelysium.rustyconnector.plugin.common.command.Client;
import group.aelysium.rustyconnector.plugin.common.command.CommonCommands;
import group.aelysium.rustyconnector.plugin.common.config.GitOpsConfig;
import group.aelysium.rustyconnector.plugin.common.config.PrivateKeyConfig;
import group.aelysium.rustyconnector.plugin.serverCommon.CommandRusty;
import group.aelysium.rustyconnector.plugin.serverCommon.DefaultConfig;
import group.aelysium.rustyconnector.plugin.serverCommon.ServerLang;
import group.aelysium.rustyconnector.server.ServerKernel;
import group.aelysium.rustyconnector.server.magic_link.WebSocketMagicLink;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

import java.util.List;

public final class PaperRustyConnector extends JavaPlugin {
    private final PluginLoader loader = new PluginLoader(List.of(
            "io.papermc.paper"
    ));

    public PaperRustyConnector() {}

    @Override
    public void onEnable() {
        DeclarativeYAML.basePath("rustyconnector", "plugins/rustyconnector");
        DeclarativeYAML.basePath("rustyconnector-modules", "rc-modules");
        ConsoleCommandSender console = this.getServer().getConsoleSender();
        console.sendMessage("Initializing RustyConnector...");

        try {
            if(PrivateKeyConfig.Load().isEmpty()) {
                console.sendMessage(Component.join(
                        JoinConfiguration.newlines(),
                        Component.empty(),
                        Component.empty(),
                        Component.empty(),
                        Component.empty(),
                        Component.text("Looks like I'm still waiting on a private.key from the proxy!", NamedTextColor.BLUE),
                        Component.text("You'll need to copy ", NamedTextColor.BLUE).append(Component.text("plugins/rustyconnector/metadata/aes.private", NamedTextColor.YELLOW)).append(Component.text(" and paste it into this server in that same folder!", NamedTextColor.BLUE)),
                        Component.text("Both the proxy and I need to have the same aes.private!", NamedTextColor.BLUE),
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

            ServerKernel.Tinder tinder = DefaultConfig.New().data(
                    new PaperServerAdapter(this.getServer())
            );
            RustyConnector.registerAndIgnite(tinder.flux());
            RustyConnector.Kernel(flux->{
                flux.onStart(kernel -> {
                    try {
                        kernel.fetchPlugin("LangLibrary").onStart(l -> ((LangLibrary) l).registerLangNodes(ServerLang.class));
                    } catch (Exception e) {
                        RC.Error(Error.from(e));
                    }
                    try {
                        kernel.fetchPlugin("MagicLink").onStart(l -> ((WebSocketMagicLink) l).connect());
                    } catch (Exception e) {
                        RC.Error(Error.from(e));
                    }
                });
            });

            LegacyPaperCommandManager<Client> commandManager = new LegacyPaperCommandManager<>(
                    this,
                    ExecutionCoordinator.asyncCoordinator(),
                    SenderMapper.create(
                            sender -> {
                                if(sender instanceof ConsoleCommandSender source) return new PaperClient.Console(source);
                                if(sender instanceof Player source) return new PaperClient.Player(source);
                                return new PaperClient.Other(sender);
                            },
                            Client::toSender
                    )
            );

            AnnotationParser<Client> annotationParser = new AnnotationParser<>(
                    commandManager,
                    Client.class
            );
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