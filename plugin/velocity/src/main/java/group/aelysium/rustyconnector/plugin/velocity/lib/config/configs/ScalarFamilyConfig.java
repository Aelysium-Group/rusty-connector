package group.aelysium.rustyconnector.plugin.velocity.lib.config.configs;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.toolkit.core.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.core.config.IYAML;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.toolkit.velocity.config.IProxyConfigService;
import group.aelysium.rustyconnector.toolkit.velocity.config.LoadBalancerConfig;
import group.aelysium.rustyconnector.toolkit.velocity.config.WhitelistConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.nio.file.Path;
import java.util.Optional;

public class ScalarFamilyConfig extends YAML implements group.aelysium.rustyconnector.toolkit.velocity.config.ScalarFamilyConfig {
    private String displayName;
    private Family.Reference parent_family = Family.Reference.rootFamily();
    private String loadBalancer = "default";
    private boolean whitelist_enabled = false;
    private String whitelist_name = "whitelist-template";

    @Override
    public IConfigService.ConfigKey key() {
        return new IConfigService.ConfigKey(ScalarFamilyConfig.class, name());
    }

    @Override
    public Optional<? extends LoadBalancerConfig> loadBalancer(IProxyConfigService service) {
        return service.loadBalancer(this.loadBalancer);
    }

    @Override
    public Optional<? extends WhitelistConfig> whitelist(IProxyConfigService service) {
        return service.whitelist(this.whitelist_name);
    }

    public String displayName() { return displayName; }
    public Family.Reference getParent_family() { return parent_family; }
    public String loadBalancer_name() { return loadBalancer; }

    public boolean isWhitelist_enabled() {
        return whitelist_enabled;
    }

    public String getWhitelist_name() {
        return whitelist_name;
    }

    protected ScalarFamilyConfig(Path dataFolder, String target, String name, LangService lang) {
        super(dataFolder, target, name, lang, LangFileMappings.PROXY_SCALAR_FAMILY_TEMPLATE);
    }

    protected void register() throws IllegalStateException {
        try {
            this.displayName = IYAML.getValue(this.data, "display-name", String.class);
        } catch (Exception ignore) {}

        try {
            this.parent_family = new Family.Reference(IYAML.getValue(this.data, "parent-family", String.class));
        } catch (Exception ignore) {}

        try {
            this.loadBalancer = IYAML.getValue(this.data, "load-balancer", String.class);
        } catch (Exception ignore) {
            this.loadBalancer = "default";
        }
        this.loadBalancer = this.loadBalancer.replaceFirst("\\.yml$|\\.yaml$","");

        this.whitelist_enabled = IYAML.getValue(this.data,"whitelist.enabled",Boolean.class);
        this.whitelist_name = IYAML.getValue(this.data,"whitelist.name",String.class);
        if(this.whitelist_enabled && this.whitelist_name.equals(""))
            throw new IllegalStateException("whitelist.id cannot be empty in order to use a whitelist in a family!");

        this.whitelist_name = this.whitelist_name.replaceFirst("\\.yml$|\\.yaml$","");
    }

    public static ScalarFamilyConfig construct(Path dataFolder, String familyName, LangService lang, ConfigService configService) {
        ScalarFamilyConfig config = new ScalarFamilyConfig(dataFolder, "families/"+familyName+".scalar.yml", familyName, lang);
        config.register();
        configService.put(config);
        return config;
    }
}