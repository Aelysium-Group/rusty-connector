package group.aelysium.websocket_bridge.config;

import group.aelysium.websocket_bridge.LiquidTimestamp;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class DefaultConfig extends YAML {
    private static DefaultConfig config;

    private int websocket_port = 3306;
    private String websocket_host = "";
    private boolean websocket_corsEnabled =  false;

    private boolean secureConnector_enabled;
    private char[] secureConnector_connectionKey;
    private LiquidTimestamp secureConnector_timeout;

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
    public boolean isWebsocket_corsEnabled() {
        return websocket_corsEnabled;
    }
    public boolean isSecureConnector_enabled() {
        return secureConnector_enabled;
    }
    public char[] getSecureConnector_connectionKey() {
        return secureConnector_connectionKey;
    }
    public LiquidTimestamp getSecureConnector_timeout() {
        return secureConnector_timeout;
    }

    @SuppressWarnings("unchecked")
    public void register() throws IllegalStateException {
        this.websocket_host = this.getNode(this.data, "websocket.host", String.class);
        this.websocket_port = this.getNode(this.data, "websocket.port", Integer.class);
        this.websocket_corsEnabled = this.getNode(this.data, "websocket.cors-enabled", Boolean.class);

        this.secureConnector_enabled = this.getNode(this.data, "secure-connector.enabled", Boolean.class);
        this.secureConnector_connectionKey = this.getNode(this.data, "secure-connector.connection-key", String.class).toCharArray();
        try {
            this.secureConnector_timeout = new LiquidTimestamp(this.getNode(this.data, "secure-connector.timeout", String.class));
        } catch (Exception e) {
            System.out.println("Unable to parse LiquidTimestamp! Setting to default of 5 Seconds.");

            this.secureConnector_timeout = new LiquidTimestamp(5, TimeUnit.SECONDS);
        }
    }
}
