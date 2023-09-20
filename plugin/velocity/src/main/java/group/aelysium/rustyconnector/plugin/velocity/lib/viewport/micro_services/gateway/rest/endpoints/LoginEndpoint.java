package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.rest.endpoints;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.GatewayService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.rest.APIService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.model.APIResponse;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.model.ViewportSession;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.model.SyncedUser;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.naming.AuthenticationException;

public class LoginEndpoint implements Route {
    GatewayService gatewayService = Tinder.get().services().viewportService().orElseThrow().services().gatewayService();

    @Override
    public APIResponse handle(Request request, Response response) {
        Gson gson = new Gson();
        JsonObject object = gson.fromJson(request.body(), JsonObject.class);
        APIResponse apiResponse = new APIResponse();

        try {
            String username = object.get("username").getAsString();
            char[] password = object.get("password").getAsString().toCharArray();

            SyncedUser user = SyncedUser.with(username, password);

            ViewportSession session = ViewportSession.from(user);

            gatewayService.login(session);

            // Build response
            {
                JsonArray endpoints = new JsonArray();
                APIService.Endpoints.toList().forEach(endpoints::add);

                JsonArray channels = new JsonArray();
                APIService.Endpoints.toList().forEach(channels::add);

                apiResponse.data("availableEndpoints", endpoints);
                apiResponse.data("availableChannels", channels);
                apiResponse.data("profile", user.toJSON());
            }
        } catch (AuthenticationException e) {
            apiResponse.error(500, "Your username or password is incorrect!");
        } catch (Exception e) {
            apiResponse.error(500, "Unable to login!");
        }

        response.status(apiResponse.status());
        response.body(apiResponse.toString());
        return apiResponse;
    }
}
