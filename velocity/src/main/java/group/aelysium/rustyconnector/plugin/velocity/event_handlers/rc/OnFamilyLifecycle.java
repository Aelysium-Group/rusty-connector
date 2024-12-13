package group.aelysium.rustyconnector.plugin.velocity.event_handlers.rc;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.common.events.EventListener;
import group.aelysium.rustyconnector.proxy.events.FamilyRegisterEvent;
import group.aelysium.rustyconnector.proxy.events.FamilyUnregisterEvent;
import group.aelysium.rustyconnector.proxy.util.AddressUtil;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

public class OnFamilyLifecycle {
    private final InetSocketAddress dummyAddress = AddressUtil.parseAddress("127.0.0.1:55555");
    protected final ProxyServer proxyServer;
    public OnFamilyLifecycle(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @EventListener
    public void handle(FamilyRegisterEvent event) {
        AtomicReference<ServerInfo> info = new AtomicReference<>(null);
        event.family().onStart(family -> {
            info.set(new ServerInfo(family.id(), dummyAddress));
            this.proxyServer.registerServer(info.get());
            family.metadata("velocity_FamilyProxy", info.get());
        });
        event.family().onClose(()->{
            if(info.get() == null) return;
            this.proxyServer.unregisterServer(info.get());
        });
    }

    @EventListener
    public void handle(FamilyUnregisterEvent event) {
        System.out.println(event.family().id());
        this.proxyServer.unregisterServer(new ServerInfo(event.family().id(), dummyAddress));
    }
}
