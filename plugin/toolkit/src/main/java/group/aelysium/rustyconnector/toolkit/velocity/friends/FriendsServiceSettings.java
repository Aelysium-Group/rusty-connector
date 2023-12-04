package group.aelysium.rustyconnector.toolkit.velocity.friends;

import group.aelysium.rustyconnector.toolkit.velocity.storage.IMySQLStorageService;

public record FriendsServiceSettings(
        IMySQLStorageService storage,
        int maxFriends,
        boolean sendNotifications,
        boolean showFamilies,
        boolean allowMessaging
) {}