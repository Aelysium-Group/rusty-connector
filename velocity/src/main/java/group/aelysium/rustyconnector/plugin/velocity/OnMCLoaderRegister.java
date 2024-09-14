package group.aelysium.rustyconnector.plugin.velocity;

import group.aelysium.rustyconnector.common.events.EventListener;
import group.aelysium.rustyconnector.proxy.events.ServerRegisterEvent;

public class OnMCLoaderRegister {
    @EventListener
    public static void handler(ServerRegisterEvent event) {
        System.out.println("mcloader has registerd! "+event.server());
    }
}
