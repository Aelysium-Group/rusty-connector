package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnection;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.core.lib.packets.PacketOrigin;
import group.aelysium.rustyconnector.core.lib.packets.PacketType;
import group.aelysium.rustyconnector.core.lib.packets.variants.CoordinateRequestQueuePacket;
import group.aelysium.rustyconnector.core.lib.serviceable.ServiceableService;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.commands.CommandTPA;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;

public class TPAService extends ServiceableService<TPAServiceHandler> {
    private TPASettings settings;
    private Map<BaseServerFamily, TPAHandler> tpaHandlers = Collections.synchronizedMap(new WeakHashMap<>());

    public TPAService(TPASettings settings) {
        super(new TPAServiceHandler());
        this.services.add(new TPACleaningService(settings.expiration()));
        this.settings = settings;
    }
    public void initCommand() {
        CommandManager commandManager = Tinder.get().velocityServer().getCommandManager();
        Tinder.get().logger().send(Component.text("Building tpa service commands...", NamedTextColor.DARK_GRAY));

        if(!commandManager.hasCommand("tpa"))
            try {
                commandManager.register(
                        commandManager.metaBuilder("tpa").build(),
                        CommandTPA.create()
                );

                Tinder.get().logger().send(Component.text(" | Registered: /tpa", NamedTextColor.YELLOW));
            } catch (Exception e) {
                e.printStackTrace();
            }

        Tinder.get().logger().send(Component.text("Finished building tpa service commands.", NamedTextColor.GREEN));
    }

    public TPASettings settings() {
        return this.settings;
    }

    public TPAHandler tpaHandler(BaseServerFamily family) {
        TPAHandler tpaHandler = this.tpaHandlers.get(family);
        if(tpaHandler == null) {
            TPAHandler newTPAHandler = new TPAHandler();
            this.tpaHandlers.put(family, newTPAHandler);
            return newTPAHandler;
        }

        return tpaHandler;
    }
    public List<TPAHandler> allTPAHandlers() {
        return this.tpaHandlers.values().stream().toList();
    }

    /**
     * Attempts to directly connect a player to a server and then teleport that player to another player.
     * @param source The player requesting to tpa.
     * @param target The player to tpa to.
     * @param targetServer The server to send the player to.
     * @throws NullPointerException If the server doesn't exist in the family.
     */
    public void tpaSendPlayer(Player source, Player target, PlayerServer targetServer) {
        Tinder api = Tinder.get();

        CoordinateRequestQueuePacket message = (CoordinateRequestQueuePacket) new GenericPacket.Builder()
                .setType(PacketType.COORDINATE_REQUEST_QUEUE)
                .setOrigin(PacketOrigin.PROXY)
                .setAddress(targetServer.address())
                .setParameter(CoordinateRequestQueuePacket.ValidParameters.TARGET_SERVER, targetServer.address())
                .setParameter(CoordinateRequestQueuePacket.ValidParameters.TARGET_USERNAME, target.getUsername())
                .setParameter(CoordinateRequestQueuePacket.ValidParameters.SOURCE_USERNAME, source.getUsername())
                .buildSendable();

        MessengerConnection<?> backboneMessenger = api.flame().backbone().connection().orElseThrow();
        backboneMessenger.publish(message);

        try {
            PlayerServer senderServer = api.services().serverService().search(source.getCurrentServer().orElseThrow().getServerInfo());

            if (senderServer.equals(targetServer)) return;
        } catch (Exception ignore) {}

        try {
            targetServer.connect(source);
        } catch (Exception e) {
            source.sendMessage(VelocityLang.TPA_FAILURE.build(target.getUsername()));
        }
    }

    @Override
    public void kill() {
        this.allTPAHandlers().forEach(TPAHandler::decompose);
        this.tpaHandlers.clear();
        super.kill();
    }
}
