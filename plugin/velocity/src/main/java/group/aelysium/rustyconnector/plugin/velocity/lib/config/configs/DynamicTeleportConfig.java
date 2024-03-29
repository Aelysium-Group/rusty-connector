package group.aelysium.rustyconnector.plugin.velocity.lib.config.configs;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigService;
import group.aelysium.rustyconnector.toolkit.core.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.core.config.IYAML;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.configurate.ConfigurationNode;

import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DynamicTeleportConfig extends YAML implements group.aelysium.rustyconnector.toolkit.velocity.config.DynamicTeleportConfig {
    private boolean enabled = false;
    private boolean tpa_enabled = false;
    private boolean tpa_friendsOnly = false;
    private List<String> tpa_enabledFamilies = new ArrayList<>();
    private boolean tpa_ignorePlayerCap = false;
    private LiquidTimestamp tpa_expiration;

    private boolean familyAnchor_enabled = false;
    private List<Map.Entry<String, String>> familyAnchor_anchors;

    private boolean familyInjector_enabled = false;
    private List<Map.Entry<String, String>> familyInjector_injectors;

    private boolean hub_enabled = false;
    private List<String> hub_enabledFamilies = new ArrayList<>();

    protected DynamicTeleportConfig(Path dataFolder, String target, String name, LangService lang) {
        super(dataFolder, target, name, lang, LangFileMappings.PROXY_DYNAMIC_TELEPORT_TEMPLATE);
    }

    @Override
    public IConfigService.ConfigKey key() {
        return IConfigService.ConfigKey.singleton(DynamicTeleportConfig.class);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isTpa_enabled() {
        return tpa_enabled;
    }

    public boolean isTpa_friendsOnly() {
        return tpa_friendsOnly;
    }

    public List<String> getTpa_enabledFamilies() {
        return tpa_enabledFamilies;
    }

    public boolean isTpa_ignorePlayerCap() {
        return tpa_ignorePlayerCap;
    }

    public LiquidTimestamp getTpa_expiration() {
        return tpa_expiration;
    }

    public boolean isFamilyAnchor_enabled() {
        return familyAnchor_enabled;
    }

    public List<Map.Entry<String, String>> getFamilyAnchor_anchors() {
        return familyAnchor_anchors;
    }

    public boolean isFamilyInjector_enabled() {
        return familyInjector_enabled;
    }

    public List<Map.Entry<String, String>> getFamilyInjector_injectors() {
        return familyInjector_injectors;
    }

    public boolean isHub_enabled() {
        return hub_enabled;
    }

    public List<String> getHub_enabledFamilies() {
        return hub_enabledFamilies;
    }

    @SuppressWarnings("unchecked")
    protected void register() throws IllegalStateException, NoOutputException {
        this.enabled = IYAML.getValue(this.data, "enabled", Boolean.class);
        if(!this.enabled) return;

        this.tpa_enabled = IYAML.getValue(this.data, "tpa.enabled", Boolean.class);
        if(this.tpa_enabled) {
            this.tpa_friendsOnly = IYAML.getValue(this.data, "tpa.friends-only", Boolean.class);

            try {
                this.tpa_enabledFamilies = (List<String>) (IYAML.getValue(this.data, "tpa.enabled-families", List.class));
            } catch (Exception e) {
                throw new IllegalStateException("The node [tpa.enabled-families] in " + this.name() + " is invalid! Make sure you are using the correct type of data!");
            }

            this.tpa_ignorePlayerCap = IYAML.getValue(this.data, "tpa.ignore-player-cap", Boolean.class);


            try {
                String expiration = IYAML.getValue(this.data, "tpa.expiration", String.class);
                if (expiration.equals("NEVER")) {
                    this.tpa_expiration = LiquidTimestamp.from(5, TimeUnit.MINUTES);
                    Tinder.get().logger().send(ProxyLang.BOXED_MESSAGE_COLORED.build("\"NEVER\" as a Liquid Timestamp for [tpa.expiration] is not allowed! Set to default of 5 Minutes.", NamedTextColor.YELLOW));
                } else this.tpa_expiration = LiquidTimestamp.from(expiration);
            } catch (ParseException e) {
                throw new IllegalStateException("You must provide a valid time value for [tpa.expiration] in dynamic_teleport.yml!");
            }
        }

        this.familyAnchor_enabled = IYAML.getValue(this.data, "family-anchor.enabled", Boolean.class);
        if(this.familyAnchor_enabled) {
            List<? extends ConfigurationNode> anchors = IYAML.get(this.data, "family-anchor.anchors").childrenList();

            this.familyAnchor_anchors = new ArrayList<>();
            if(anchors.size() != 0)
                for (ConfigurationNode entry: anchors)
                    this.familyAnchor_anchors.add(Map.entry(
                            IYAML.getValue(entry, "name", String.class),
                            IYAML.getValue(entry, "family", String.class)
                    ));
        }

        this.familyInjector_enabled = IYAML.getValue(this.data, "family-injectors.enabled", Boolean.class);
        if(this.familyInjector_enabled) {
            List<? extends ConfigurationNode> injectors = IYAML.get(this.data, "family-injectors.injectors").childrenList();

            this.familyInjector_injectors = new ArrayList<>();
            if(injectors.size() != 0)
                for (ConfigurationNode entry: injectors)
                    this.familyInjector_injectors.add(Map.entry(
                            IYAML.getValue(entry, "host", String.class),
                            IYAML.getValue(entry, "family", String.class)
                    ));
        }

        this.hub_enabled = IYAML.getValue(this.data, "hub.enabled", Boolean.class);
        if(this.hub_enabled) {
            try {
                this.hub_enabledFamilies = (List<String>) (IYAML.getValue(this.data, "hub.enabled-families", List.class));
            } catch (Exception e) {
                throw new IllegalStateException("The node [hub.enabled-families] in " + this.name() + " is invalid! Make sure you are using the correct type of data!");
            }
        }
    }

    public static DynamicTeleportConfig construct(Path dataFolder, LangService lang, ConfigService configService) {
        DynamicTeleportConfig config = new DynamicTeleportConfig(dataFolder, "extras/dynamic_teleport.yml", "dynamic_teleport", lang);
        config.register();
        configService.put(config);
        return config;
    }
}
