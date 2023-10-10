package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.rest.endpoints;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.ViewportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.rest.APIService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.rest.APIResponse;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.naming.AuthenticationException;

public class LogoutEndpoint implements Route {
    @Override
    public APIResponse handle(Request request, Response response) throws Exception {
        ViewportService viewportService = Tinder.get().services().viewportService().orElseThrow();
        Gson gson = new Gson();
        JsonObject object = gson.fromJson(request.body(), JsonObject.class);
        APIResponse apiResponse = new APIResponse();

        try {
            APIService.Session session = viewportService.services().api().login(request.headers("Authorization"), request.ip());

            viewportService.services().api().logout(session);

            apiResponse.data("successful", new JsonPrimitive(true));
        } catch (AuthenticationException ignore) {
            apiResponse.data("successful", new JsonPrimitive(true));
        } catch (Exception e) {
            apiResponse.error(500, "Unable to login!");
        }

        response.status(apiResponse.status());
        response.body(apiResponse.toString());
        return apiResponse;
    }
}
