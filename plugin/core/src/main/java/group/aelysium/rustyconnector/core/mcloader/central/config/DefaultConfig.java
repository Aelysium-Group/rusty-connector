package group.aelysium.rustyconnector.core.mcloader.central.config;

import group.aelysium.rustyconnector.toolkit.core.logger.PluginLogger;
import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.TinderAdapterForCore;

import java.io.File;

public class DefaultConfig extends YAML {
    private String address;
    private String magicConfig;
    private boolean magicInterfaceResolver;

    public DefaultConfig(File configPointer) {
        super(configPointer);
    }

    public String address() {
        return address;
    }

    public String magicConfig() {
        return magicConfig;
    }
    public boolean magicInterfaceResolver() {
        return magicInterfaceResolver;
    }

    public void register(int configVersion) throws IllegalStateException {
        try {
            this.processVersion(configVersion);
        } catch (Exception | UnsupportedClassVersionError e) {
            throw new IllegalStateException(e.getMessage());
        }

        this.magicConfig = this.getNode(this.data,"magic-config",String.class);
        if(this.magicConfig.equals("")) throw new IllegalStateException("You must provide a magic config name name in order for RustyConnector to work! The config name must correspond to a config on your proxy.");

        this.address = this.getNode(this.data,"address",String.class);

        this.magicInterfaceResolver = this.getNode(this.data,"magic-interface-resolver",Boolean.class);
        if(this.address.equals("")) this.magicInterfaceResolver = true;
    }
}
