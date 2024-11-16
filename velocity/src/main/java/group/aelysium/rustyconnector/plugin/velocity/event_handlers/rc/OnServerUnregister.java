package group.aelysium.rustyconnector.plugin.velocity.event_handlers.rc;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.events.EventListener;
import group.aelysium.rustyconnector.proxy.events.ServerRegisterEvent;
import group.aelysium.rustyconnector.proxy.events.ServerUnregisterEvent;
import net.kyori.adventure.text.Component;

public class OnServerUnregister {
    @EventListener
    public static void handle(ServerUnregisterEvent event) {
        Component message = RC.Lang("rustyconnector-serverUnregister").generate(event.server());
        RC.Adapter().log(message);
    }
}
