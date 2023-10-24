package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.websocket;

import group.aelysium.rustyconnector.api.core.serviceable.interfaces.Service;
import org.eclipse.jetty.websocket.api.Session;

import java.util.ArrayList;
import java.util.List;

public class WebSocketGateway extends Service {
    private List<Session> hangingSessions = new ArrayList<>();

    public void hang(Session session) {
        if(this.hangingSessions.contains(session)) return;
        this.hangingSessions.add(session);
    }

    /**
     * Unhangs a {@link Session}. Once a {@link Session} is unhung, it should either be closed and voided, or assigned to a {@link group.aelysium.rustyconnector.plugin.velocity.lib.viewport.rest.APIService.Session ViewportSession}
     * @param session The {@link Session} to unhang.
     * @return `true` if the session existed and was unhung. `false` if the session never existed.
     */
    public boolean unhang(Session session) {
        boolean exists = this.hangingSessions.contains(session);
        this.hangingSessions.remove(session);

        return exists;
    }

    public boolean isHung(Session session) {
        return this.hangingSessions.contains(session);
    }

    @Override
    public void kill() {
        this.hangingSessions.forEach(session -> session.close(503, "Server Closed."));
    }
}
