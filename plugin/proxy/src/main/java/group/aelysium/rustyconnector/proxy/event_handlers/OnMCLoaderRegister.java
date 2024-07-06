package group.aelysium.rustyconnector.proxy.event_handlers;

import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.toolkit.common.events.Listener;
import group.aelysium.rustyconnector.toolkit.common.log_gate.GateKey;
import group.aelysium.rustyconnector.toolkit.proxy.events.mc_loader.MCLoaderRegisterEvent;

public class OnMCLoaderRegister implements Listener<MCLoaderRegisterEvent> {
    public void handler(MCLoaderRegisterEvent event) {
        PluginLogger logger = Tinder.get().logger();

        // Fire console message
        if(logger.loggerGate().check(GateKey.REGISTRATION_ATTEMPT))
            ProxyLang.REGISTERED.send(logger, event.mcLoader().uuidOrDisplayName(), event.family().id());
    }
}