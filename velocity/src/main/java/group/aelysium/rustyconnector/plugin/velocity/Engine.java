package group.aelysium.rustyconnector.plugin.velocity;

import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.EventManager;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.plugin.velocity.commands.CommandRusty;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.DefaultConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.FamilyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.LoggerConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.WhitelistConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.events.OnPlayerJoin;
import group.aelysium.rustyconnector.plugin.velocity.lib.events.OnPlayerKick;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.Proxy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;

public class Engine {
    public static boolean start() {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        if(!initConfigs(plugin)) return false;
        if(!initCommands(plugin)) return false;
        if(!initEvents(plugin)) return false;

        VelocityLang.WORDMARK_RUSTY_CONNECTOR.send(plugin.logger());

        DefaultConfig defaultConfig = DefaultConfig.getConfig();
        if(defaultConfig.isBootCommands_enabled()) {
            plugin.logger().log("Issuing boot commands...");
            defaultConfig.getBootCommands_commands().forEach(command -> {
                plugin.logger().log(">>> "+command);
                plugin.getProxy().dispatchCommand(command);
            });
        }

        WhitelistConfig.empty();
        DefaultConfig.empty();
        FamilyConfig.empty();

        return true;
    }
    public static void stop() {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        WhitelistConfig.empty();
        DefaultConfig.empty();
        FamilyConfig.empty();
        LoggerConfig.empty();

        plugin.getProxy().killHeartbeat();
        plugin.getProxy().killRedis();
        plugin.unsetProxy();
        plugin.unsetMessageTunnel();

        plugin.getVelocityServer().getCommandManager().unregister("rc");

        plugin.getVelocityServer().getEventManager().unregisterListener(plugin, new OnPlayerJoin());
        plugin.getVelocityServer().getEventManager().unregisterListener(plugin, new OnPlayerJoin());
    }

    private static boolean initConfigs(VelocityRustyConnector plugin) {
        try {
            DefaultConfig defaultConfig = DefaultConfig.newConfig(new File(plugin.getDataFolder(), "config.yml"), "velocity_config_template.yml");
            if(!defaultConfig.generate()) {
                throw new IllegalStateException("Unable to load or create config.yml!");
            }
            defaultConfig.register();

            LoggerConfig loggerConfig = LoggerConfig.newConfig(new File(plugin.getDataFolder(), "logger.yml"), "velocity_logger_template.yml");
            if(!loggerConfig.generate()) {
                throw new IllegalStateException("Unable to load or create logger.yml!");
            }
            loggerConfig.register();
            PluginLogger.init(loggerConfig);

            plugin.setProxy(Proxy.init(defaultConfig));

            return true;
        } catch (Exception e) {
            VelocityLang.BOXED_MESSAGE_COLORED.send(plugin.logger(), Component.text(e.getMessage()), NamedTextColor.RED);
            return false;
        }
    }
    private static boolean initCommands(VelocityRustyConnector plugin) {
        CommandManager commandManager = plugin.getVelocityServer().getCommandManager();
        try {
            CommandMeta meta = commandManager.metaBuilder("rustyconnector")
                    .aliases("rusty", "rc")
                    .aliases("/rustyconnector","/rusty","/rc") // Add slash varients so that they can be used in console as well
                    .build();
            BrigadierCommand command = CommandRusty.create();

            commandManager.register(meta, command);

            return true;
        } catch (Exception e) {
            VelocityLang.BOXED_MESSAGE_COLORED.send(plugin.logger(), Component.text(e.getMessage()), NamedTextColor.RED);
            return false;
        }
    }

    private static boolean initEvents(VelocityRustyConnector plugin) {
        EventManager manager = plugin.getVelocityServer().getEventManager();
        try {
            manager.register(plugin, new OnPlayerJoin());
            manager.register(plugin, new OnPlayerKick());

            return true;
        } catch (Exception e) {
            VelocityLang.BOXED_MESSAGE_COLORED.send(plugin.logger(), Component.text(e.getMessage()), NamedTextColor.RED);
            return false;
        }
    }
}
