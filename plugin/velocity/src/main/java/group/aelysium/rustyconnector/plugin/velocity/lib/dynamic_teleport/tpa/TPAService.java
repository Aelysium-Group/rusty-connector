package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.api.velocity.dynamic_teleport.tpa.ITPAService;
import group.aelysium.rustyconnector.api.velocity.dynamic_teleport.tpa.TPAServiceSettings;
import group.aelysium.rustyconnector.core.lib.messenger.MessengerConnection;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.core.lib.packets.PacketOrigin;
import group.aelysium.rustyconnector.core.lib.packets.PacketType;
import group.aelysium.rustyconnector.core.lib.packets.variants.CoordinateRequestQueuePacket;
import group.aelysium.rustyconnector.api.velocity.util.DependencyInjector;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.commands.CommandTPA;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;

public class TPAService implements ITPAService<TPACleaningService, PlayerServer, PlayerFocusedFamily, TPARequest, TPAHandler> {
    private final TPACleaningService cleaningService;
    private final TPAServiceSettings settings;
    private final Map<BaseFamily, TPAHandler> tpaHandlers = Collections.synchronizedMap(new WeakHashMap<>());

    public TPAService(TPAServiceSettings settings) {
        this.cleaningService = new TPACleaningService(settings.expiration());
        this.settings = settings;
    }
    public void initCommand(DependencyInjector.DI3<FamilyService, ServerService, List<Component>> dependencies) {
        CommandManager commandManager = Tinder.get().velocityServer().getCommandManager();
        List<Component> bootOutput = dependencies.d3();

        bootOutput.add(Component.text("Building tpa service commands...", NamedTextColor.DARK_GRAY));

        if(!commandManager.hasCommand("tpa"))
            try {
                commandManager.register(
                        commandManager.metaBuilder("tpa").build(),
                        CommandTPA.create(DependencyInjector.inject(dependencies.d1(), dependencies.d2(), this))
                );

                bootOutput.add(Component.text(" | Registered: /tpa", NamedTextColor.YELLOW));
            } catch (Exception e) {
                e.printStackTrace();
            }

        bootOutput.add(Component.text("Finished building tpa service commands.", NamedTextColor.GREEN));
    }

    public TPAServiceSettings settings() {
        return this.settings;
    }
    public TPACleaningService cleaner() {
        return this.cleaningService;
    }

    public TPAHandler tpaHandler(PlayerFocusedFamily family) {
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

        MessengerConnection backboneMessenger = api.flame().backbone().connection().orElseThrow();
        backboneMessenger.publish(message);

        try {
            PlayerServer senderServer = api.services().server().search(source.getCurrentServer().orElseThrow().getServerInfo());

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
        this.cleaningService.kill();

        CommandManager commandManager = Tinder.get().velocityServer().getCommandManager();
        commandManager.unregister("tpa");
    }
}
