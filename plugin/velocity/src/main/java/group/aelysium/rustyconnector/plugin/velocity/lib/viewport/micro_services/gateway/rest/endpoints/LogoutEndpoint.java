package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.rest.endpoints;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.ViewportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.model.APIResponse;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.model.ViewportSession;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.naming.AuthenticationException;

public class LogoutEndpoint implements Route {
    ViewportService viewportService = VelocityAPI.get().services().viewportService().orElseThrow();

    @Override
    public APIResponse handle(Request request, Response response) throws Exception {
        Gson gson = new Gson();
        JsonObject object = gson.fromJson(request.body(), JsonObject.class);
        APIResponse apiResponse = new APIResponse();

        try {
            char[] requestToken = request.headers("Authorization").toCharArray();
            ViewportSession session = ViewportSession.with(requestToken);

            viewportService.services().gatewayService().logout(session);

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
