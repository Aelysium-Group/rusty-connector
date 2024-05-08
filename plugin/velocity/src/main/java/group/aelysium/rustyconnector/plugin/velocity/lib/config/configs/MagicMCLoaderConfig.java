package group.aelysium.rustyconnector.plugin.velocity.lib.config.configs;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigService;
import group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.MagicLinkService;
import group.aelysium.rustyconnector.toolkit.core.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.core.config.IYAML;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;

import java.nio.file.Path;

public class MagicMCLoaderConfig extends YAML implements group.aelysium.rustyconnector.toolkit.velocity.config.MagicMCLoaderConfig {
    private String server_family;
    private int server_weight;
    private int server_playerCap_soft;
    private int server_playerCap_hard;

    protected MagicMCLoaderConfig(Path dataFolder, String target, String name, LangService lang) {
        super(dataFolder, target, name, lang, LangFileMappings.PROXY_MAGIC_CONFIG_TEMPLATE);
    }

    @Override
    public IConfigService.ConfigKey key() {
        return new IConfigService.ConfigKey(MagicMCLoaderConfig.class, name());
    }

    public String family() {
        return server_family;
    }

    public int weight() {
        return server_weight;
    }

    public int playerCap_soft() {
        return server_playerCap_soft;
    }

    public int playerCap_hard() {
        return server_playerCap_hard;
    }

    protected void register() throws IllegalStateException {
        this.server_family = IYAML.getValue(this.data,"family",String.class);
        if(this.server_family.equals("")) throw new IllegalStateException("You must provide a family id in order for RustyConnector to work! The family id must also exist on your families.yml configuration.");

        this.server_weight = IYAML.getValue(this.data,"weight",Integer.class);
        if(this.server_weight < 0) throw new IllegalStateException("Server weight cannot be a negative number.");

        this.server_playerCap_soft = IYAML.getValue(this.data,"player-cap.soft",Integer.class);
        this.server_playerCap_hard = IYAML.getValue(this.data,"player-cap.hard",Integer.class);
        if(this.server_playerCap_soft >= this.server_playerCap_hard) this.server_playerCap_soft = this.server_playerCap_hard;
    }

    public static MagicLinkService.MagicLinkMCLoaderSettings construct(Path dataFolder, String magicConfigName, LangService lang, ConfigService configService) {
        MagicMCLoaderConfig config = new MagicMCLoaderConfig(dataFolder, "magic_configs/"+magicConfigName+".yml", magicConfigName, lang);
        config.register();
        configService.put(config);
        return new MagicLinkService.MagicLinkMCLoaderSettings(
                config.server_family,
                config.server_weight,
                config.server_playerCap_soft,
                config.server_playerCap_hard
        );
    }
}
