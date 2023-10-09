package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import group.aelysium.rustyconnector.core.lib.serviceable.Service;

import java.util.Map;

public class FriendsServiceHandler extends group.aelysium.rustyconnector.core.lib.serviceable.ServiceHandler {
    public FriendsServiceHandler(Map<Class<? extends Service>, Service> services) {
        super(services);
    }
    public FriendsServiceHandler() {
        super();
    }

    public FriendsDataEnclave dataEnclave() {
        return this.find(FriendsDataEnclave.class).orElseThrow();
    }
}