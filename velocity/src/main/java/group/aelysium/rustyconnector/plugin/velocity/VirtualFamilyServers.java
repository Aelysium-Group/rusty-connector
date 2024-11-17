package group.aelysium.rustyconnector.plugin.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.ara.Closure;
import group.aelysium.ara.Particle;
import group.aelysium.rustyconnector.proxy.family.Family;
import group.aelysium.rustyconnector.proxy.util.AddressUtil;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class VirtualFamilyServers implements Closure {
    protected final ProxyServer server;
    protected final Map<String, Particle.Flux<? extends Family>> mappings = new ConcurrentHashMap<>();

    public VirtualFamilyServers(ProxyServer server) {
        this.server = server;
    }

    public Optional<Particle.Flux<? extends Family>> fetch(String key) {
        return Optional.ofNullable(this.mappings.get(key));
    }

    public boolean createNew(Particle.Flux<? extends Family> flux) {
        AtomicBoolean success = new AtomicBoolean(false);
        flux.executeNow(f->{
            if(this.server.getServer(f.id()).isPresent()) {
                UUID uuid = UUID.randomUUID();
                this.server.registerServer(new ServerInfo(uuid.toString(), AddressUtil.parseAddress("127.0.0.1")));
                this.mappings.put(uuid.toString(), flux);
            } else {
                this.server.registerServer(new ServerInfo(f.id(), AddressUtil.parseAddress("127.0.0.1")));
                this.mappings.put(f.id(), flux);
            }
            success.set(true);
        });
        return success.get();
    }

    public void delete(String key) {
        this.mappings.remove(key);
    }

    @Override
    public void close() throws Exception {
        this.mappings.forEach((k, v)->{
            try {
                RegisteredServer s = this.server.getServer(k).orElseThrow();
                this.server.unregisterServer(s.getServerInfo());
            } catch (Exception ignore) {}
        });
        this.mappings.clear();
    }
}
