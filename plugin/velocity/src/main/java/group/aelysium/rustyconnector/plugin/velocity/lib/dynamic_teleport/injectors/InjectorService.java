package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.injectors;

import group.aelysium.rustyconnector.plugin.velocity.lib.config.configs.DynamicTeleportConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.injectors.IInjectorService;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;

public class InjectorService implements IInjectorService {
    private final Map<String, IFamily> injectors;

    protected InjectorService(Map<String, IFamily> injectors) {
        this.injectors = injectors;
    }

    public Optional<IFamily> familyOf(String host) {
        IFamily family = this.injectors.get(host);
        if(family == null) return Optional.empty();
        return Optional.of(family);
    }

    public void create(String host, IFamily target) {
        this.injectors.put(host, target);
    }

    public void delete(String host) {
        this.injectors.remove(host);
    }

    public List<String> injectorsFor(IFamily target) {
        List<String> injectors = new ArrayList<>();
        this.injectors.entrySet().stream().filter(injector -> injector.getValue().equals(target)).forEach(item -> injectors.add(item.getKey()));
        return injectors;
    }

    public void kill() {
        this.injectors.clear();
    }

    public static Optional<InjectorService> init(DependencyInjector.DI2<List<Component>, FamilyService> dependencies, DynamicTeleportConfig config) {
        List<Component> bootOutput = dependencies.d1();
        FamilyService familyService = dependencies.d2();

        try {
            if(!config.isFamilyInjector_enabled()) return Optional.empty();

            Map<String, IFamily> injectors = new HashMap<>();
            for(Map.Entry<String, String> entry : config.getFamilyInjector_injectors()) {
                IFamily family;
                try {
                    family = familyService.find(entry.getValue()).orElseThrow();
                } catch (Exception ignore) {
                    bootOutput.add(Component.text("The family "+entry.getValue()+" doesn't exist! Ignoring...", NamedTextColor.RED));
                    continue;
                }

                injectors.put(entry.getKey(), family);
            }

            return Optional.of(new InjectorService(injectors));
        } catch (Exception e) {
            bootOutput.add(Component.text("Issue initializing Family Injectors! "+ e.getMessage(), NamedTextColor.RED));
        }

        return Optional.empty();
    }
}
