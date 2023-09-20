package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.rest.endpoints;

import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.GatewayService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.websocket.WebSocketService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.model.APIResponse;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.model.ViewportSession;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.naming.AuthenticationException;
import java.util.NoSuchElementException;

public class SwitchChannelEndpoint implements Route {
    GatewayService gatewayService = Tinder.get().services().viewportService().orElseThrow().services().gatewayService();

    @Override
    public APIResponse handle(Request request, Response response) {
        APIResponse apiResponse = new APIResponse();

        try {
            char[] requestToken = request.headers("Authorization").toCharArray();
            ViewportSession session = ViewportSession.with(requestToken);

            String channelId = request.params(":channel_id");
            if(channelId == null) throw new NullPointerException();

            WebSocketService.Events.Mapping mapping = WebSocketService.Events.toList().stream().filter(event -> event.name().equals(channelId)).findFirst().orElse(null);
            if(mapping == null) throw new IllegalStateException();

            gatewayService.websocket().updateListening(session, mapping.clazz());
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
