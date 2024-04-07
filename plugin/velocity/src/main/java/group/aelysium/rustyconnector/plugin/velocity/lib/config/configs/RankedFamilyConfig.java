package group.aelysium.rustyconnector.plugin.velocity.lib.config.configs;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.toolkit.core.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.core.config.IYAML;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.toolkit.velocity.config.IProxyConfigService;
import group.aelysium.rustyconnector.toolkit.velocity.config.MatchMakerConfig;
import group.aelysium.rustyconnector.toolkit.velocity.config.WhitelistConfig;

import java.nio.file.Path;
import java.util.Optional;

public class RankedFamilyConfig extends YAML implements group.aelysium.rustyconnector.toolkit.velocity.config.RankedFamilyConfig {
    private String displayName;
    private Family.Reference parent_family = Family.Reference.rootFamily();
    private String matchmaker;
    private String gameId;
    private boolean whitelist_enabled;
    private String whitelist_name;

    @Override
    public IConfigService.ConfigKey key() {
        return new IConfigService.ConfigKey(RankedFamilyConfig.class, name());
    }

    @Override
    public Optional<? extends MatchMakerConfig> matchmaker(IProxyConfigService service) {
        return service.matchmaker(this.matchmaker);
    }
    @Override
    public Optional<? extends WhitelistConfig> whitelist(IProxyConfigService service) {
        return service.whitelist(this.whitelist_name);
    }
    public String displayName() { return displayName; }
    public Family.Reference getParent_family() { return parent_family; }

    public String gameId() {
        return gameId;
    }
    public String matchmaker_name() {
        return matchmaker;
    }

    public boolean isWhitelist_enabled() {
        return whitelist_enabled;
    }

    public String getWhitelist_name() {
        return whitelist_name;
    }

    protected RankedFamilyConfig(Path dataFolder, String target, String name, LangService lang) {
        super(dataFolder, target, name, lang, LangFileMappings.PROXY_RANKED_FAMILY_TEMPLATE);
    }

    protected void register(String familyName) throws IllegalStateException {
        try {
            this.displayName = IYAML.getValue(this.data, "display-name", String.class);
        } catch (Exception ignore) {}

        try {
            this.parent_family = new Family.Reference(IYAML.getValue(this.data, "parent-family", String.class));
        } catch (Exception ignore) {}

        this.gameId = IYAML.getValue(this.data,"game-id",String.class);
        if(this.gameId.equalsIgnoreCase("default") || this.gameId.equalsIgnoreCase(""))
            this.gameId = familyName;
        if(this.gameId.length() > 16)
            throw new IllegalStateException("The game-id in your ranked families: "+this.gameId+" cannot be more than 16 characters long!");

        this.matchmaker = IYAML.getValue(this.data,"matchmaker",String.class);

        this.whitelist_enabled = IYAML.getValue(this.data,"whitelist.enabled",Boolean.class);
        this.whitelist_name = IYAML.getValue(this.data,"whitelist.name",String.class);
        if(this.whitelist_enabled && this.whitelist_name.equals(""))
            throw new IllegalStateException("whitelist.id cannot be empty in order to use a whitelist in a family!");

        this.whitelist_name = this.whitelist_name.replaceFirst("\\.yml$|\\.yaml$","");
    }

    public static RankedFamilyConfig construct(Path dataFolder, String familyName, LangService lang, ConfigService configService) {
        RankedFamilyConfig config = new RankedFamilyConfig(dataFolder, "families/"+familyName+".ranked.yml", familyName, lang);
        config.register(familyName);
        configService.put(config);
        return config;
    }
}