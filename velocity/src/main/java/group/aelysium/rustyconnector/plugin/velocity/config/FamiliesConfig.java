package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FamiliesConfig extends YAML {
    private static FamiliesConfig config;
    private String rootFamily_name = "lobby";
    private Boolean rootFamily_catchDisconnectingPlayers = false;
    private List<String> scalar = new ArrayList<>();
    private List<String> staticF = new ArrayList<>();
    private List<String> rounded = new ArrayList<>();

    private FamiliesConfig(File configPointer, String template) {
        super(configPointer, template);
    }

    /**
     * Get the current config.
     * @return The config.
     */
    public static FamiliesConfig getConfig() {
        return config;
    }

    /**
     * Create a new config for the proxy, this will delete the old config.
     * @return The newly created config.
     */
    public static FamiliesConfig newConfig(File configPointer, String template) {
        config = new FamiliesConfig(configPointer, template);
        return FamiliesConfig.getConfig();
    }

    /**
     * Delete all configs associated with this class.
     */
    public static void empty() {
        config = null;
    }

    public String getRootFamilyName() {
        return this.rootFamily_name;
    }
    public Boolean shouldRootFamilyCatchDisconnectingPlayers() {
        return this.rootFamily_catchDisconnectingPlayers;
    }

    public List<String> getScalarFamilies() {
        return this.scalar;
    }
    public List<String> getStaticFamilies() {
        return this.staticF;
    }
    public List<String> getRoundedFamilies() {
        return this.rounded;
    }

    @SuppressWarnings("unchecked")
    public void register() throws IllegalStateException, NoOutputException {
        PluginLogger logger = VelocityRustyConnector.getAPI().getLogger();

        // Families
        this.rootFamily_name = this.getNode(this.data,"root-family.name",String.class);
        this.rootFamily_catchDisconnectingPlayers = this.getNode(this.data,"root-family.catch-disconnecting-players",Boolean.class);
        try {
            this.scalar = (List<String>) (this.getNode(this.data,"scalar",List.class));
        } catch (Exception e) {
            throw new IllegalStateException("The node [scalar] in "+this.getName()+" is invalid! Make sure you are using the correct type of data!");
        }
        try {
            this.staticF = (List<String>) (this.getNode(this.data,"static",List.class));
        } catch (Exception e) {
            throw new IllegalStateException("The node [scalar] in "+this.getName()+" is invalid! Make sure you are using the correct type of data!");
        }
        try {
            this.rounded = (List<String>) (this.getNode(this.data,"rounded",List.class));
        } catch (Exception e) {
            throw new IllegalStateException("The node [scalar] in "+this.getName()+" is invalid! Make sure you are using the correct type of data!");
        }

        this.scalar.forEach(familyName -> {
            if(familyName.length() > 32)
                throw new IllegalStateException("All family names must be under 32 characters long! `" + familyName + "` was " + familyName.length());
        });

        this.staticF.forEach(familyName -> {
            if(familyName.length() > 32)
                throw new IllegalStateException("All family names must be under 32 characters long! `" + familyName + "` was " + familyName.length());
        });

        if(this.checkForAll())
            throw new IllegalStateException("You can't name a family: `all`");

        if(this.doDuplicatesExist())
            throw new IllegalStateException("You can't have two families with the same name! This rule is regardless of what type the family is!");

        if(this.isRootFamilyDuplicated()) {
            Lang.BOXED_MESSAGE_COLORED.send(logger, Component.text(this.rootFamily_name + " was found duplicated in your family nodes. This is no longer supported. Instead, ONLY place the name of your root family in [root-family.name]. Ignoring..."), NamedTextColor.YELLOW);
            this.scalar.remove(this.rootFamily_name);
            this.staticF.remove(this.rootFamily_name);
            this.rounded.remove(this.rootFamily_name);
        }
    }

    private boolean doDuplicatesExist() {
        return this.scalar.stream().filter(this.staticF::contains).filter(this.rounded::contains).toList().size() > 0;
    }

    private boolean isRootFamilyDuplicated() {
        return this.scalar.contains(this.rootFamily_name) ||
               this.staticF.contains(this.rootFamily_name) ||
               this.rounded.contains(this.rootFamily_name);
    }

    private boolean checkForAll() {
        return this.rootFamily_name.equalsIgnoreCase("all") ||
               this.scalar.contains("all") ||
               this.staticF.contains("all") ||
               this.rounded.contains("all");
    }
}
