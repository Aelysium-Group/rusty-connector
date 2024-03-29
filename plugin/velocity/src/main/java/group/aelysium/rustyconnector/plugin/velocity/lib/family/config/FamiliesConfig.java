package group.aelysium.rustyconnector.plugin.velocity.lib.family.config;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.lang.Lang;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FamiliesConfig extends YAML {
    private String rootFamily_name = "lobby";
    private Boolean rootFamily_catchDisconnectingPlayers = false;
    private List<String> scalar = new ArrayList<>();
    private List<String> staticF = new ArrayList<>();

    public FamiliesConfig(File configPointer) {
        super(configPointer);
    }

    public String rootFamilyName() {
        return this.rootFamily_name;
    }
    public Boolean shouldRootFamilyCatchDisconnectingPlayers() {
        return this.rootFamily_catchDisconnectingPlayers;
    }

    public List<String> scalarFamilies() {
        return this.scalar;
    }
    public List<String> staticFamilies() {
        return this.staticF;
    }

    @SuppressWarnings("unchecked")
    public void register() throws IllegalStateException, NoOutputException {
        PluginLogger logger = Tinder.get().logger();

        // Families
        try {
            this.rootFamily_name = this.getNode(this.data, "root-family.name", String.class);
            if (this.rootFamily_name.equals("") || this.rootFamily_name.length() < 1) throw new Exception();
        } catch (Exception ignore) {
            VelocityLang.BOXED_MESSAGE_COLORED.send(logger, "Your [root-family.name] is empty or unparseable. It has been set to the default of \"lobby\"", NamedTextColor.YELLOW);
            this.rootFamily_name = "lobby";
        }

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
            VelocityLang.BOXED_MESSAGE_COLORED.send(logger, this.rootFamily_name + " was found duplicated in your family nodes. This is no longer supported. Instead, ONLY place the name of your root family in [root-family.name]. Ignoring...", NamedTextColor.YELLOW);
            this.scalar.remove(this.rootFamily_name);
            this.staticF.remove(this.rootFamily_name);
        }
    }

    private boolean doDuplicatesExist() {
        return this.scalar.stream().filter(this.staticF::contains).toList().size() > 0;
    }

    private boolean isRootFamilyDuplicated() {
        return this.scalar.contains(this.rootFamily_name) ||
               this.staticF.contains(this.rootFamily_name);
    }

    private boolean checkForAll() {
        return this.rootFamily_name.equalsIgnoreCase("all") ||
               this.scalar.contains("all") ||
               this.staticF.contains("all");
    }
}
