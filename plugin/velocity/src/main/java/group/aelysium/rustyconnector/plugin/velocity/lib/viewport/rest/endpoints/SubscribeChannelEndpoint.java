package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.rest.endpoints;

import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.rest.APIService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.websocket.WebSocketService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.rest.APIResponse;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.naming.AuthenticationException;
import java.util.NoSuchElementException;

public class SubscribeChannelEndpoint implements Route {
    @Override
    public APIResponse handle(Request request, Response response) {
        APIService api = Tinder.get().services().viewportService().orElseThrow().services().api();
        APIResponse apiResponse = new APIResponse();

        try {
            APIService.Session session = api.login(request.headers("Authorization"), request.ip());

            String channelId = request.params(":channel_id");
            if(channelId == null) throw new NullPointerException();

            WebSocketService.Events.Mapping mapping = WebSocketService.Events.toList().stream().filter(event -> event.name().equals(channelId)).findFirst().orElse(null);
            if(mapping == null) throw new IllegalStateException();

            session.subscribe(mapping);
            apiResponse.data("success", new JsonPrimitive(true));
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
