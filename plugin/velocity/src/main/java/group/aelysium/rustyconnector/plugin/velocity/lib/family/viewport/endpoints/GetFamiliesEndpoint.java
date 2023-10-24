package group.aelysium.rustyconnector.plugin.velocity.lib.family.viewport.endpoints;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family.RootFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family.ScalarFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.static_family.StaticFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.rest.APIResponse;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.rest.APIService;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.naming.AuthenticationException;
import java.util.NoSuchElementException;

public class GetFamiliesEndpoint implements Route {
    @Override
    public APIResponse handle(Request request, Response response) {
        APIService api = Tinder.get().services().viewportService().orElseThrow().services().api();
        APIResponse apiResponse = new APIResponse();

        try {
            String requestToken = request.headers("Authorization");
            api.login(requestToken, request.ip());

            JsonArray families = new JsonArray();
            for (BaseFamily family : Tinder.get().services().family().dump()) {
                String type = "unknown";
                if(family instanceof ScalarFamily) type = "scalar";
                if(family instanceof StaticFamily) type = "scalar";

                JsonArray health = new JsonArray(); // To be expanded upon in the future
                health.add(new JsonPrimitive(0));
                health.add(new JsonPrimitive(0));
                health.add(new JsonPrimitive(0));
                health.add(new JsonPrimitive(0));

                JsonObject object = new JsonObject();

                object.add("name",         new JsonPrimitive(family.name()));
                object.add("root",         new JsonPrimitive(family instanceof RootFamily));
                object.add("type",         new JsonPrimitive(type));
                object.add("health",       health);
                object.add("player_count", new JsonPrimitive(family.playerCount()));
                object.add("server_count", new JsonPrimitive(family.registeredServers().size()));

                families.add(object);
            }
            apiResponse.data("families", families);
        } catch (IllegalStateException e) {
            apiResponse.error(500, "An invalid channel id was passed!");
        } catch (NullPointerException e) {
            apiResponse.error(500, "No channel id was passed to connect to!");
        } catch (AuthenticationException ignore) {
            apiResponse.error(500, "Unable to authenticate!");
        } catch (NoSuchElementException ignore) {
            apiResponse.error(500, "Not connected to websocket!");
        }

        response.status(apiResponse.status());
        response.body(apiResponse.toString());
        return apiResponse;
    }
}
