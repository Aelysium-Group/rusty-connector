package group.aelysium.rustyconnector.plugin.velocity.lib.config.configs;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.config.DynamicTeleportConfig;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketStatus;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketType;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DataTransitConfig extends YAML {
    private int maxPacketLength = 512;

    private int cache_size = 100;
    private final List<PacketType.Mapping> cache_ignoredTypes = new ArrayList<>();
    private final List<PacketStatus> cache_ignoredStatuses = new ArrayList<>();

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

    public List<PacketType.Mapping> cache_ignoredTypes() {
        return cache_ignoredTypes;
    }

    public List<PacketStatus> cache_ignoredStatuses() {
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

    protected DataTransitConfig(Path dataFolder, String target, LangService lang) {
        super(dataFolder, target, lang, LangFileMappings.PROXY_DATA_TRANSIT_TEMPLATE);
    }

    @SuppressWarnings("unchecked")
    protected void register() throws IllegalStateException, NoOutputException {
        PluginLogger logger = Tinder.get().logger();

        this.maxPacketLength = this.getNode(this.data,"max-packet-length",Integer.class);
        if(this.maxPacketLength < 384) {
            ProxyLang.BOXED_MESSAGE_COLORED.send(logger, "Max message length is to small to be effective! " + this.maxPacketLength + " < 384. Max message length set to 384.", NamedTextColor.YELLOW);
            this.maxPacketLength = 384;
        }

        this.cache_size = this.getNode(this.data,"cache.size",Integer.class);
        if(this.cache_size > 500) {
            ProxyLang.BOXED_MESSAGE_COLORED.send(logger, "Message cache size is to large! " + this.cache_size + " > 500. Message cache size set to 500.", NamedTextColor.YELLOW);
            this.cache_size = 500;
        }
        try {
            List<String> stringTypes = (List<String>) this.getNode(this.data,"cache.ignored-types",List.class);
            stringTypes.forEach(item -> {
                try {
                    this.cache_ignoredTypes.add(PacketType.mapping(item));
                } catch (Exception ignore) {
                    logger.send(ProxyLang.BOXED_MESSAGE_COLORED.build("There is no packet type of "+item+"! Ignoring...", NamedTextColor.YELLOW));
                }
            });
        } catch (Exception e) {
            throw new IllegalStateException("The node [cache.ignored-types] in "+this.getName()+" is invalid! Make sure you are using the correct type of data!");
        }
        try {
            List<String> stringStatuses = (List<String>) this.getNode(this.data,"cache.ignored-statuses",List.class);
            stringStatuses.forEach(item -> {
                try {
                    this.cache_ignoredStatuses.add(PacketStatus.valueOf(item));
                } catch (Exception ignore) {
                    logger.send(ProxyLang.BOXED_MESSAGE_COLORED.build("There is no packet status type of "+item+"! Ignoring...", NamedTextColor.YELLOW));
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

    public static DataTransitConfig construct(Path dataFolder, LangService lang) {
        DataTransitConfig config = new DataTransitConfig(dataFolder, "extras/data_transit.yml", lang);
        config.register();
        return config;
    }
}
