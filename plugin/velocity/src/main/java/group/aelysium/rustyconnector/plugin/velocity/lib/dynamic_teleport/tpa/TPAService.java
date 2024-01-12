package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa;

import com.velocitypowered.api.command.CommandManager;
import group.aelysium.rustyconnector.core.lib.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.toolkit.core.packet.Packet;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa.ITPACleaningService;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa.ITPAHandler;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa.ITPAService;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa.TPAServiceSettings;
import group.aelysium.rustyconnector.core.lib.packets.QueueTPAPacket;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.commands.CommandTPA;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;

public class TPAService implements ITPAService {
    private final TPACleaningService cleaningService;
    private final TPAServiceSettings settings;
    private final Map<IFamily, ITPAHandler> tpaHandlers = Collections.synchronizedMap(new WeakHashMap<>());

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
    public ITPACleaningService cleaner() {
        return this.cleaningService;
    }

    public ITPAHandler tpaHandler(IFamily family) {
        ITPAHandler tpaHandler = this.tpaHandlers.get(family);
        if(tpaHandler == null) {
            TPAHandler newTPAHandler = new TPAHandler();
            this.tpaHandlers.put(family, newTPAHandler);
            return newTPAHandler;
        }

        return tpaHandler;
    }
    public List<ITPAHandler> allTPAHandlers() {
        return this.tpaHandlers.values().stream().toList();
    }

    /**
     * Attempts to directly connect a player to a server and then teleport that player to another player.
     * @param source The player requesting to tpa.
     * @param target The player to tpa to.
     * @param targetServer The server to send the player to.
     * @throws NullPointerException If the server doesn't exist in the family.
     */
    public void tpaSendPlayer(IPlayer source, IPlayer target, IMCLoader targetServer) {
        Tinder api = Tinder.get();

        Packet message = api.services().packetBuilder().newBuilder()
                .identification(BuiltInIdentifications.QUEUE_TPA)
                .sendingToMCLoader(targetServer.uuid())
                .parameter(QueueTPAPacket.Parameters.TARGET_USERNAME, target.username())
                .parameter(QueueTPAPacket.Parameters.SOURCE_USERNAME, source.username())
                .build();

        api.services().magicLink().connection().orElseThrow().publish(message);

        try {
            if (source.server().orElseThrow().equals(targetServer)) return;
        } catch (Exception ignore) {}

        try {
            targetServer.connect(source);
        } catch (Exception e) {
            source.sendMessage(ProxyLang.TPA_FAILURE.build(target.username()));
        }
    }

    public void kill() {
        this.allTPAHandlers().forEach(ITPAHandler::decompose);
        this.tpaHandlers.clear();
        this.cleaningService.kill();

        CommandManager commandManager = Tinder.get().velocityServer().getCommandManager();
        commandManager.unregister("tpa");
    }
}
