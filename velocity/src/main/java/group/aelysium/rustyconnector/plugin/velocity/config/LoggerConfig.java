package group.aelysium.rustyconnector.plugin.velocity.config;

import java.io.File;

public class LoggerConfig extends YAML {
    private static LoggerConfig config;

    private boolean saveTrashedMessages = true;
    private boolean messaging_registration = false;
    private boolean messaging_unregistration = false;
    private boolean messaging_ping = false;
    private boolean messaging_messageParserTrash = false;

    private boolean security_messageTunnelFailedMessage = true;

    private boolean log_playerJoin = false;
    private boolean log_playerLeave = false;
    private boolean log_playerMove = false;
    private boolean log_familyBalancing = false;

    private String consoleIcons_attemptingRegistration = "?>>>?";
    private String consoleIcons_registered = ">>>>>";
    private String consoleIcons_attemptingUnregistration = "?///?";
    private String consoleIcons_unregistered = "/////";
    private String consoleIcons_canceledRequest = "xxxxx";
    private String consoleIcons_familyBalancing = "▲▼▲▼▲";
    private String consoleIcons_ping = "|>>>>";

    private LoggerConfig(File configPointer, String template) {
        super(configPointer, template);
    }

    public boolean shouldSaveTrashedMessages() {
        return saveTrashedMessages;
    }

    public boolean isMessaging_registration() {
        return messaging_registration;
    }

    public boolean isMessaging_unregistration() {
        return messaging_unregistration;
    }

    public boolean isMessaging_ping() {
        return messaging_ping;
    }

    public boolean isMessaging_messageParserTrash() {
        return messaging_messageParserTrash;
    }

    public boolean isSecurity_messageTunnelFailedMessage() {
        return security_messageTunnelFailedMessage;
    }

    public boolean isLog_playerJoin() {
        return log_playerJoin;
    }

    public boolean isLog_playerLeave() {
        return log_playerLeave;
    }

    public boolean isLog_playerMove() {
        return log_playerMove;
    }

    public boolean isLog_familyBalancing() {
        return log_familyBalancing;
    }

    public String getConsoleIcons_attemptingRegistration() {
        return consoleIcons_attemptingRegistration;
    }

    public String getConsoleIcons_registered() {
        return consoleIcons_registered;
    }

    public String getConsoleIcons_attemptingUnregistration() {
        return consoleIcons_attemptingUnregistration;
    }

    public String getConsoleIcons_unregistered() {
        return consoleIcons_unregistered;
    }

    public String getConsoleIcons_canceledRequest() {
        return consoleIcons_canceledRequest;
    }

    public String getConsoleIcons_familyBalancing() {
        return consoleIcons_familyBalancing;
    }

    public String getConsoleIcons_ping() {
        return consoleIcons_ping;
    }

    /**
     * Get the current config.
     * @return The config.
     */
    public static LoggerConfig getConfig() {
        return config;
    }

    /**
     * Create a new config for the proxy, this will delete the old config.
     * @return The newly created config.
     */
    public static LoggerConfig newConfig(File configPointer, String template) {
        config = new LoggerConfig(configPointer, template);
        return LoggerConfig.getConfig();
    }

    /**
     * Delete all configs associated with this class.
     */
    public static void empty() {
        config = null;
    }

    public void register() throws IllegalStateException {
        this.saveTrashedMessages = this.getNode(this.data,"save-trashed-messages",Boolean.class);

        this.messaging_registration = this.getNode(this.data,"messaging.registration",Boolean.class);
        this.messaging_unregistration = this.getNode(this.data,"messaging.unregistration",Boolean.class);
        this.messaging_ping = this.getNode(this.data,"messaging.ping",Boolean.class);
        this.messaging_messageParserTrash = this.getNode(this.data,"messaging.message-parser-trash",Boolean.class);

        this.security_messageTunnelFailedMessage = this.getNode(this.data,"security.message-tunnel-failed-message",Boolean.class);

        this.log_playerJoin = this.getNode(this.data,"log.player-join",Boolean.class);
        this.log_playerLeave = this.getNode(this.data,"log.player-leave",Boolean.class);
        this.log_playerMove = this.getNode(this.data,"log.player-move",Boolean.class);
        this.log_familyBalancing = this.getNode(this.data,"log.family-balancing",Boolean.class);

        this.consoleIcons_attemptingRegistration = this.getNode(this.data,"console-icons.attempting-registration",String.class);
        this.consoleIcons_registered = this.getNode(this.data,"console-icons.registered",String.class);
        this.consoleIcons_attemptingUnregistration = this.getNode(this.data,"console-icons.attempting-unregistration",String.class);
        this.consoleIcons_unregistered = this.getNode(this.data,"console-icons.unregistered",String.class);
        this.consoleIcons_canceledRequest = this.getNode(this.data,"console-icons.canceled-request",String.class);
        this.consoleIcons_familyBalancing = this.getNode(this.data,"console-icons.family-balancing",String.class);
        this.consoleIcons_ping = this.getNode(this.data,"console-icons.ping",String.class);
    }
}
