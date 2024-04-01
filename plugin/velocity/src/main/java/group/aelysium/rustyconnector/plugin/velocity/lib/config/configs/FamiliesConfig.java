package group.aelysium.rustyconnector.plugin.velocity.lib.config.configs;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.toolkit.core.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.core.config.IYAML;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import net.kyori.adventure.text.format.NamedTextColor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FamiliesConfig extends YAML implements group.aelysium.rustyconnector.toolkit.velocity.config.FamiliesConfig {
    private String rootFamily_name = "lobby";
    private Boolean rootFamily_catchDisconnectingPlayers = false;
    private Map<String, Boolean> scalar = new HashMap<>();
    private Map<String, Boolean> staticF = new HashMap<>();
    private Map<String, Boolean> ranked = new HashMap<>();

    protected FamiliesConfig(Path dataFolder, String target, String name, LangService lang) {
        super(dataFolder, target, name, lang, LangFileMappings.PROXY_FAMILIES_TEMPLATE);
    }

    @Override
    public IConfigService.ConfigKey key() {
        return IConfigService.ConfigKey.singleton(FamiliesConfig.class);
    }

    public String rootFamilyName() {
        return this.rootFamily_name;
    }
    public Boolean shouldRootFamilyCatchDisconnectingPlayers() {
        return this.rootFamily_catchDisconnectingPlayers;
    }

    public List<String> scalarFamilies() {
        return this.scalar.keySet().stream().toList();
    }
    public List<String> staticFamilies() {
        return this.staticF.keySet().stream().toList();
    }
    public List<String> rankedFamilies() {
        return this.ranked.keySet().stream().toList();
    }

    @SuppressWarnings("unchecked")
    protected void register() throws IllegalStateException, NoOutputException {
        PluginLogger logger = Tinder.get().logger();

        // Families
        try {
            this.rootFamily_name = IYAML.getValue(this.data, "root-family.name", String.class);
            if (this.rootFamily_name.equals("") || this.rootFamily_name.length() < 1) throw new Exception();
        } catch (Exception ignore) {
            ProxyLang.BOXED_MESSAGE_COLORED.send(logger, "Your [root-family.name] is empty or unparseable. It has been set to the default of \"lobby\"", NamedTextColor.YELLOW);
            this.rootFamily_name = "lobby";
        }

        this.rootFamily_catchDisconnectingPlayers = IYAML.getValue(this.data,"root-family.catch-disconnecting-players",Boolean.class);
        try {
            List<String> array = IYAML.get(this.data,"scalar").getList(String.class, new ArrayList<>());
            array.forEach(item -> this.scalar.put(item.toLowerCase(), false));
        } catch (Exception e) {
            throw new IllegalStateException("The node [scalar] in "+this.name()+" is invalid! Make sure you are using the correct type of data!");
        }
        try {
            List<String> array = IYAML.get(this.data,"static").getList(String.class, new ArrayList<>());
            array.forEach(item -> this.staticF.put(item.toLowerCase(), false));
        } catch (Exception e) {
            throw new IllegalStateException("The node [scalar] in "+this.name()+" is invalid! Make sure you are using the correct type of data!");
        }
        try {
            List<String> array = IYAML.get(this.data,"ranked").getList(String.class, new ArrayList<>());
            array.forEach(item -> this.ranked.put(item.toLowerCase(), false));
        } catch (Exception e) {
            throw new IllegalStateException("The node [ranked] in "+this.name()+" is invalid! Make sure you are using the correct type of data!");
        }



        // Get ready to perform some checks
        List<String> all = new ArrayList<>();
        all.addAll(this.scalarFamilies());
        all.addAll(this.staticFamilies());
        all.addAll(this.rankedFamilies());

        // Check family name length
        {
            all.forEach(familyName -> {
                if (familyName.length() > 16)
                    throw new IllegalStateException("All family names must be under 16 characters long! `" + familyName + "` was " + familyName.length());
            });
        }

        // Check for the word "all"
        {
            if (all.contains("all"))
                throw new IllegalStateException("You can't name a family: `all`");
        }

        // Check for duplicate names
        {
            Map<String, Integer> familyNames = new HashMap<>();
            for (String name : all) {
                if (familyNames.containsKey(name)) {
                    familyNames.put(name, familyNames.get(name) + 1);
                    continue;
                }

                familyNames.put(name, 0);
            }
            for (Map.Entry<String, Integer> entry : familyNames.entrySet()) {
                if (entry.getValue() > 0)
                    throw new IllegalStateException("You can't have two families with the same id! This rule is regardless of what type the family is! Found duplicate: " + entry.getKey());
            }
        }
    }

    public static FamiliesConfig construct(Path dataFolder, LangService lang, ConfigService configService) {
        FamiliesConfig config = new FamiliesConfig(dataFolder, "families.yml", "families", lang);
        config.register();
        configService.put(config);
        return config;
    }
}
