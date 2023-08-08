package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors.AnchorService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.hub.HubService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.TPAService;

import java.util.Map;
import java.util.Optional;

public class FriendsServiceHandler extends group.aelysium.rustyconnector.core.lib.serviceable.ServiceHandler {
    public FriendsServiceHandler(Map<Class<? extends Service>, Service> services) {
        super(services);
    }
    public FriendsServiceHandler() {
        super();
    }

    public FriendsDataEnclaveService dataEnclave() {
        return this.find(FriendsDataEnclaveService.class).orElseThrow();
    }
}