package group.aelysium.rustyconnector.plugin.velocity.lib.family.viewport.endpoints;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.rest.APIResponse;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.rest.APIService;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.naming.AuthenticationException;
import java.util.NoSuchElementException;

public class GetFamilyEndpoint implements Route {
    @Override
    public APIResponse handle(Request request, Response response) {
        APIService api = Tinder.get().services().viewportService().orElseThrow().services().api();
        APIResponse apiResponse = new APIResponse();

        try {
            api.login(request.headers("Authorization"), request.ip());

            String familyName = request.params(":family_name");
            if(familyName == null) throw new NullPointerException();

            JsonArray servers = new JsonArray();
            Tinder.get().services().familyService().find(familyName).registeredServers().forEach(server -> {
                JsonObject object = new JsonObject();
                object.add("name", new JsonPrimitive(server.serverInfo().getName()));
                object.add("id", new JsonPrimitive(server.id().toString()));
                object.add("player_count", new JsonPrimitive(server.playerCount()));
                servers.add(object);
            });

            apiResponse.data("servers", servers);
        } catch (IllegalStateException e) {
            apiResponse.error(500, "An invalid family id was passed!");
        } catch (NullPointerException e) {
            apiResponse.error(500, "No family id was passed to connect to!");
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
