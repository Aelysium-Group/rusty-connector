package group.aelysium.rustyconnector.plugin.velocity.lib.config.configs;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.core.lib.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigService;
import group.aelysium.rustyconnector.toolkit.core.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.core.config.IYAML;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketStatus;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import net.kyori.adventure.text.format.NamedTextColor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DataTransitConfig extends YAML implements group.aelysium.rustyconnector.toolkit.velocity.config.DataTransitConfig {
    private int cache_size = 100;
    private final List<PacketIdentification> cache_ignoredTypes = new ArrayList<>();
    private final List<PacketStatus> cache_ignoredStatuses = new ArrayList<>();

    private boolean whitelist_enabled = false;
    private List<String> whitelist_addresses = new ArrayList<>();
    private boolean denylist_enabled = false;
    private List<String> denylist_addresses = new ArrayList<>();


    public int cache_size() {
        return cache_size;
    }

    public List<PacketIdentification> cache_ignoredTypes() {
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

    protected DataTransitConfig(Path dataFolder, String target, String name, LangService lang) {
        super(dataFolder, target, name, lang, LangFileMappings.PROXY_DATA_TRANSIT_TEMPLATE);
    }

    @Override
    public IConfigService.ConfigKey key() {
        return IConfigService.ConfigKey.singleton(DataTransitConfig.class);
    }

    @SuppressWarnings("unchecked")
    protected void register() throws IllegalStateException, NoOutputException {
        PluginLogger logger = Tinder.get().logger();

        this.cache_size = IYAML.getValue(this.data,"cache.size",Integer.class);
        if(this.cache_size > 500) {
            ProxyLang.BOXED_MESSAGE_COLORED.send(logger, "Message cache size is to large! " + this.cache_size + " > 500. Message cache size set to 500.", NamedTextColor.YELLOW);
            this.cache_size = 500;
        }
        try {
            List<String> stringTypes = IYAML.get(this.data,"cache.ignored-types").getList(String.class, new ArrayList<>());
            stringTypes.forEach(item -> {
                try {
                    this.cache_ignoredTypes.add(BuiltInIdentifications.mapping(item));
                } catch (Exception ignore) {
                    logger.send(ProxyLang.BOXED_MESSAGE_COLORED.build("There is no packet type of "+item+"! Ignoring...", NamedTextColor.YELLOW));
                }
            });
        } catch (Exception e) {
            throw new IllegalStateException("The node [cache.ignored-types] in "+this.name()+" is invalid! Make sure you are using the correct type of data!");
        }
        try {
            List<String> stringStatuses = IYAML.get(this.data,"cache.ignored-statuses").getList(String.class, new ArrayList<>());
            stringStatuses.forEach(item -> {
                try {
                    this.cache_ignoredStatuses.add(PacketStatus.valueOf(item));
                } catch (Exception ignore) {
                    logger.send(ProxyLang.BOXED_MESSAGE_COLORED.build("There is no packet status type of "+item+"! Ignoring...", NamedTextColor.YELLOW));
                }
            });
        } catch (Exception e) {
            throw new IllegalStateException("The node [cache.ignored-statuses] in "+this.name()+" is invalid! Make sure you are using the correct type of data!");
        }

        this.whitelist_enabled = IYAML.getValue(this.data,"whitelist.enabled",Boolean.class);
        try {
            this.whitelist_addresses = IYAML.get(this.data,"whitelist.addresses").getList(String.class, new ArrayList<>());
        } catch (Exception e) {
            throw new IllegalStateException("The node [whitelist.addresses] in "+this.name()+" is invalid! Make sure you are using the correct type of data!");
        }
        this.denylist_enabled = IYAML.getValue(this.data,"denylist.enabled",Boolean.class);
        try {
            this.denylist_addresses = IYAML.get(this.data,"denylist.addresses").getList(String.class, new ArrayList<>());
        } catch (Exception e) {
            throw new IllegalStateException("The node [denylist.addresses] in "+this.name()+" is invalid! Make sure you are using the correct type of data!");
        }
    }

    public static DataTransitConfig construct(Path dataFolder, LangService lang, ConfigService configService) {
        DataTransitConfig config = new DataTransitConfig(dataFolder, "extras/data_transit.yml", "data_transit", lang);
        config.register();
        configService.put(config);
        return config;
    }
}
