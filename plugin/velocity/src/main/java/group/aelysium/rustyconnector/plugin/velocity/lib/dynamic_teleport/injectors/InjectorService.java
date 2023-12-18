package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.injectors;

import group.aelysium.rustyconnector.plugin.velocity.lib.config.configs.DynamicTeleportConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.anchors.IAnchorService;
import group.aelysium.rustyconnector.toolkit.velocity.family.InitiallyConnectableFamily;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;

public class InjectorService implements IAnchorService<MCLoader, Player, LoadBalancer, InitiallyConnectableFamily<MCLoader, Player, LoadBalancer>> {
    private final Map<String, InitiallyConnectableFamily<MCLoader, Player, LoadBalancer>> injectors;

    protected InjectorService(Map<String, InitiallyConnectableFamily<MCLoader, Player, LoadBalancer>> injectors) {
        this.injectors = injectors;
    }

    public Optional<InitiallyConnectableFamily<MCLoader, Player, LoadBalancer>> familyOf(String anchor) {
        try {
            InitiallyConnectableFamily<MCLoader, Player, LoadBalancer> family = this.injectors.get(anchor);
            if(family == null) return Optional.empty();

            return Optional.of(family);
        } catch (Exception ignore) {}

        return Optional.empty();
    }

    public void create(String name, InitiallyConnectableFamily<MCLoader, Player, LoadBalancer> target) {
        this.injectors.put(name, target);
    }

    public void delete(String name) {
        this.injectors.remove(name);
    }

    public List<String> anchorsFor(InitiallyConnectableFamily<MCLoader, Player, LoadBalancer> target) {
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

            Map<String, InitiallyConnectableFamily<MCLoader, Player, LoadBalancer>> anchors = new HashMap<>();
            for(Map.Entry<String, String> entry : config.getFamilyAnchor_anchors()) {
                InitiallyConnectableFamily<MCLoader, Player, LoadBalancer> family;
                try {
                    Family fetchedFamily = (Family) new Family.Reference(entry.getValue()).get();
                    if(!(fetchedFamily instanceof InitiallyConnectableFamily<?, ?, ?>)) {
                        bootOutput.add(Component.text("The family "+entry.getValue()+" doesn't support family injectors! Ignoring...", NamedTextColor.RED));
                        continue;
                    }

                    family = (InitiallyConnectableFamily<MCLoader, Player, LoadBalancer>) fetchedFamily;
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
