package group.aelysium.websocket_bridge.config;

import java.io.File;

public class DefaultConfig extends YAML {
    private static DefaultConfig config;

    private int websocket_port = 3306;
    private String websocket_host = "";

    private char[] websocket_connectionKey;
    private boolean websocket_corsEnabled =  false;

    private DefaultConfig(File configPointer, String template) {
        super(configPointer, template);
    }

    /**
     * Get the current config.
     * @return The config.
     */
    public static DefaultConfig getConfig() {
        return config;
    }

    /**
     * Create a new config for the proxy, this will delete the old config.
     * @return The newly created config.
     */
    public static DefaultConfig newConfig(File configPointer, String template) {
        config = new DefaultConfig(configPointer, template);
        return DefaultConfig.getConfig();
    }
    public String getWebsocket_host() {
        return this.websocket_host;
    }
    public int getWebsocket_port() {
        return this.websocket_port;
    }

    public char[] getWebsocket_connectionKey() {
        return websocket_connectionKey;
    }

    public boolean isWebsocket_corsEnabled() {
        return websocket_corsEnabled;
    }

    @SuppressWarnings("unchecked")
    public void register() throws IllegalStateException {
        this.websocket_host = this.getNode(this.data, "websocket.host", String.class);
        this.websocket_port = this.getNode(this.data, "websocket.port", Integer.class);
        this.websocket_connectionKey = this.getNode(this.data, "websocket.connection-key", String.class).toCharArray();
        this.websocket_corsEnabled = this.getNode(this.data, "websocket.cors-enabled", Boolean.class);
    }
}
