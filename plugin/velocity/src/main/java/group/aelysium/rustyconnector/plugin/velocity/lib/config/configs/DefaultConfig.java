package group.aelysium.rustyconnector.plugin.velocity.lib.config.configs;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class DefaultConfig extends YAML {
    private boolean whitelist_enabled = false;
    private String whitelist_name = "whitelist-template";

    private Integer magicLink_serverTimeout = 15;
    private Integer magicLink_serverPingInterval = 10;

    public boolean whitelist_enabled() {
        return this.whitelist_enabled;
    }

    public String whitelist_name() {
        return this.whitelist_name;
    }

    public Integer magicLink_serverTimeout() {
        return magicLink_serverTimeout;
    }

    public Integer magicLink_serverPingInterval() {
        return magicLink_serverPingInterval;
    }

    protected DefaultConfig(Path dataFolder, String target, LangService lang) {
        super(dataFolder, target, lang, LangFileMappings.PROXY_CONFIG_TEMPLATE);
    }


    @SuppressWarnings("unchecked")
    protected void register(int configVersion) throws IllegalStateException, NoOutputException {
        PluginLogger logger = Tinder.get().logger();

        try {
            this.processVersion(configVersion);
        } catch (Exception | UnsupportedClassVersionError e) {
            throw new IllegalStateException(e.getMessage());
        }

        // Whitelist

        this.whitelist_enabled = this.getNode(this.data,"whitelist.enabled",Boolean.class);
        this.whitelist_name = this.getNode(this.data,"whitelist.id",String.class);
        if(this.whitelist_enabled && this.whitelist_name.equals(""))
            throw new IllegalStateException("whitelist.id cannot be empty in order to use a whitelist on the proxy!");

        this.whitelist_name = this.whitelist_name.replaceFirst("\\.yml$|\\.yaml$","");

        // Hearts
        this.magicLink_serverTimeout = this.getNode(this.data,"magic-link.server-timeout",Integer.class);
        if(this.magicLink_serverTimeout < 5) {
            ProxyLang.BOXED_MESSAGE_COLORED.send(logger, "Server timeout is set dangerously fast: " + this.magicLink_serverTimeout + "s. Setting to default of 5s.", NamedTextColor.YELLOW);
            this.magicLink_serverTimeout = 5;
        }
        this.magicLink_serverPingInterval = this.getNode(this.data,"magic-link.server-ping-interval",Integer.class);
        if(this.magicLink_serverPingInterval < 5) {
            ProxyLang.BOXED_MESSAGE_COLORED.send(logger, "Server ping interval is set dangerously fast: " + this.magicLink_serverPingInterval + "s. Setting to default of 5s.", NamedTextColor.YELLOW);
            this.magicLink_serverPingInterval = 5;
        }
        if(this.magicLink_serverTimeout < this.magicLink_serverPingInterval) {
            ProxyLang.BOXED_MESSAGE_COLORED.send(logger, "Server timeout can't be less than server ping interval!", NamedTextColor.YELLOW);
            this.magicLink_serverPingInterval = this.magicLink_serverTimeout - 2;
        }
    }

    public static DefaultConfig construct(Path dataFolder, LangService lang, int pluginConfigVersion) {
        DefaultConfig config = new DefaultConfig(dataFolder, "config.yml", lang);
        config.register(pluginConfigVersion);
        return config;
    }
}
