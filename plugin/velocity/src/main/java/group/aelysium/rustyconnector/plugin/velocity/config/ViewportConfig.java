package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.model.Role;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.model.User;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class ViewportConfig extends YAML {
    private static ViewportConfig config;

    private boolean enabled = false;

    private String storage = "";

    private InetSocketAddress websocket_address;

    private InetSocketAddress rest_address;

    private List<Role> roles = new ArrayList<>();
    private List<User> users = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }
    public String storage() {
        return storage;
    }
    public InetSocketAddress getWebsocket_address() {
        return websocket_address;
    }
    public InetSocketAddress getRest_address() {
        return rest_address;
    }
    public List<Role> getRoles() {
        return roles;
    }
    public List<User> getUsers() {
        return users;
    }

    private ViewportConfig(File configPointer, String template) {
        super(configPointer, template);
    }

    /**
     * Create a new config for the proxy, this will delete the old config.
     * @return The newly created config.
     */
    public static ViewportConfig newConfig(File configPointer, String template) {
        config = new ViewportConfig(configPointer, template);
        return ViewportConfig.config();
    }

    private static ViewportConfig config() {
        return config;
    }

    /**
     * Get the current config.
     * @return The config.
     */
    public static ViewportConfig getConfig() {
        return config;
    }

    /**
     * Delete all configs associated with this class.
     */
    public static void empty() {
        config = null;
    }

    @SuppressWarnings("unchecked")
    public void register() throws IllegalStateException {
        this.enabled = this.getNode(this.data, "enabled", Boolean.class);

        this.websocket_address = AddressUtil.parseAddress(
                this.getNode(this.data, "websocket.hostname", String.class) + ":" +
                        this.getNode(this.data, "websocket.port", Integer.class)
        );

        this.rest_address = AddressUtil.parseAddress(
                this.getNode(this.data, "rest.hostname", String.class) + ":" +
                        this.getNode(this.data, "rest.port", Integer.class)
        );

        this.storage = this.getNode(this.data, "storage", String.class);
        if (this.storage.equals("")) throw new IllegalStateException("Please assign a storage method.");
    }
}
