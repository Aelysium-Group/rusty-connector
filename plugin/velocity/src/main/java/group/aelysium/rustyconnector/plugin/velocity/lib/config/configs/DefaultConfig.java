package group.aelysium.rustyconnector.plugin.velocity.lib.config.configs;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.toolkit.core.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.core.config.IYAML;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import net.kyori.adventure.text.format.NamedTextColor;

import java.nio.file.Path;

public class DefaultConfig extends YAML implements group.aelysium.rustyconnector.toolkit.velocity.config.DefaultConfig {
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

    protected DefaultConfig(Path dataFolder, String target, String name, LangService lang) {
        super(dataFolder, target, name, lang, LangFileMappings.PROXY_CONFIG_TEMPLATE);
    }

    @Override
    public IConfigService.ConfigKey key() {
        return IConfigService.ConfigKey.singleton(DefaultConfig.class);
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

        this.whitelist_enabled = IYAML.getValue(this.data,"whitelist.enabled",Boolean.class);
        this.whitelist_name = IYAML.getValue(this.data,"whitelist.name",String.class);
        if(this.whitelist_enabled && this.whitelist_name.equals(""))
            throw new IllegalStateException("whitelist.id cannot be empty in order to use a whitelist on the proxy!");

        this.whitelist_name = this.whitelist_name.replaceFirst("\\.yml$|\\.yaml$","");

        // Hearts
        this.magicLink_serverTimeout = IYAML.getValue(this.data,"magic-link.server-timeout",Integer.class);
        if(this.magicLink_serverTimeout < 5) {
            ProxyLang.BOXED_MESSAGE_COLORED.send(logger, "Server timeout is set dangerously fast: " + this.magicLink_serverTimeout + "s. Setting to default of 5s.", NamedTextColor.YELLOW);
            this.magicLink_serverTimeout = 5;
        }
        this.magicLink_serverPingInterval = IYAML.getValue(this.data,"magic-link.server-ping-interval",Integer.class);
        if(this.magicLink_serverPingInterval < 5) {
            ProxyLang.BOXED_MESSAGE_COLORED.send(logger, "Server ping interval is set dangerously fast: " + this.magicLink_serverPingInterval + "s. Setting to minimum of 5s.", NamedTextColor.YELLOW);
            this.magicLink_serverPingInterval = 5;
        }
        if(this.magicLink_serverTimeout <= this.magicLink_serverPingInterval) {
            ProxyLang.BOXED_MESSAGE_COLORED.send(logger, "Server timeout must be more than server ping interval!", NamedTextColor.YELLOW);
            this.magicLink_serverPingInterval = this.magicLink_serverTimeout - 2;
        }
    }

    public static DefaultConfig construct(Path dataFolder, LangService lang, int pluginConfigVersion, ConfigService configService) {
        DefaultConfig config = new DefaultConfig(dataFolder, "config.yml", "config", lang);
        config.register(pluginConfigVersion);
        configService.put(config);
        return config;
    }
}
