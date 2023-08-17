package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.rest;

import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.rest.endpoints.LoginEndpoint;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.rest.endpoints.LogoutEndpoint;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.rest.endpoints.SwitchChannelEndpoint;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.websocket.WebSocketService;

import java.net.InetSocketAddress;
import java.util.*;

import static spark.Service.ignite;

public class APIService extends Service {
    private final WebSocketService websocket;
    private final spark.Service spark;

    public APIService(InetSocketAddress address) {
        this.spark = ignite();
        this.spark.ipAddress(address.getHostName()).port(address.getPort());

        this.websocket = new WebSocketService(this.spark);

        {
            this.spark.post(Endpoints.LOGIN, new LoginEndpoint());

            this.spark.get(Endpoints.LOGOUT, new LogoutEndpoint());
            this.spark.get(Endpoints.SWITCH_WEBSOCKET_CHANNEL, new SwitchChannelEndpoint());
        }
    }

    private static void enableCORS(spark.Service service) {
        service.options("/*", (request, response) -> {

            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        service.before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Request-Methods", "POST, GET");
            response.header("Access-Control-Allow-Headers", "*");
            // Note: this may or may not be necessary in your particular application
            response.type("application/json");
        });
    }

    public WebSocketService websocket() {
        return this.websocket;
    }

    @Override
    public void kill() {
        this.websocket.kill();
        try {
            this.spark.stop();
        } catch (Exception ignore) {}
    }

    public interface Endpoints {
        String LOGIN = "/login";
        String LOGOUT = "/logout";
        String SWITCH_WEBSOCKET_CHANNEL = "/socket/:channel_id";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(LOGIN);
            list.add(LOGOUT);
            list.add(SWITCH_WEBSOCKET_CHANNEL);

            return list;
        }
    }
}
