package group.aelysium.rustyconnector.api.velocity.friends;

import group.aelysium.rustyconnector.api.velocity.storage.IMySQLStorageService;

public record FriendsServiceSettings(
        IMySQLStorageService storage,
        int maxFriends,
        boolean sendNotifications,
        boolean showFamilies,
        boolean allowMessaging
) {}