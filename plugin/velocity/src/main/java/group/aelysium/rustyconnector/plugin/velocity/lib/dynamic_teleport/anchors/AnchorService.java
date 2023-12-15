package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors;

import com.velocitypowered.api.command.CommandManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.anchors.IAnchorService;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.config.DynamicTeleportConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors.commands.CommandAnchor;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;

public class AnchorService implements IAnchorService<MCLoader, Player, LoadBalancer, Family> {
    private final Map<String, Family> anchors;

    protected AnchorService(Map<String, Family> anchors) {
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

    public Optional<Family> familyOf(String anchor) {
        try {
            Family family = this.anchors.get(anchor);
            if(family == null) return Optional.empty();

            return Optional.of(family);
        } catch (Exception ignore) {}

        return Optional.empty();
    }

    public void create(String name, Family target) {
        this.anchors.put(name, target);
    }

    public void delete(String name) {
        this.anchors.remove(name);
    }

    public List<String> anchorsFor(Family target) {
        List<String> anchors = new ArrayList<>();
        this.anchors.entrySet().stream().filter(anchor -> anchor.getValue().equals(target)).forEach(item -> anchors.add(item.getKey()));
        return anchors;
    }

    public static Optional<AnchorService> init(DependencyInjector.DI1<List<Component>> dependencies, DynamicTeleportConfig config) {
        List<Component> bootOutput = dependencies.d1();

        try {
            if(!config.isFamilyAnchor_enabled()) return Optional.empty();

            Map<String, Family> anchors = new HashMap<>();
            for(Map.Entry<String, String> entry : config.getFamilyAnchor_anchors()) {
                Family family;
                try {
                    family = (Family) new Family.Reference(entry.getValue()).get();
                } catch (Exception ignore) {
                    bootOutput.add(Component.text("The family "+entry.getValue()+" doesn't exist! Ignoring...", NamedTextColor.RED));
                    continue;
                }

                anchors.put(entry.getKey(), family);
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
