package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport;

import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.injectors.InjectorService;
import group.aelysium.rustyconnector.toolkit.core.serviceable.ServiceableService;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa.TPAServiceSettings;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.configs.DynamicTeleportConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors.AnchorService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.hub.HubService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.TPAService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector.inject;

public class DynamicTeleportService extends ServiceableService<DynamicTeleportServiceHandler> {
    protected DynamicTeleportService(Map<Class<? extends Service>, Service> services) {
        super(new DynamicTeleportServiceHandler(services));
    }

    public static DynamicTeleportService init(DependencyInjector.DI2<List<Component>, FamilyService> dependencies, DynamicTeleportConfig config) {
        List<Component> bootOutput = dependencies.d1();

        if(!config.isEnabled()) return new DynamicTeleportService(new HashMap<>());

        try {
            DynamicTeleportService.Builder builder = new DynamicTeleportService.Builder();

            if(config.isTpa_enabled()) {
                TPAServiceSettings tpaSettings = new TPAServiceSettings(
                        config.isTpa_friendsOnly(),
                        config.isTpa_ignorePlayerCap(),
                        config.getTpa_expiration(),
                        config.getTpa_enabledFamilies()
                );

                TPAService tpaService = new TPAService(tpaSettings);

                builder.addService(tpaService);
                bootOutput.add(Component.text(" | The TPA module was enabled!", NamedTextColor.GREEN));
            } else
                bootOutput.add(Component.text(" | The TPA module wasn't enabled.",NamedTextColor.DARK_GRAY));

            if(config.isHub_enabled()) {
                try {
                    builder.addService(new HubService(config.getHub_enabledFamilies()));
                    bootOutput.add(Component.text(" | The Hub module was enabled!",NamedTextColor.GREEN));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else
                bootOutput.add(Component.text(" | The Hub module wasn't enabled.",NamedTextColor.DARK_GRAY));

            if(config.isFamilyAnchor_enabled()) {
                try {
                    builder.addService(AnchorService.init(inject(bootOutput, dependencies.d2()), config).orElseThrow());
                    bootOutput.add(Component.text(" | The Anchor module was enabled!",NamedTextColor.GREEN));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else
                bootOutput.add(Component.text(" | The Anchor module wasn't enabled.",NamedTextColor.DARK_GRAY));

            if(config.isFamilyInjector_enabled()) {
                try {
                    builder.addService(InjectorService.init(inject(bootOutput, dependencies.d2()), config).orElseThrow());
                    bootOutput.add(Component.text(" | The Injector module was enabled!",NamedTextColor.GREEN));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else
                bootOutput.add(Component.text(" | The Injector module wasn't enabled.",NamedTextColor.DARK_GRAY));

            return builder.build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new DynamicTeleportService(new HashMap<>());
    }

    protected static class Builder {
        protected final Map<Class<? extends Service>, Service> services = new HashMap<>();

        public DynamicTeleportService.Builder addService(Service service) {
            this.services.put(service.getClass(), service);
            return this;
        }

        public DynamicTeleportService build() {
            return new DynamicTeleportService(this.services);
        }
    }
}