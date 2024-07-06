package group.aelysium.rustyconnector.proxy.event_handlers;

import group.aelysium.rustyconnector.proxy.family.mcloader.RankedMCLoader;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.toolkit.common.events.Listener;
import group.aelysium.rustyconnector.toolkit.common.log_gate.GateKey;
import group.aelysium.rustyconnector.toolkit.proxy.events.mc_loader.MCLoaderUnregisterEvent;

public class OnMCLoaderUnregister implements Listener<MCLoaderUnregisterEvent> {
    public void handler(MCLoaderUnregisterEvent event) {
        PluginLogger logger = Tinder.get().logger();

        try {
            RankedMCLoader mcLoader = (RankedMCLoader) event.mcLoader();
            mcLoader.currentSession().orElseThrow().implode("The server that this session was on has closed!");
        } catch (Exception ignore) {}

        // Fire console message
        if (logger.loggerGate().check(GateKey.UNREGISTRATION_ATTEMPT))
            ProxyLang.UNREGISTERED.send(logger, event.mcLoader().uuidOrDisplayName(), event.family().id());
    }
}