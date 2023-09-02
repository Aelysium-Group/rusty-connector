package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;

public class FriendsConfig extends YAML {
    private static FriendsConfig config;

    private boolean enabled = false;
    private int maxFriends;
    private boolean sendNotifications;
    private boolean showFamilies;

    private boolean allowMessaging;

    private String storage = "";

    private FriendsConfig(File configPointer, String template) {
        super(configPointer, template);
    }

    /**
     * Get the current config.
     * @return The config.
     */
    public static FriendsConfig getConfig() {
        return config;
    }

    /**
     * Create a new config for the proxy, this will delete the old config.
     * @return The newly created config.
     */
    public static FriendsConfig newConfig(File configPointer, String template) {
        config = new FriendsConfig(configPointer, template);
        return FriendsConfig.getConfig();
    }

    /**
     * Delete all configs associated with this class.
     */
    public static void empty() {
        config = null;
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

    public String storage() {
        return this.storage;
    }

    @SuppressWarnings("unchecked")
    public void register() throws IllegalStateException, NoOutputException {
        PluginLogger logger = VelocityAPI.get().logger();

        this.enabled = this.getNode(this.data, "enabled", Boolean.class);
        if(!this.enabled) return;

        this.maxFriends = this.getNode(this.data, "max-friends", Integer.class);
        if(maxFriends > 100) {
            this.maxFriends = 100;
            logger.send(VelocityLang.BOXED_MESSAGE_COLORED.build(Component.text("[max-friends] in friends.yml is to high! Setting to 100."), NamedTextColor.YELLOW));
        }

        this.sendNotifications = this.getNode(this.data, "send-notifications", Boolean.class);
        this.showFamilies = this.getNode(this.data, "show-families", Boolean.class);
        this.allowMessaging = this.getNode(this.data, "allow-messaging", Boolean.class);

        this.storage = this.getNode(this.data, "storage", String.class);
        if (this.storage.equals("")) throw new IllegalStateException("Please assign a storage method.");
    }
}
