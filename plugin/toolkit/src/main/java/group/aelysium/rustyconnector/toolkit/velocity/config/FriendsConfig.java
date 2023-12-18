package group.aelysium.rustyconnector.toolkit.velocity.config;

import group.aelysium.rustyconnector.toolkit.core.config.IYAML;

public interface FriendsConfig {
    boolean isEnabled();
    int getMaxFriends();
    boolean isSendNotifications();
    boolean isShowFamilies();
    boolean isAllowMessaging();
}
