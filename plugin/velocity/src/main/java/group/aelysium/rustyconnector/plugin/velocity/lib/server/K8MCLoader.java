package group.aelysium.rustyconnector.plugin.velocity.lib.server;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.toolkit.velocity.server.IK8MCLoader;

public class K8MCLoader extends MCLoader implements IK8MCLoader {
    private final String pod;

    public K8MCLoader(String pod, ServerInfo serverInfo, int softPlayerCap, int hardPlayerCap, int weight, int timeout) {
        super(serverInfo, softPlayerCap, hardPlayerCap, weight, timeout);
        this.pod = pod;
    }

    public String podName() {
        return this.pod;
    }

    public static class Reference extends group.aelysium.rustyconnector.toolkit.velocity.util.Reference<K8MCLoader, String> {
        protected String familyName = null;

        public Reference(String podName) {
            super(podName);
        }
        public Reference(String podName, String familyName) {
            super(podName);
            this.familyName = familyName;
        }

        public K8MCLoader get() {
            if(this.familyName != null) return Tinder.get().services().server().fetchPods(this.referencer, this.familyName).orElseThrow();
            return Tinder.get().services().server().fetchPods(this.referencer).orElseThrow();
        }
    }
}
