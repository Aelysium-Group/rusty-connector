package group.aelysium.rustyconnector.plugin.velocity.lib.friends.config;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.config.PartyConfig;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.nio.file.Path;

public class FriendsConfig extends YAML {
    private boolean enabled = false;
    private int maxFriends;
    private boolean sendNotifications;
    private boolean showFamilies;

    private boolean allowMessaging;

    protected FriendsConfig(Path dataFolder, String target, LangService lang) {
        super(dataFolder, target, lang, LangFileMappings.PROXY_FRIENDS_TEMPLATE);
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

        this.enabled = this.getNode(this.data, "enabled", Boolean.class);
        if(!this.enabled) return;

        this.maxFriends = this.getNode(this.data, "max-friends", Integer.class);
        if(maxFriends > 100) {
            this.maxFriends = 100;
            logger.send(ProxyLang.BOXED_MESSAGE_COLORED.build("[max-friends] in friends.yml is to high! Setting to 100.", NamedTextColor.YELLOW));
        }

        this.sendNotifications = this.getNode(this.data, "send-notifications", Boolean.class);
        this.showFamilies = this.getNode(this.data, "show-families", Boolean.class);
        this.allowMessaging = this.getNode(this.data, "allow-messaging", Boolean.class);
    }

    public static FriendsConfig construct(Path dataFolder, LangService lang) {
        FriendsConfig config = new FriendsConfig(dataFolder, "extras/friends.yml", lang);
        config.register();
        return config;
    }
}
