package group.aelysium.rustyconnector.plugin.velocity.lib.server;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.lang_messaging.GateKey;
import group.aelysium.rustyconnector.core.lib.model.ClockService;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.Processor;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.StaticServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.HashMap;
import java.util.Map;

public class ServerLifeMatrixService extends ClockService {
    private final Map<ServerInfo, Boolean> lifeMatrix = new HashMap<>();
    protected final long heartbeat;
    private final boolean shouldUnregister;

    public ServerLifeMatrixService(int threads, long heartbeat, boolean shouldUnregister) {
        super(true, threads);
        this.heartbeat = heartbeat;
        this.shouldUnregister = shouldUnregister;
    }

    public ServerLifeMatrixService() {
        super(false, 0);
        this.heartbeat = 0;
        this.shouldUnregister = false;
    }

    /**
     * Revive a server so that it isn't killed in the next heartbeat
     * @param serverInfo The server to revive.
     */
    public void reviveServer(ServerInfo serverInfo) {
        this.throwIfDisabled();

        if(this.lifeMatrix.get(serverInfo) == null) throw new NullPointerException("This server doesn't exist. Either it never registered or it has already been killed!");

        this.lifeMatrix.put(serverInfo, true);
    }

    /**
     * Register a server into the life matrix so that it will start ticking.
     * @param serverInfo The server to register.
     */
    public void registerServer(ServerInfo serverInfo) {
        this.throwIfDisabled();

        this.lifeMatrix.put(serverInfo, true);
    }


    /**
     * Unregister a server from the life matrix so that it will stop ticking.
     * @param serverInfo The server to unregister.
     */
    public void unregisterServer(ServerInfo serverInfo) {
        this.throwIfDisabled();

        this.lifeMatrix.remove(serverInfo);
    }

    public void startHeartbeat() {
        this.throwIfDisabled();

        VelocityAPI api = VelocityRustyConnector.getAPI();

        for (BaseServerFamily family : api.getService(FamilyService.class).dump()) {
            if (!(family instanceof PlayerFocusedServerFamily)) continue;

            this.scheduleRecurring(() -> {
                PluginLogger logger = api.getLogger();

                if(logger.getGate().check(GateKey.PING))
                    logger.log("Sending out pings and killing dead servers...");

                try {
                    for (Map.Entry<ServerInfo, Boolean> entry : lifeMatrix.entrySet()) {
                        ServerInfo serverInfo = entry.getKey();

                        PlayerServer server = api.getService(ServerService.class).findServer(serverInfo);
                        if(server == null) {
                            logger.log(serverInfo.getName() + " couldn't be found! Ignoring...");
                            continue;
                        }

                        if(!entry.getValue()) {
                            if(shouldUnregister) {
                                api.getService(ServerService.class).unregisterServer(serverInfo, server.getFamilyName(), true);
                                if (logger.getGate().check(GateKey.PING))
                                    logger.log(server.getServerInfo().getName() + " never responded to ping! Killing it...");
                            } else {
                                if (logger.getGate().check(GateKey.PING))
                                    logger.log(server.getServerInfo().getName() + " never responded to ping!");
                            }
                            continue;
                        }

                        lifeMatrix.put(serverInfo,false);

                        server.ping();
                    }
                } catch (Exception e) {
                    logger.log(e.getMessage());
                }
                try {
                    api.getService(FamilyService.class).dump().forEach(currentFamily -> {
                        if(!(currentFamily instanceof StaticServerFamily)) return;

                        try {
                            ((StaticServerFamily) currentFamily).purgeExpiredMappings();
                        } catch (Exception e) {
                            VelocityLang.BOXED_MESSAGE_COLORED.send(logger, Component.text("There was an issue while purging expired mappings for: "+currentFamily.getName()+". "+e.getMessage()), NamedTextColor.RED);
                        }
                    });
                } catch (Exception e) {
                    logger.log(e.getMessage());
                }
            }, this.heartbeat);
        }
    }
}
