package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors;

import com.velocitypowered.api.command.CommandManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.anchors.IAnchorService;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.configs.DynamicTeleportConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors.commands.CommandAnchor;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;

public class AnchorService implements IAnchorService {
    private final Map<String, IFamily> anchors;

    protected AnchorService(Map<String, IFamily> anchors) {
        this.anchors = anchors;
    }

    public void initCommands(DependencyInjector.DI3<DynamicTeleportService, ServerService, List<Component>> dependencies) {
        CommandManager commandManager = Tinder.get().velocityServer().getCommandManager();
        List<Component> bootOutput = dependencies.d3();

        bootOutput.add(Component.text("Building anchor service commands...", NamedTextColor.DARK_GRAY));
        this.anchors.forEach((name, family) -> {
            if(commandManager.hasCommand(name)) {
                bootOutput.add(Component.text("Issue initializing Family Anchors! A command called /"+name+" already exists! Please find another id for this anchor!", NamedTextColor.RED));
                return;
            }

            try {
                commandManager.register(
                        commandManager.metaBuilder(name).build(),
                        CommandAnchor.create(DependencyInjector.inject(dependencies.d1(), dependencies.d2(), this), name)
                );

                bootOutput.add(Component.text(" | Registered: /"+name, NamedTextColor.YELLOW));
            } catch (Exception e) {
                bootOutput.add(Component.text("Issue initializing Family Anchors! "+ e.getMessage(), NamedTextColor.RED));
            }
        });

        bootOutput.add(Component.text("Finished building anchor service commands.", NamedTextColor.GREEN));
    }

    public Optional<IFamily> familyOf(String anchor) {
        try {
            IFamily family = this.anchors.get(anchor);
            if(family == null) return Optional.empty();

            return Optional.of(family);
        } catch (Exception ignore) {}

        return Optional.empty();
    }

    public void create(String name, IFamily target) {
        this.anchors.put(name, target);
    }

    public void delete(String name) {
        this.anchors.remove(name);
    }

    public List<String> anchorsFor(IFamily target) {
        List<String> anchors = new ArrayList<>();
        this.anchors.entrySet().stream().filter(anchor -> anchor.getValue().equals(target)).forEach(item -> anchors.add(item.getKey()));
        return anchors;
    }

    public static Optional<AnchorService> init(DependencyInjector.DI2<List<Component>, FamilyService> dependencies, DynamicTeleportConfig config) {
        List<Component> bootOutput = dependencies.d1();
        FamilyService familyService = dependencies.d2();

        try {
            if(!config.isFamilyAnchor_enabled()) return Optional.empty();

            Map<String, IFamily> anchors = new HashMap<>();
            for(Map.Entry<String, String> entry : config.getFamilyAnchor_anchors()) {
                IFamily family;
                try {
                    family = familyService.find(entry.getValue()).orElseThrow();

                    anchors.put(entry.getKey(), family);
                } catch (Exception e) {
                    bootOutput.add(Component.text("The family "+entry.getValue()+" doesn't exist! Ignoring...", NamedTextColor.RED));
                }
            }

            return Optional.of(new AnchorService(anchors));
        } catch (Exception e) {
            bootOutput.add(Component.text("Issue initializing Family Anchors! "+ e.getMessage(), NamedTextColor.RED));
        }

        return Optional.empty();
    }

    public void kill() {
        CommandManager commandManager = Tinder.get().velocityServer().getCommandManager();
        this.anchors.forEach((name, family) -> commandManager.unregister(name));

        this.anchors.clear();
    }
}
