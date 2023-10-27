package group.aelysium.rustyconnector.plugin.velocity.central.config;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.lang.Lang;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;

public class DefaultConfig extends YAML {
    private boolean debug = false;

    private boolean whitelist_enabled = false;
    private String whitelist_name = "whitelist-template";

    private Integer services_serverLifecycle_serverTimeout = 15;
    private Integer services_serverLifecycle_serverPingInterval = 10;
    private Boolean services_loadBalancing_enabled = true;
    private Integer services_loadBalancing_interval = 20;

    public DefaultConfig(File configPointer) {
        super(configPointer);
    }

    public boolean whitelist_enabled() {
        return this.whitelist_enabled;
    }

    public String whitelist_name() {
        return this.whitelist_name;
    }

    public Integer services_serverLifecycle_serverTimeout() {
        return services_serverLifecycle_serverTimeout;
    }

    public Integer services_serverLifecycle_serverPingInterval() {
        return services_serverLifecycle_serverPingInterval;
    }

    public Integer services_loadBalancing_interval() {
        return services_loadBalancing_interval;
    }

    public Boolean services_loadBalancing_enabled() {
        return services_loadBalancing_enabled;
    }

    @SuppressWarnings("unchecked")
    public void register(int configVersion) throws IllegalStateException, NoOutputException {
        PluginLogger logger = Tinder.get().logger();

        try {
            this.processVersion(configVersion);
        } catch (Exception | UnsupportedClassVersionError e) {
            throw new IllegalStateException(e.getMessage());
        }

        try {
            this.debug = this.getNode(this.data,"debug",Boolean.class);
        } catch (Exception e) {
            this.debug = false;
        }

        // Whitelist

        this.whitelist_enabled = this.getNode(this.data,"whitelist.enabled",Boolean.class);
        this.whitelist_name = this.getNode(this.data,"whitelist.name",String.class);
        if(this.whitelist_enabled && this.whitelist_name.equals(""))
            throw new IllegalStateException("whitelist.name cannot be empty in order to use a whitelist on the proxy!");

        this.whitelist_name = this.whitelist_name.replaceFirst("\\.yml$|\\.yaml$","");

        // Hearts
        this.services_serverLifecycle_serverTimeout = this.getNode(this.data,"services.server-lifecycle.server-timeout",Integer.class);
        if(this.services_serverLifecycle_serverTimeout < 5) {
            VelocityLang.BOXED_MESSAGE_COLORED.send(logger, "Server timeout is set dangerously fast: " + this.services_serverLifecycle_serverTimeout + "s. Setting to default of 5s.", NamedTextColor.YELLOW);
            this.services_serverLifecycle_serverTimeout= 5;
        }
        this.services_serverLifecycle_serverPingInterval = this.getNode(this.data,"services.server-lifecycle.server-ping-interval",Integer.class);
        if(this.services_serverLifecycle_serverPingInterval < 5) {
            VelocityLang.BOXED_MESSAGE_COLORED.send(logger, "Server ping interval is set dangerously fast: " + this.services_serverLifecycle_serverPingInterval + "s. Setting to default of 5s.", NamedTextColor.YELLOW);
            this.services_serverLifecycle_serverPingInterval = 5;
        }
        if(this.services_serverLifecycle_serverTimeout < this.services_serverLifecycle_serverPingInterval) {
            VelocityLang.BOXED_MESSAGE_COLORED.send(logger, "Server timeout can't be less that server ping interval!", NamedTextColor.YELLOW);
            this.services_serverLifecycle_serverPingInterval = this.services_serverLifecycle_serverTimeout - 2;
        }


        this.services_loadBalancing_enabled = this.getNode(this.data,"services.load-balancing.enabled",Boolean.class);
        this.services_loadBalancing_interval = this.getNode(this.data,"services.load-balancing.interval",Integer.class);
        if(this.services_loadBalancing_interval < 7) {
            VelocityLang.BOXED_MESSAGE_COLORED.send(logger, "Server sorting interval is set dangerously fast: " + this.services_loadBalancing_interval + "ms. Setting to default of 20ms.", NamedTextColor.YELLOW);
            this.services_loadBalancing_interval = 20;
        }
    }
}
