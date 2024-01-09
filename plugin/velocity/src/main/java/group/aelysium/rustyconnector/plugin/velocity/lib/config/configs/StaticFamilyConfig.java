package group.aelysium.rustyconnector.plugin.velocity.lib.config.configs;

import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.toolkit.core.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.core.config.IYAML;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.toolkit.velocity.config.IProxyConfigService;
import group.aelysium.rustyconnector.toolkit.velocity.config.LoadBalancerConfig;
import group.aelysium.rustyconnector.toolkit.velocity.config.WhitelistConfig;
import group.aelysium.rustyconnector.toolkit.velocity.family.UnavailableProtocol;
import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.nio.file.Path;
import java.text.ParseException;
import java.util.Optional;

public class StaticFamilyConfig extends YAML implements group.aelysium.rustyconnector.toolkit.velocity.config.StaticFamilyConfig {
    private String displayName;
    private Family.Reference parent_family = Family.Reference.rootFamily();
    private String firstConnection_loadBalancer = "default";

    private UnavailableProtocol consecutiveConnections_homeServer_ifUnavailable = UnavailableProtocol.ASSIGN_NEW_HOME;
    private LiquidTimestamp consecutiveConnections_homeServer_expiration = null;
    private boolean whitelist_enabled = false;
    private String whitelist_name = "whitelist-template";

    public String displayName() { return displayName; }
    public Family.Reference getParent_family() { return parent_family; }
    public String getFirstConnection_loadBalancer() { return firstConnection_loadBalancer; }

    public boolean isWhitelist_enabled() {
        return whitelist_enabled;
    }

    public String getWhitelist_name() {
        return whitelist_name;
    }

    public UnavailableProtocol getConsecutiveConnections_homeServer_ifUnavailable() {
        return consecutiveConnections_homeServer_ifUnavailable;
    }
    public LiquidTimestamp getConsecutiveConnections_homeServer_expiration() {
        return consecutiveConnections_homeServer_expiration;
    }

    @Override
    public Optional<? extends LoadBalancerConfig> loadBalancer(IProxyConfigService service) {
        return service.loadBalancer(this.firstConnection_loadBalancer);
    }

    @Override
    public Optional<? extends WhitelistConfig> whitelist(IProxyConfigService service) {
        return service.whitelist(this.whitelist_name);
    }

    protected StaticFamilyConfig(Path dataFolder, String target, String name, LangService lang) {
        super(dataFolder, target, name, lang, LangFileMappings.PROXY_STATIC_FAMILY_TEMPLATE);
    }

    @Override
    public IConfigService.ConfigKey key() {
        return new IConfigService.ConfigKey(StaticFamilyConfig.class, name());
    }

    protected void register() throws IllegalStateException {
        try {
            this.displayName = IYAML.getValue(this.data, "display-name", String.class);
        } catch (Exception ignore) {}

        try {
            this.parent_family = new Family.Reference(IYAML.getValue(this.data, "parent-family", String.class));
        } catch (Exception ignore) {}

        try {
            this.firstConnection_loadBalancer = IYAML.getValue(this.data, "first-connection.load-balancer", String.class);
        } catch (Exception ignore) {
            this.firstConnection_loadBalancer = "default";
        }
        this.firstConnection_loadBalancer = this.firstConnection_loadBalancer.replaceFirst("\\.yml$|\\.yaml$","");

        try {
            this.consecutiveConnections_homeServer_ifUnavailable = UnavailableProtocol.valueOf(IYAML.getValue(this.data,"consecutive-connections.home-server.if-unavailable",String.class));
        } catch (IllegalArgumentException ignore) {
            throw new IllegalStateException("You must provide a valid option for [consecutive-connections.home-server.if-unavailable] in your static family configs!");
        }
        try {
            String expiration = IYAML.getValue(this.data, "consecutive-connections.home-server.expiration", String.class);
            if(expiration.equals("NEVER")) this.consecutiveConnections_homeServer_expiration = null;
            else this.consecutiveConnections_homeServer_expiration = LiquidTimestamp.from(expiration);
        } catch (ParseException e) {
            throw new IllegalStateException("You must provide a valid time value for [consecutive-connections.home-server.expiration] in your static family configs!");
        }

        this.whitelist_enabled = IYAML.getValue(this.data,"whitelist.enabled",Boolean.class);
        this.whitelist_name = IYAML.getValue(this.data,"whitelist.name",String.class);
        if(this.whitelist_enabled && this.whitelist_name.equals(""))
            throw new IllegalStateException("whitelist.id cannot be empty in order to use a whitelist in a family!");

        this.whitelist_name = this.whitelist_name.replaceFirst("\\.yml$|\\.yaml$","");
    }

    public static StaticFamilyConfig construct(Path dataFolder, String familyName, LangService lang, ConfigService configService) {
        StaticFamilyConfig config = new StaticFamilyConfig(dataFolder, "families/"+familyName+".static.yml", familyName, lang);
        config.register();
        configService.put(config);
        return config;
    }
}
