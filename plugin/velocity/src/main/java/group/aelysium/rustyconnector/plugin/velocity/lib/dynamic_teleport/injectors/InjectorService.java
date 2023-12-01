package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.injectors;

import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.config.DynamicTeleportConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.anchors.IAnchorService;
import group.aelysium.rustyconnector.toolkit.velocity.family.IInitialEventConnectable;
import group.aelysium.rustyconnector.toolkit.velocity.family.IRootConnectable;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;

public class InjectorService implements IAnchorService<MCLoader, Player, IRootConnectable<MCLoader, Player>> {
    private final Map<String, IRootConnectable<MCLoader, Player>> injectors;

    protected InjectorService(Map<String, IRootConnectable<MCLoader, Player>> injectors) {
        this.injectors = injectors;
    }

    public Optional<IRootConnectable<MCLoader, Player>> familyOf(String anchor) {
        try {
            IRootConnectable<MCLoader, Player> family = this.injectors.get(anchor);
            if(family == null) return Optional.empty();

            return Optional.of(family);
        } catch (Exception ignore) {}

        return Optional.empty();
    }

    public void create(String name, IRootConnectable<MCLoader, Player> target) {
        this.injectors.put(name, target);
    }

    public void delete(String name) {
        this.injectors.remove(name);
    }

    public List<String> anchorsFor(IRootConnectable<MCLoader, Player> target) {
        List<String> anchors = new ArrayList<>();
        this.injectors.entrySet().stream().filter(anchor -> anchor.getValue().equals(target)).forEach(item -> anchors.add(item.getKey()));
        return anchors;
    }

    public void kill() {
        this.injectors.clear();
    }

    public static Optional<InjectorService> init(DependencyInjector.DI1<List<Component>> dependencies, DynamicTeleportConfig config) {
        List<Component> bootOutput = dependencies.d1();

        try {
            if(!config.isFamilyAnchor_enabled()) return Optional.empty();

            Map<String, IRootConnectable<MCLoader, Player>> anchors = new HashMap<>();
            for(Map.Entry<String, String> entry : config.getFamilyAnchor_anchors()) {
                IRootConnectable<MCLoader, Player> family;
                try {
                    Family fetchedFamily = new Family.Reference(entry.getValue()).get();
                    if(!fetchedFamily.metadata().supportsInitialEventConnections()) {
                        bootOutput.add(Component.text("The family "+entry.getValue()+" doesn't support family injectors! Ignoring...", NamedTextColor.RED));
                        continue;
                    }

                    family = (IRootConnectable<MCLoader, Player>) fetchedFamily;
                } catch (Exception ignore) {
                    bootOutput.add(Component.text("The family "+entry.getValue()+" doesn't exist! Ignoring...", NamedTextColor.RED));
                    continue;
                }

                anchors.put(entry.getKey(), family);
            }

            return Optional.of(new InjectorService(anchors));
        } catch (Exception e) {
            bootOutput.add(Component.text("Issue initializing Family Injectors! "+ e.getMessage(), NamedTextColor.RED));
        }

        return Optional.empty();
    }
}
