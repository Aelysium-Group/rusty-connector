package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway;

import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.rest.RESTAPIService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.websocket.WebSocketService;

import java.net.InetSocketAddress;

public class GatewayService extends Service {
    private WebSocketService websocket;
    private RESTAPIService rest;

    public GatewayService(InetSocketAddress websocket, InetSocketAddress rest) {
        this.websocket = new WebSocketService(websocket);
        this.rest = new RESTAPIService(rest);
    }

    public WebSocketService websocket() {
        return this.websocket;
    }

    public RESTAPIService rest() {
        return this.rest;
    }

    @Override
    public void kill() {
        this.websocket.kill();
        this.rest.kill();
    }
}
