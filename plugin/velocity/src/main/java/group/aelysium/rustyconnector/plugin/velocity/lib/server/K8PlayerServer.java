package group.aelysium.rustyconnector.plugin.velocity.lib.server;

import com.velocitypowered.api.proxy.server.ServerInfo;

public class K8PlayerServer extends PlayerServer {
    private final String pod;

    public K8PlayerServer(String pod, ServerInfo serverInfo, int softPlayerCap, int hardPlayerCap, int weight, int timeout) {
        super(serverInfo, softPlayerCap, hardPlayerCap, weight, timeout);
        this.pod = pod;
    }

    public String podName() {
        return this.pod;
    }
}
