package group.aelysium.rustyconnector.plugin.velocity.lib.family.rounded;

import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;

public class RoundedServerManager {
    protected Vector<RoundedServer> items = new Vector<>();

    public void add(RoundedServer server) {
        if(this.items.contains(server)) return;
        this.items.add(server);
    }

    public void remove(RoundedServer server) {
        this.items.remove(server);
    }

    /**
     * Returns a list of servers which don't have active sessions.
     * If there are no such servers, returns `null`.
     */
    public List<RoundedServer> findAvailable() {
        List<RoundedServer> results = this.items.stream().filter(entry -> entry.getSession() instanceof NullRoundedSession).toList();
        if(results.size() <= 0) return null;
        return results;
    }

    public void forEach(Consumer<? super RoundedServer> action) {
        this.items.forEach(action);
    }

    public List<RoundedServer> dump() {
        return this.items.stream().toList();
    }
}
