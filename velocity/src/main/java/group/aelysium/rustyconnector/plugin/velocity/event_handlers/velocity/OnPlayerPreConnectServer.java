package group.aelysium.rustyconnector.plugin.velocity.event_handlers.velocity;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.proxy.family.Family;
import group.aelysium.rustyconnector.proxy.family.Server;

import java.util.NoSuchElementException;
import java.util.Optional;

public class OnPlayerPreConnectServer {
    protected final ProxyServer server;

    public OnPlayerPreConnectServer(ProxyServer server) {
        this.server = server;
    }

    @Subscribe(order = PostOrder.CUSTOM, priority = Short.MIN_VALUE)
    public EventTask onPlayerChangeServer(ServerPreConnectEvent event) {
            return EventTask.async(() -> {
                RegisteredServer target = null;
                if(event.getResult().getServer().isPresent()) target = event.getResult().getServer().orElseThrow();
                else target = event.getOriginalServer();

                Family family = null;
                try {
                    family = RC.P.Family(target.getServerInfo().getName()).orElse(null);
                } catch (Exception ignore) {}

                if(family == null) return;

                Optional<Server> optionalServer = family.availableServer();
                if(optionalServer.isEmpty()) {
                    event.setResult(ServerPreConnectEvent.ServerResult.denied());
                    return;
                }
                Server server = optionalServer.orElseThrow();

                RegisteredServer registeredServer = (RegisteredServer) server.metadata("velocity_RegisteredServer")
                        .orElseThrow(()->new NoSuchElementException("The server "+server.id()+" doesn't seem to have a RegisteredServer (from velocity) associated with it."));
                event.setResult(ServerPreConnectEvent.ServerResult.allowed(registeredServer));
            });
    }
}