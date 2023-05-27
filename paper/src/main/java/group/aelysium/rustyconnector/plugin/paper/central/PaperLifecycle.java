package group.aelysium.rustyconnector.plugin.paper.central;

import group.aelysium.rustyconnector.core.central.PluginLifecycle;
import group.aelysium.rustyconnector.core.lib.config.MigrationDirections;
import group.aelysium.rustyconnector.core.lib.exception.DuplicateLifecycleException;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.commands.CommandRusty;
import group.aelysium.rustyconnector.plugin.paper.config.DefaultConfig;
import group.aelysium.rustyconnector.plugin.paper.lib.events.OnPlayerJoin;
import group.aelysium.rustyconnector.plugin.paper.lib.events.OnPlayerLeave;
import group.aelysium.rustyconnector.plugin.paper.lib.events.OnPlayerPreLogin;
import group.aelysium.rustyconnector.plugin.paper.lib.lang_messaging.PaperLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;

public class PaperLifecycle extends PluginLifecycle {
    public boolean start() throws DuplicateLifecycleException {
        PaperAPI api = PaperRustyConnector.getAPI();
        if(this.isRunning()) throw new DuplicateLifecycleException(
                PaperLang.RCNAME_PAPER_FOLIA.build(api.isFolia()).toString() +
                " is already running! You can't start it a second time!");

        MigrationDirections.init();

        if(!loadConfigs()) return false;
        if(!loadCommands()) return false;
        if(!loadEvents()) return false;

        return true;
    }
    public void stop() {
        PaperAPI api = PaperRustyConnector.getAPI();

        DefaultConfig.empty();

        if(api.getVirtualProcessor() != null) {
            api.getVirtualProcessor().unregisterFromProxy();

           api.getVirtualProcessor().closeRedis();
        }

        api.getCommandManager().deleteRootCommand("rc");
    }

    protected boolean loadConfigs() {
        PaperAPI api = PaperRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();
        try {
            DefaultConfig defaultConfig = DefaultConfig.newConfig(new File(api.getDataFolder(), "config.yml"), "paper_config_template.yml");
            if(!defaultConfig.generate()) {
                throw new IllegalStateException("Unable to load or create config.yml!");
            }
            defaultConfig.register();

            api.configureProcessor(defaultConfig);

            Lang.WORDMARK_RUSTY_CONNECTOR.send(logger);

            if(defaultConfig.isRegisterOnBoot()) {
                Lang.BOXED_MESSAGE.send(logger, Component.text("Sent a registration request over the data-channel...", NamedTextColor.GREEN));
                api.getVirtualProcessor().registerToProxy();
            }

            DefaultConfig.empty();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Lang.BOXED_MESSAGE_COLORED.send(logger, Component.text(e.getMessage()), NamedTextColor.RED);
            return false;
        }
    }
    protected boolean loadCommands() {
        PaperAPI api = PaperRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();
        try {

            CommandRusty.create(api.getCommandManager());

            return true;
        } catch (Exception e) {
            logger.log(e.getMessage());
            return false;
        }
    }

    protected boolean loadEvents() {
        PaperAPI api = PaperRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();

        try {
            api.getServer().getPluginManager().registerEvents(new OnPlayerJoin(), api.accessPlugin());
            api.getServer().getPluginManager().registerEvents(new OnPlayerLeave(), api.accessPlugin());
            api.getServer().getPluginManager().registerEvents(new OnPlayerPreLogin(), api.accessPlugin());

            return true;
        } catch (Exception e) {
            Lang.BOXED_MESSAGE_COLORED.send(logger, Component.text(e.getMessage()), NamedTextColor.RED);
            return false;
        }
    }
}
