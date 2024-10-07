package group.aelysium.rustyconnector.plugin.velocity.lib.config.configs;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.WhitelistPlayerFilter;
import group.aelysium.rustyconnector.toolkit.core.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.core.config.IYAML;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.toolkit.velocity.whitelist.IWhitelistPlayerFilter;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.configurate.serialize.SerializationException;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WhitelistConfig extends YAML implements group.aelysium.rustyconnector.toolkit.velocity.config.WhitelistConfig {
    private boolean use_players = false;
    private final List<IWhitelistPlayerFilter> players = new ArrayList<>();

    private boolean use_permission = false;
    private String message = "You aren't whitelisted on this server!";
    private boolean strict = false;
    private boolean inverted = false;

    public boolean getUse_players() {
        return use_players;
    }

    public List<IWhitelistPlayerFilter> getPlayers() {
        return players;
    }

    public boolean getUse_permission() {
        return use_permission;
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

        this.use_players = IYAML.getValue(this.data,"use-players",Boolean.class);

        try {
            IYAML.get(this.data,"players").childrenList().forEach(e -> {
                try {
                    String username = e.node("username").get(String.class);
                    String uuid = e.node("uuid").get(String.class);
                    String ip = e.node("ip").get(String.class);
                    new WhitelistPlayerFilter(
                            username,
                            uuid == null ? null : UUID.fromString(uuid),
                            ip
                    );
                } catch (SerializationException ex) {
                    throw new RuntimeException(ex);
                }
            });
        } catch (Exception e) {
            throw new IllegalStateException("The node [players] in "+this.name()+" is invalid! Make sure you are using the correct type of data!");
        }

        this.use_permission = IYAML.getValue(this.data,"use-permission",Boolean.class);

        this.message = IYAML.getValue(data,"message",String.class);
        if(this.message.equalsIgnoreCase(""))
            throw new IllegalStateException("Whitelist kick messages cannot be empty!");

        this.strict = IYAML.getValue(data,"strict",Boolean.class);

        this.inverted = IYAML.getValue(data,"inverted",Boolean.class);
    }

    public static WhitelistConfig construct(Path dataFolder, String whitelistName, LangService lang, ConfigService configService) {
        WhitelistConfig config = new WhitelistConfig(dataFolder, "whitelists/"+whitelistName+".yml", whitelistName, lang);
        config.register();
        configService.put(config);
        return config;
    }
}
