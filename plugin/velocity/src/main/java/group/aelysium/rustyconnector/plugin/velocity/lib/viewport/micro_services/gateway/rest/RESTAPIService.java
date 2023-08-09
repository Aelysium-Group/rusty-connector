package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.rest;

import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.rest.endpoints.LoginEndpoint;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.model.Session;

import java.net.InetSocketAddress;
import java.util.*;

import static spark.Service.ignite;

public class RESTAPIService extends Service {
    private Vector<Session> sessions = new Vector<>();
    private spark.Service spark;

    public RESTAPIService(InetSocketAddress address) {
        this.spark = ignite();
        this.spark.staticFiles.location("/viewport");

        {
            this.spark.post("/login", new LoginEndpoint());
        }

        this.spark.ipAddress(address.getHostName()).port(address.getPort());
        this.spark.init();
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

    public Vector<Session> sessions() {
        return this.sessions;
    }

    @Override
    public void kill() {
        this.spark.stop();
    }
}
