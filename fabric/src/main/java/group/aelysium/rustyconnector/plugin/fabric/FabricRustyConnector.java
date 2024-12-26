package group.aelysium.rustyconnector.plugin.fabric;

import group.aelysium.declarative_yaml.DeclarativeYAML;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.RustyConnector;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.lang.LangLibrary;
import group.aelysium.rustyconnector.plugin.common.command.Client;
import group.aelysium.rustyconnector.plugin.common.command.CommonCommands;
import group.aelysium.rustyconnector.plugin.common.config.GitOpsConfig;
import group.aelysium.rustyconnector.plugin.common.config.PrivateKeyConfig;
import group.aelysium.rustyconnector.plugin.serverCommon.CommandRusty;
import group.aelysium.rustyconnector.plugin.serverCommon.DefaultConfig;
import group.aelysium.rustyconnector.plugin.serverCommon.ServerLang;
import group.aelysium.rustyconnector.server.ServerAdapter;
import group.aelysium.rustyconnector.server.ServerKernel;
import group.aelysium.rustyconnector.server.magic_link.WebSocketMagicLink;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.text.Text;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.fabric.FabricServerCommandManager;

import static net.kyori.adventure.text.Component.text;

public class FabricRustyConnector implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        FabricServerCommandManager<Client> commandManager = new FabricServerCommandManager<>(
                ExecutionCoordinator.asyncCoordinator(),
                SenderMapper.create(
                        sender -> {
                            if(sender.isExecutedByPlayer()) return new FabricClient.Player(sender);
                            if(sender.getEntity() == null && sender.getServer() != null) return new FabricClient.Console(sender);
                            return new FabricClient.Other(sender);
                        },
                        Client::toSender
                )
        );

        AnnotationParser<Client> annotationParser = new AnnotationParser<>(commandManager, Client.class);
        annotationParser.parse(new CommonCommands());
        annotationParser.parse(new CommandRusty());

        ServerLifecycleEvents.SERVER_STARTED.register(s -> {
            s.sendMessage(Text.of("Initializing RustyConnector..."));
            ServerAdapter adapter = new FabricServerAdapter(s);

            try {
                if(PrivateKeyConfig.Load().isEmpty()) {
                    adapter.log(Component.join(
                            JoinConfiguration.newlines(),
                            Component.empty(),
                            Component.empty(),
                            Component.empty(),
                            Component.empty(),
                            text("Looks like I'm still waiting on a private.key from the proxy!", NamedTextColor.BLUE),
                            text("You'll need to copy ", NamedTextColor.BLUE).append(text("plugins/rustyconnector/metadata/aes.private", NamedTextColor.YELLOW)).append(text(" and paste it into this server in that same folder!", NamedTextColor.BLUE)),
                            text("Both the proxy and I need to have the same aes.private!", NamedTextColor.BLUE),
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
                        adapter
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

                RC.Lang("rustyconnector-wordmark").send(RC.Kernel().version());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(s -> {
            try {
                RustyConnector.unregister();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}