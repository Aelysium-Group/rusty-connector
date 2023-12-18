package group.aelysium.rustyconnector.plugin.velocity.lib.config.configs;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.toolkit.core.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.core.config.IYAML;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import net.kyori.adventure.text.format.NamedTextColor;

import java.nio.file.Path;

public class FriendsConfig extends YAML implements group.aelysium.rustyconnector.toolkit.velocity.config.FriendsConfig {
    private boolean enabled = false;
    private int maxFriends;
    private boolean sendNotifications;
    private boolean showFamilies;

    private boolean allowMessaging;

    protected FriendsConfig(Path dataFolder, String target, String name, LangService lang) {
        super(dataFolder, target, name, lang, LangFileMappings.PROXY_FRIENDS_TEMPLATE);
    }

    @Override
    public IConfigService.ConfigKey key() {
        return IConfigService.ConfigKey.singleton(FriendsConfig.class);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getMaxFriends() {
        return maxFriends;
    }

    public boolean isSendNotifications() {
        return sendNotifications;
    }

    public boolean isShowFamilies() {
        return showFamilies;
    }

    public boolean isAllowMessaging() {
        return allowMessaging;
    }

    @SuppressWarnings("unchecked")
    protected void register() throws IllegalStateException, NoOutputException {
        PluginLogger logger = Tinder.get().logger();

        this.enabled = IYAML.getValue(this.data, "enabled", Boolean.class);
        if(!this.enabled) return;

        this.maxFriends = IYAML.getValue(this.data, "max-friends", Integer.class);
        if(maxFriends > 100) {
            this.maxFriends = 100;
            logger.send(ProxyLang.BOXED_MESSAGE_COLORED.build("[max-friends] in friends.yml is to high! Setting to 100.", NamedTextColor.YELLOW));
        }

        this.sendNotifications = IYAML.getValue(this.data, "send-notifications", Boolean.class);
        this.showFamilies = IYAML.getValue(this.data, "show-families", Boolean.class);
        this.allowMessaging = IYAML.getValue(this.data, "allow-messaging", Boolean.class);
    }

    public static FriendsConfig construct(Path dataFolder, LangService lang, ConfigService configService) {
        FriendsConfig config = new FriendsConfig(dataFolder, "extras/friends.yml", "friends", lang);
        config.register();
        configService.put(config);
        return config;
    }
}
