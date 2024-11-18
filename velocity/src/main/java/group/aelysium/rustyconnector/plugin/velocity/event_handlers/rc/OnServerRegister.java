package group.aelysium.rustyconnector.plugin.velocity.event_handlers.rc;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.events.EventListener;
import group.aelysium.rustyconnector.proxy.events.ServerRegisterEvent;
import net.kyori.adventure.text.Component;

public class OnServerRegister {


    @EventListener()
    public static void handle(ServerRegisterEvent event) {
        Component message = RC.Lang("rustyconnector-serverRegister").generate(event.server(), event.family());
        RC.Adapter().log(message);
    }
}
