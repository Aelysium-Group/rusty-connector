package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.rest.endpoints;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.core.lib.hash.MD5;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.ViewportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.model.APIResponse;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.model.Session;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.model.User;
import spark.Request;
import spark.Response;
import spark.Route;

import java.time.Instant;
import java.util.ArrayList;

/**
 * Route: /login
 */
public class LoginEndpoint implements Route {
    ViewportService viewportService = Tinder.get().services().viewportService().orElseThrow();

    @Override
    public APIResponse handle(Request request, Response response) throws Exception {
        Gson gson = new Gson();
        JsonObject object = gson.fromJson(request.body(), JsonObject.class);
        APIResponse apiResponse = new APIResponse();

        try {
            String username = object.get("username").getAsString();
            char[] password = object.get("password").getAsString().toCharArray();

            char[] sessionKey = MD5.hash(username + Instant.EPOCH).toCharArray();

            User user = new User(username, password, new ArrayList<>());
            Session session = new Session(sessionKey, user);

            viewportService.services().gatewayService().rest().sessions().add(session);
        } catch (Exception e) {
            apiResponse.error(500, "Unable to login!");
        }

        response.status(apiResponse.status());
        response.body(apiResponse.toString());
        return apiResponse;
    }
}
