package group.aelysium.rustyconnector.plugin.velocity.lib.config.configs;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.core.lib.messenger.config.ConnectorsConfig;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;

import java.io.File;
import java.nio.file.Path;

public class LoggerConfig extends YAML {
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

    protected LoggerConfig(Path dataFolder, String target, LangService lang) {
        super(dataFolder, target, lang, LangFileMappings.PROXY_LOGGER_TEMPLATE);
    }

    protected void register() throws IllegalStateException {
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
    }

    public static LoggerConfig construct(Path dataFolder, LangService lang) {
        LoggerConfig config = new LoggerConfig(dataFolder, "extras/logger.yml", lang);
        config.register();
        return config;
    }
}
