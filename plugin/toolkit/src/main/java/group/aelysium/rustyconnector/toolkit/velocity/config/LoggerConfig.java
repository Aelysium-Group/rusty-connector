package group.aelysium.rustyconnector.toolkit.velocity.config;

public interface LoggerConfig {
    boolean shouldSaveTrashedMessages();
    boolean isMessaging_registration();
    boolean isMessaging_unregistration();
    boolean isMessaging_ping();
    boolean isMessaging_messageParserTrash();
    boolean isSecurity_messageTunnelFailedMessage();
    boolean isLog_playerJoin();
    boolean isLog_playerLeave();
    boolean isLog_playerMove();
    boolean isLog_familyBalancing();
}
