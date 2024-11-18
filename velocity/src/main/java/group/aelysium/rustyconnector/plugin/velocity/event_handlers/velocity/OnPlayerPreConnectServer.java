package group.aelysium.rustyconnector.plugin.velocity.event_handlers.velocity;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import group.aelysium.ara.Particle;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.plugin.velocity.VirtualFamilyServers;
import group.aelysium.rustyconnector.proxy.family.Family;
import group.aelysium.rustyconnector.proxy.family.Server;
import group.aelysium.rustyconnector.proxy.player.Player;

import java.util.Optional;

public class OnPlayerPreConnectServer {
    protected final ProxyServer server;
    protected final VirtualFamilyServers virtualFamilyServers;

    public OnPlayerPreConnectServer(ProxyServer server, VirtualFamilyServers virtualFamilyServers) {
        this.server = server;
        this.virtualFamilyServers = virtualFamilyServers;
    }

    @Subscribe(order = PostOrder.CUSTOM, priority = Short.MIN_VALUE)
    public EventTask onPlayerChangeServer(ServerPreConnectEvent event) {
            return EventTask.async(() -> {
                RegisteredServer target = null;
                if(event.getResult().getServer().isPresent()) target = event.getResult().getServer().orElseThrow();
                else target = event.getOriginalServer();

                Optional<Particle.Flux<? extends Family>> fluxOptional = this.virtualFamilyServers.fetch(target.getServerInfo().getName());
                if(fluxOptional.isEmpty()) return;

                Particle.Flux<? extends Family> flux = fluxOptional.orElseThrow();
                if(!flux.exists()) {
                    event.setResult(ServerPreConnectEvent.ServerResult.denied());
                    return;
                }

                Family family = flux.orElseThrow();
                Optional<Server> optionalServer = family.availableServer();
                if(optionalServer.isEmpty()) {
                    event.setResult(ServerPreConnectEvent.ServerResult.denied());
                    return;
                }
                Server server = optionalServer.orElseThrow();

                RegisteredServer registeredServer = (RegisteredServer) server.raw();
                event.setResult(ServerPreConnectEvent.ServerResult.allowed(registeredServer));
            });
    }
}