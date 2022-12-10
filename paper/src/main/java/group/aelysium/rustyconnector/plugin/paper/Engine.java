package group.aelysium.rustyconnector.plugin.paper;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.plugin.paper.commands.CommandRusty;
import group.aelysium.rustyconnector.plugin.paper.lib.PaperServer;
import group.aelysium.rustyconnector.plugin.paper.lib.config.DefaultConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.util.function.Function;

public class Engine {
    public static boolean start() {
        PaperRustyConnector plugin = PaperRustyConnector.getInstance();

        if(!initConfigs(plugin)) return false;
        if(!initCommands(plugin)) return false;
        if(!initEvents(plugin)) return false;

        return true;
    }
    public static void stop() {
        PaperRustyConnector plugin = PaperRustyConnector.getInstance();

        plugin.getVirtualServer().unregisterFromProxy();

        DefaultConfig.empty();

        plugin.getVirtualServer().killRedis();

        plugin.getCommandManager().deleteRootCommand("rc");
    }

    private static boolean initConfigs(PaperRustyConnector plugin) {
        try {
            DefaultConfig defaultConfig = DefaultConfig.newConfig(new File(plugin.getDataFolder(), "config.yml"), "paper_config_template.yml");
            if(!defaultConfig.generate()) {
                throw new IllegalStateException("Unable to load or create config.yml!");
            }
            defaultConfig.register();

            plugin.setServer(PaperServer.init(defaultConfig));

            Lang.WORDMARK_RUSTY_CONNECTOR.send(plugin.logger());

            if(defaultConfig.isRegisterOnBoot()) {
                Lang.BOXED_MESSAGE.send(plugin.logger(), Component.text("Sent a registration request over the data-channel...", NamedTextColor.GREEN));
                plugin.getVirtualServer().registerToProxy();
            }

            DefaultConfig.empty();

            return true;
        } catch (Exception e) {
            plugin.logger().log(e.getMessage());
            return false;
        }
    }
    private static boolean initCommands(PaperRustyConnector plugin) {
        try {
            plugin.setCommandManager(new PaperCommandManager<>(
                    plugin,
                    CommandExecutionCoordinator.simpleCoordinator(),
                    Function.identity(),
                    Function.identity()
            ));

            CommandRusty.create(plugin.getCommandManager());

            return true;
        } catch (Exception e) {
            plugin.logger().log(e.getMessage());
            return false;
        }
    }

    private static boolean initEvents(PaperRustyConnector plugin) {
        return true;
    }
}
