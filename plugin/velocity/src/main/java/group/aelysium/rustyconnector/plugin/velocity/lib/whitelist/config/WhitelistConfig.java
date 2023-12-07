package group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.config;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WhitelistConfig extends YAML {
    private boolean use_players = false;
    private List<Object> players = new ArrayList<>();

    private boolean use_permission = false;

    private boolean use_country = false;
    private List<String> countries = new ArrayList<>();
    private String message = "You aren't whitelisted on this server!";
    private boolean strict = false;
    private boolean inverted = false;

    public WhitelistConfig(File configPointer) {
        super(configPointer);
    }
    public boolean getUse_players() {
        return use_players;
    }

    public List<Object> getPlayers() {
        return players;
    }

    public boolean getUse_permission() {
        return use_permission;
    }

    public boolean getUse_country() {
        return use_country;
    }

    public List<String> getCountries() {
        return countries;
    }

    public String getMessage() {
        return message;
    }
    public boolean isStrict() {
        return strict;
    }
    public boolean isInverted() {
        return inverted;
    }

    @SuppressWarnings("unchecked")
    public void register() throws IllegalStateException {
        PluginLogger logger = Tinder.get().logger();

        this.use_players = this.getNode(this.data,"use-players",Boolean.class);
        try {
            this.players = (this.getNode(this.data,"players",List.class));
        } catch (ClassCastException e) {
            throw new IllegalStateException("The node [players] in "+this.getName()+" is invalid! Make sure you are using the correct type of data!");
        }

        this.use_permission = this.getNode(this.data,"use-permission",Boolean.class);

        this.use_country = this.getNode(this.data,"use-country",Boolean.class);
        if(this.use_country)
            ProxyLang.BOXED_MESSAGE_COLORED.send(logger, "RustyConnector does not currently support country codes in whitelists. Setting `use-country` to false.", NamedTextColor.YELLOW);
        this.use_country = false;
        this.countries = new ArrayList<>();

        this.message = this.getNode(data,"message",String.class);
        if(this.message.equalsIgnoreCase(""))
            throw new IllegalStateException("Whitelist kick messages cannot be empty!");

        this.strict = this.getNode(data,"strict",Boolean.class);

        this.inverted = this.getNode(data,"inverted",Boolean.class);
    }
}
