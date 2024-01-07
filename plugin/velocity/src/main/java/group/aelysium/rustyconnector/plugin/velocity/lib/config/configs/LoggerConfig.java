package group.aelysium.rustyconnector.plugin.velocity.lib.config.configs;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigService;
import group.aelysium.rustyconnector.toolkit.core.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.core.config.IYAML;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;

import java.nio.file.Path;

public class LoggerConfig extends YAML implements group.aelysium.rustyconnector.toolkit.velocity.config.LoggerConfig {
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

    protected LoggerConfig(Path dataFolder, String target, String name, LangService lang) {
        super(dataFolder, target, name, lang, LangFileMappings.PROXY_LOGGER_TEMPLATE);
    }

    @Override
    public IConfigService.ConfigKey key() {
        return IConfigService.ConfigKey.singleton(LoggerConfig.class);
    }

    protected void register() throws IllegalStateException {
        this.saveTrashedMessages = IYAML.getValue(this.data,"save-trashed-messages",Boolean.class);

        this.messaging_registration = IYAML.getValue(this.data,"messaging.registration",Boolean.class);
        this.messaging_unregistration = IYAML.getValue(this.data,"messaging.unregistration",Boolean.class);
        this.messaging_ping = IYAML.getValue(this.data,"messaging.ping",Boolean.class);
        this.messaging_messageParserTrash = IYAML.getValue(this.data,"messaging.message-parser-trash",Boolean.class);

        this.security_messageTunnelFailedMessage = IYAML.getValue(this.data,"security.message-tunnel-failed-message",Boolean.class);

        this.log_playerJoin = IYAML.getValue(this.data,"log.player-join",Boolean.class);
        this.log_playerLeave = IYAML.getValue(this.data,"log.player-leave",Boolean.class);
        this.log_playerMove = IYAML.getValue(this.data,"log.player-move",Boolean.class);
        this.log_familyBalancing = IYAML.getValue(this.data,"log.family-balancing",Boolean.class);
    }

    public static LoggerConfig construct(Path dataFolder, LangService lang, ConfigService configService) {
        LoggerConfig config = new LoggerConfig(dataFolder, "extras/logger.yml", "logger", lang);
        config.register();
        configService.put(config);
        return config;
    }
}
