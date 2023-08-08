package group.aelysium.rustyconnector.plugin.velocity.lib.players;

import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsDataEnclaveService;

import java.util.Map;

public class PlayerServiceHandler extends group.aelysium.rustyconnector.core.lib.serviceable.ServiceHandler {
    public PlayerServiceHandler(Map<Class<? extends Service>, Service> services) {
        super(services);
    }
    public PlayerServiceHandler() {
        super();
    }

    public PlayerDataEnclaveService dataEnclave() {
        return this.find(PlayerDataEnclaveService.class).orElseThrow();
    }
}