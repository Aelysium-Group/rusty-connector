package group.aelysium.rustyconnector.mcloader.central.config;

import group.aelysium.rustyconnector.common.lang.LangService;
import group.aelysium.rustyconnector.toolkit.common.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.common.lang.IConfig;
import group.aelysium.rustyconnector.toolkit.common.lang.LangFileMappings;
import group.aelysium.rustyconnector.common.config.Config;

import java.nio.file.Path;

public class DefaultConfig extends Config {
    private String address;
    private String magicConfig;
    private String displayName = null;

    public String address() {
        return address;
    }
    public String magicConfig() {
        return magicConfig;
    }
    public String displayName() {
        return this.displayName;
    }

    protected DefaultConfig(Path dataFolder, String target, String name, LangService lang) {
        super(dataFolder, target, name, lang, LangFileMappings.MCLOADER_CONFIG_TEMPLATE);
    }

    protected void register(int configVersion) throws IllegalStateException {
        try {
            this.processVersion(configVersion);
        } catch (Exception | UnsupportedClassVersionError e) {
            throw new IllegalStateException(e.getMessage());
        }

        this.magicConfig = IConfig.getValue(this.data,"magic-config",String.class);
        if(this.magicConfig.equals("")) throw new IllegalStateException("You must provide a magic config name name in order for RustyConnector to work! The config name must correspond to a config on your proxy.");

        this.address = IConfig.getValue(this.data,"address",String.class);

        try {
            this.displayName = IConfig.getValue(this.data,"display-name",String.class);
            if(this.displayName.isEmpty()) throw new Exception();
            if(this.displayName.length() > 16) this.displayName = this.displayName.substring(0, 16);
        } catch (Exception e) {
            this.displayName = "";
        }
    }

    public static DefaultConfig construct(Path dataFolder, LangService lang, int pluginConfigVersion) {
        DefaultConfig config = new DefaultConfig(dataFolder, "config.yml", "config", lang);
        config.register(pluginConfigVersion);
        return config;
    }

    @Override
    public IConfigService.ConfigKey key() {
        return IConfigService.ConfigKey.singleton(DefaultConfig.class);
    }
}
