package group.aelysium.rustyconnector.plugin.fabric.events;

import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;

public class OnPlayerPreLogin {

    public static void register() {
        /*ServerLoginConnectionEvents.INIT.register((handler, server) -> {

            if(event.getLoginResult() == AsyncPlayerPreLoginEvent.Result.KICK_FULL) event.allow();
        });*/
    }
}