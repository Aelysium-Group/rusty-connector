package group.aelysium.rustyconnector.toolkit.velocity.friends;

import group.aelysium.rustyconnector.toolkit.velocity.storage.IStorageService;

public record FriendsServiceSettings(
        IStorageService storage,
        int maxFriends,
        boolean sendNotifications,
        boolean showFamilies,
        boolean allowMessaging
) {}