package group.aelysium.rustyconnector.plugin.paper.config;

import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.util.List;

public class RoundedLifecycleConfig extends YAML {

    private static RoundedLifecycleConfig config;

    private int start_delay;
    private List<String> start_commands;

    private int end_delay;
    private List<String> end_commands;
    private int end_delayToClosing;

    private List<String> close_commands;

    private RoundedLifecycleConfig(File configPointer, String template) {
        super(configPointer, template);
    }

    public int getStart_delay() {
        return start_delay;
    }

    public List<String> getStart_commands() {
        return start_commands;
    }

    public int getEnd_delay() {
        return end_delay;
    }

    public List<String> getEnd_commands() {
        return end_commands;
    }

    public int getEnd_delayToClosing() {
        return end_delayToClosing;
    }

    public List<String> getClose_commands() {
        return close_commands;
    }

    /**
     * Get the current config.
     * @return The config.
     */
    public static RoundedLifecycleConfig getConfig() {
        return config;
    }

    /**
     * Create a new config for the proxy, this will delete the old config.
     * @return The newly created config.
     */
    public static RoundedLifecycleConfig newConfig(File configPointer, String template) {
        config = new RoundedLifecycleConfig(configPointer, template);
        return RoundedLifecycleConfig.getConfig();
    }

    /**
     * Delete all configs associated with this class.
     */
    public static void empty() {
        config = null;
    }

    @SuppressWarnings("unchecked")
    public void register() throws IllegalStateException {
        this.start_delay = this.getNode(this.data,"start.delay",Integer.class);
        try {
            this.start_commands = (List<String>) (this.getNode(this.data,"start.commands",List.class));
        } catch (Exception e) {
            throw new IllegalStateException("The node [start.commands] in "+this.getName()+" is invalid! Make sure you are using the correct type of data!");
        }

        this.end_delay = this.getNode(this.data,"end.delay",Integer.class);
        try {
            this.end_commands = (List<String>) (this.getNode(this.data,"end.commands",List.class));
        } catch (Exception e) {
            throw new IllegalStateException("The node [end.commands] in "+this.getName()+" is invalid! Make sure you are using the correct type of data!");
        }
        this.end_delayToClosing = this.getNode(this.data,"end.delay-to-close",Integer.class);

        try {
            this.close_commands = (List<String>) (this.getNode(this.data,"close.commands",List.class));
        } catch (Exception e) {
            throw new IllegalStateException("The node [close.commands] in "+this.getName()+" is invalid! Make sure you are using the correct type of data!");
        }
    }
}
