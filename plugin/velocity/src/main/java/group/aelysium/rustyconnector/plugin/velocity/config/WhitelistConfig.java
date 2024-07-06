package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.rustyconnector.common.config.Config;
import group.aelysium.rustyconnector.common.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.config.ConfigService;
import group.aelysium.rustyconnector.toolkit.proxy.lang.ProxyLang;
import group.aelysium.rustyconnector.toolkit.common.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.common.lang.IConfig;
import group.aelysium.rustyconnector.toolkit.common.lang.LangFileMappings;
import net.kyori.adventure.text.format.NamedTextColor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WhitelistConfig extends Config implements group.aelysium.rustyconnector.toolkit.proxy.config.WhitelistConfig {
    private boolean use_players = false;
    private List<Object> players = new ArrayList<>();

    private boolean use_permission = false;

    private boolean use_country = false;
    private List<String> countries = new ArrayList<>();
    private String message = "You aren't whitelisted on this server!";
    private boolean strict = false;
    private boolean inverted = false;

    public boolean getUse_players() {
        return use_players;
    }

    public List<Object> getPlayers() {
        return players;
    }

    public boolean getUse_permission() {
        return use_permission;
    }

    public boolean getUse_country() {
        return use_country;
    }

    public List<String> getCountries() {
        return countries;
    }

    public String getMessage() {
        return message;
    }
    public boolean isStrict() {
        return strict;
    }
    public boolean isInverted() {
        return inverted;
    }

    protected WhitelistConfig(Path dataFolder, String target, String name, LangService lang) {
        super(dataFolder, target, name, lang, LangFileMappings.PROXY_WHITELIST_TEMPLATE);
    }

    @Override
    public IConfigService.ConfigKey key() {
        return new IConfigService.ConfigKey(WhitelistConfig.class, name());
    }

    @SuppressWarnings("unchecked")
    protected void register() throws IllegalStateException {
        PluginLogger logger = Tinder.get().logger();

        this.use_players = IConfig.getValue(this.data,"use-players",Boolean.class);
        try {
            this.players = (IConfig.getValue(this.data,"players",List.class));
        } catch (ClassCastException e) {
            throw new IllegalStateException("The node [players] in "+this.name()+" is invalid! Make sure you are using the correct type of data!");
        }

        this.use_permission = IConfig.getValue(this.data,"use-permission",Boolean.class);

        this.use_country = IConfig.getValue(this.data,"use-country",Boolean.class);
        if(this.use_country)
            ProxyLang.BOXED_MESSAGE_COLORED.send(logger, "RustyConnector does not currently support country codes in whitelists. Setting `use-country` to false.", NamedTextColor.YELLOW);
        this.use_country = false;
        this.countries = new ArrayList<>();

        this.message = IConfig.getValue(data,"message",String.class);
        if(this.message.equalsIgnoreCase(""))
            throw new IllegalStateException("Whitelist kick messages cannot be empty!");

        this.strict = IConfig.getValue(data,"strict",Boolean.class);

        this.inverted = IConfig.getValue(data,"inverted",Boolean.class);
    }

    public static WhitelistConfig construct(Path dataFolder, String whitelistName, LangService lang, ConfigService configService) {
        WhitelistConfig config = new WhitelistConfig(dataFolder, "whitelists/"+whitelistName+".yml", whitelistName, lang);
        config.register();
        configService.put(config);
        return config;
    }
}
