package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.rest.endpoints;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.api.velocity.util.Version;
import group.aelysium.rustyconnector.core.lib.model.UserPass;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.rest.APIService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.rest.APIResponse;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.naming.AuthenticationException;

public class LoginEndpoint implements Route {
    @Override
    public APIResponse handle(Request request, Response response) {
        APIService api = Tinder.get().services().viewportService().orElseThrow().services().api();
        Gson gson = new Gson();
        JsonObject object = gson.fromJson(request.body(), JsonObject.class);
        APIResponse apiResponse = new APIResponse();

        try {
            Version version = new Version(object.get("version").getAsString());
            Version rcVersion = Tinder.get().flame().version();
            if(version.compareTo(rcVersion) != 0) {
                Tinder.get().logger().log("Incoming Viewport login attempt failed be cause they're on v"+version+" and we're on v"+rcVersion+"!");
                apiResponse.error(500, "Unable to login!");
                return apiResponse;
            }
            String username = object.get("user").getAsString();
            char[] password = object.get("password").getAsString().toCharArray();

            UserPass user = new UserPass(username, password);

            APIService.Session session = api.register(user, request.ip());

            apiResponse.data("live_channel", new JsonPrimitive(api.websocket().websocketEndpoint()));
            apiResponse.data("token", new JsonPrimitive(new String(session.token())));
        } catch (AuthenticationException e) {
            apiResponse.error(500, "Your username or password is incorrect!");
        } catch (Exception e) {
            e.printStackTrace();
            apiResponse.error(500, "Unable to login!");
        }

        response.status(apiResponse.status());
        response.body(apiResponse.toString());
        return apiResponse;
    }
}
