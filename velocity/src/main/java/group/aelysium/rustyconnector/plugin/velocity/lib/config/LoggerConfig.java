package group.aelysium.rustyconnector.plugin.velocity.lib.config;

import java.io.File;

public class LoggerConfig extends YAML {
    private static LoggerConfig config;

    private boolean messaging_registrationRequest = false;
    private boolean messaging_unregistrationRequest = false;
    private boolean messaging_callForRegistration = false;
    private boolean messaging_ping = false;
    private boolean messaging_pong = false;
    private boolean messaging_messageParserTrash = false;

    private boolean security_blacklistedAddressMessage = true;
    private boolean security_whitelistDeniedAddressMessage = true;

    private boolean log_playerJoin = false;
    private boolean log_playerLeave = false;
    private boolean log_playerMove = false;
    private boolean log_familyBalancing = false;

    private String consoleIcons_requestingRegistration = "?>>>?";
    private String consoleIcons_registered = ">>>>>";
    private String consoleIcons_callForRegistration = "|>~=-";
    private String consoleIcons_requestingUnregistration = "?///?";
    private String consoleIcons_unregistered = "/////";
    private String consoleIcons_canceledRequest = "xxxxx";
    private String consoleIcons_familyBalancing = "▲▼▲▼▲";
    private String consoleIcons_ping = "|>>>>";
    private String consoleIcons_pong = "<<<<|";

    private LoggerConfig(File configPointer, String template) {
        super(configPointer, template);
    }

    public boolean isMessaging_registrationRequest() {
        return messaging_registrationRequest;
    }

    public boolean isMessaging_unregistrationRequest() {
        return messaging_unregistrationRequest;
    }

    public boolean isMessaging_callForRegistration() {
        return messaging_callForRegistration;
    }

    public boolean isMessaging_ping() {
        return messaging_ping;
    }

    public boolean isMessaging_pong() {
        return messaging_pong;
    }

    public boolean isMessaging_messageParserTrash() {
        return messaging_messageParserTrash;
    }

    public boolean isSecurity_blacklistedAddressMessage() {
        return security_blacklistedAddressMessage;
    }

    public boolean isSecurity_whitelistDeniedAddressMessage() {
        return security_whitelistDeniedAddressMessage;
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

    public String getConsoleIcons_requestingRegistration() {
        return consoleIcons_requestingRegistration;
    }

    public String getConsoleIcons_registered() {
        return consoleIcons_registered;
    }

    public String getConsoleIcons_callForRegistration() {
        return consoleIcons_callForRegistration;
    }

    public String getConsoleIcons_requestingUnregistration() {
        return consoleIcons_requestingUnregistration;
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

    public String getConsoleIcons_pong() {
        return consoleIcons_pong;
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

    @SuppressWarnings("unchecked")
    public void register() throws IllegalStateException {
        this.messaging_registrationRequest = this.getNode(this.data,"messaging.registration-request",Boolean.class);
        this.messaging_unregistrationRequest = this.getNode(this.data,"messaging.unregistration-request",Boolean.class);
        this.messaging_callForRegistration = this.getNode(this.data,"messaging.call-for-registration",Boolean.class);
        this.messaging_ping = this.getNode(this.data,"messaging.ping",Boolean.class);
        this.messaging_pong = this.getNode(this.data,"messaging.pong",Boolean.class);
        this.messaging_messageParserTrash = this.getNode(this.data,"messaging.message-parser-trash",Boolean.class);

        this.security_blacklistedAddressMessage = this.getNode(this.data,"security.blacklisted-address-message",Boolean.class);
        this.security_whitelistDeniedAddressMessage = this.getNode(this.data,"security.whitelist-denied-address-message",Boolean.class);

        this.log_playerJoin = this.getNode(this.data,"log.player-join",Boolean.class);
        this.log_playerLeave = this.getNode(this.data,"log.player-leave",Boolean.class);
        this.log_playerMove = this.getNode(this.data,"log.player-move",Boolean.class);
        this.log_familyBalancing = this.getNode(this.data,"log.family-balancing",Boolean.class);

        this.consoleIcons_requestingRegistration = this.getNode(this.data,"console-icons.requesting-registration",String.class);
        this.consoleIcons_registered = this.getNode(this.data,"console-icons.registered",String.class);
        this.consoleIcons_callForRegistration = this.getNode(this.data,"console-icons.call-for-registration",String.class);
        this.consoleIcons_requestingUnregistration = this.getNode(this.data,"console-icons.requesting-unregistration",String.class);
        this.consoleIcons_unregistered = this.getNode(this.data,"console-icons.unregistered",String.class);
        this.consoleIcons_canceledRequest = this.getNode(this.data,"console-icons.canceled-request",String.class);
        this.consoleIcons_familyBalancing = this.getNode(this.data,"console-icons.family-balancing",String.class);
        this.consoleIcons_ping = this.getNode(this.data,"console-icons.ping",String.class);
        this.consoleIcons_pong = this.getNode(this.data,"console-icons.pong",String.class);
    }
}
