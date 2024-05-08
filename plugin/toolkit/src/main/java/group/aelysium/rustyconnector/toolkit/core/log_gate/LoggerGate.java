package group.aelysium.rustyconnector.toolkit.core.log_gate;

import java.util.HashMap;
import java.util.Map;

public class LoggerGate {
    private final Map<GateKey, Boolean> nodes = new HashMap<>();

    public void registerNode(GateKey key, Boolean value) {
        this.nodes.put(key, value);
    }

    /**
     * Check if a gateway is open.
     * @param key The key to this gateway.
     * @return `true` if the key works. Otherwise, `false`
     */
    public boolean check(GateKey key) {
        if(key == null) return false;

        Boolean response = this.nodes.get(key);
        if(response == null) return false;

        return response;
    }
}
