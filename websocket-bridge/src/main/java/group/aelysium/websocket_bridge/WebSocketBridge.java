package group.aelysium.websocket_bridge;

import group.aelysium.websocket_bridge.config.DefaultConfig;
import group.aelysium.websocket_bridge.websocket.WebSocketService;

import java.io.File;
import java.io.InputStream;
import java.net.InetSocketAddress;

/**
 * Bridge the gap between the particulate control dashboard and Redis!
 */
public class WebSocketBridge {
    private static WebSocketBridge instance;
    public static WebSocketBridge instance() {
        return instance;
    }

    public InputStream getResourceAsStream(String filename)  {
        return getClass().getClassLoader().getResourceAsStream(filename);
    }

    public static String getDataFolder() {
        String currentdir = System.getProperty("user.dir");
        currentdir = currentdir.replace( "\\", "/" );
        return currentdir;
    }

    private WebSocketService websocketService;

    public void start() {
        System.out.println("Loading config.yml...");
        DefaultConfig defaultConfig = DefaultConfig.newConfig(new File(getDataFolder(), "config.yml"), "config_template.yml");
        if(!defaultConfig.generate())
            throw new IllegalStateException("Unable to load or create config.yml!");
        defaultConfig.register();

        System.out.println("Preparing websocket listener...");

        this.websocketService = new WebSocketService(
                new InetSocketAddress(defaultConfig.getWebsocket_host(), defaultConfig.getWebsocket_port()),
                defaultConfig.isWebsocket_corsEnabled(),
                new WebSocketService.SecureConnectorSettings(
                        defaultConfig.isSecureConnector_enabled(),
                        defaultConfig.getSecureConnector_connectionKey(),
                        defaultConfig.getSecureConnector_timeout()
                )
        );
        System.out.println("Finished! Listening to websocket on port: "+defaultConfig.getWebsocket_port());

        System.out.println("Started.");
    }

    public WebSocketService webSocketService() { return this.websocketService; }

    public static void main(String[] args ) {
        WebSocketBridge webSocketBridge = new WebSocketBridge();

        instance = webSocketBridge;

        webSocketBridge.start();


        Runtime.getRuntime().addShutdownHook(new Thread(() -> WebSocketBridge.instance().webSocketService().kill()));
    }
}
