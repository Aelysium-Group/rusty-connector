package group.aelysium.rustyconnector.plugin.velocity.event_handlers.rc;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.events.EventListener;
import group.aelysium.rustyconnector.proxy.events.ServerTimeoutEvent;
import group.aelysium.rustyconnector.proxy.events.ServerUnregisterEvent;
import net.kyori.adventure.text.Component;

public class OnServerTimeout {
    @EventListener
    public static void handle(ServerTimeoutEvent event) {
        //Component message = RC.Lang("rustyconnector-serverUnregister").generate(event.server());
        //RC.Adapter().log(message);
    }
}
