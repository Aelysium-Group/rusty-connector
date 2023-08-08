package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageStatus;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DataTransitConfig extends YAML {
    private static DataTransitConfig config;

    private int maxPacketLength = 512;

    private int cache_size = 100;
    private final List<RedisMessageType.Mapping> cache_ignoredTypes = new ArrayList<>();
    private final List<MessageStatus> cache_ignoredStatuses = new ArrayList<>();

    private boolean whitelist_enabled = false;
    private List<String> whitelist_addresses = new ArrayList<>();
    private boolean denylist_enabled = false;
    private List<String> denylist_addresses = new ArrayList<>();

    public int maxPacketLength() {
        return maxPacketLength;
    }

    public int cache_size() {
        return cache_size;
    }

    public List<RedisMessageType.Mapping> cache_ignoredTypes() {
        return cache_ignoredTypes;
    }

    public List<MessageStatus> cache_ignoredStatuses() {
        return cache_ignoredStatuses;
    }

    public boolean whitelist_enabled() {
        return whitelist_enabled;
    }

    public List<String> whitelist_addresses() {
        return whitelist_addresses;
    }

    public boolean denylist_enabled() {
        return denylist_enabled;
    }

    public List<String> denylist_addresses() {
        return denylist_addresses;
    }

    private DataTransitConfig(File configPointer, String template) {
        super(configPointer, template);
    }

    /**
     * Get the current config.
     * @return The config.
     */
    public static DataTransitConfig config() {
        return config;
    }

    /**
     * Create a new config for the proxy, this will delete the old config.
     * @return The newly created config.
     */
    public static DataTransitConfig newConfig(File configPointer, String template) {
        config = new DataTransitConfig(configPointer, template);
        return DataTransitConfig.config();
    }

    /**
     * Delete all configs associated with this class.
     */
    public static void empty() {
        config = null;
    }

    @SuppressWarnings("unchecked")
    public void register() throws IllegalStateException, NoOutputException {
        PluginLogger logger = VelocityAPI.get().logger();

        this.maxPacketLength = this.getNode(this.data,"max-packet-length",Integer.class);
        if(this.maxPacketLength < 384) {
            Lang.BOXED_MESSAGE_COLORED.send(logger, Component.text("Max message length is to small to be effective! " + this.maxPacketLength + " < 384. Max message length set to 384."), NamedTextColor.YELLOW);
            this.maxPacketLength = 384;
        }

        this.cache_size = this.getNode(this.data,"cache.size",Integer.class);
        if(this.cache_size > 500) {
            Lang.BOXED_MESSAGE_COLORED.send(logger, Component.text("Message cache size is to large! " + this.cache_size + " > 500. Message cache size set to 500."), NamedTextColor.YELLOW);
            this.cache_size = 500;
        }
        try {
            List<String> stringTypes = (List<String>) this.getNode(this.data,"cache.ignored-types",List.class);
            stringTypes.forEach(item -> {
                try {
                    this.cache_ignoredTypes.add(RedisMessageType.mapping(item));
                } catch (Exception ignore) {
                    logger.send(VelocityLang.BOXED_MESSAGE_COLORED.build(Component.text("There is no packet type of "+item+"! Ignoring..."), NamedTextColor.YELLOW));
                }
            });
        } catch (Exception e) {
            throw new IllegalStateException("The node [cache.ignored-types] in "+this.getName()+" is invalid! Make sure you are using the correct type of data!");
        }
        try {
            List<String> stringStatuses = (List<String>) this.getNode(this.data,"cache.ignored-statuses",List.class);
            stringStatuses.forEach(item -> {
                try {
                    this.cache_ignoredStatuses.add(MessageStatus.valueOf(item));
                } catch (Exception ignore) {
                    logger.send(VelocityLang.BOXED_MESSAGE_COLORED.build(Component.text("There is no packet status type of "+item+"! Ignoring..."), NamedTextColor.YELLOW));
                }
            });
        } catch (Exception e) {
            throw new IllegalStateException("The node [cache.ignored-statuses] in "+this.getName()+" is invalid! Make sure you are using the correct type of data!");
        }

        this.whitelist_enabled = this.getNode(this.data,"whitelist.enabled",Boolean.class);
        try {
            this.whitelist_addresses = (List<String>) this.getNode(this.data,"whitelist.addresses",List.class);
        } catch (Exception e) {
            throw new IllegalStateException("The node [whitelist.addresses] in "+this.getName()+" is invalid! Make sure you are using the correct type of data!");
        }
        this.denylist_enabled = this.getNode(this.data,"denylist.enabled",Boolean.class);
        try {
            this.denylist_addresses = (List<String>) this.getNode(this.data,"denylist.addresses",List.class);
        } catch (Exception e) {
            throw new IllegalStateException("The node [denylist.addresses] in "+this.getName()+" is invalid! Make sure you are using the correct type of data!");
        }
    }
}
