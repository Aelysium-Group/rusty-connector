package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors;

import com.velocitypowered.api.command.CommandManager;
import group.aelysium.rustyconnector.api.velocity.lib.serviceable.Service;
import group.aelysium.rustyconnector.api.velocity.lib.util.DependencyInjector;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.config.DynamicTeleportConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors.commands.CommandAnchor;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.lang.ref.WeakReference;
import java.util.*;

public class AnchorService extends Service {
    private Map<String, WeakReference<BaseServerFamily>> anchors;

    private AnchorService(Map<String, WeakReference<BaseServerFamily>> anchors) {
        this.anchors = anchors;
    }

    public void initCommands(DependencyInjector.DI3<DynamicTeleportService, ServerService, List<Component>> dependencies) {
        CommandManager commandManager = Tinder.get().velocityServer().getCommandManager();
        List<Component> bootOutput = dependencies.d3();

        bootOutput.add(Component.text("Building anchor service commands...", NamedTextColor.DARK_GRAY));
        this.anchors.forEach((name, family) -> {
            if(commandManager.hasCommand(name)) {
                bootOutput.add(Component.text("Issue initializing Family Anchors! A command called /"+name+" already exists! Please find another name for this anchor!", NamedTextColor.RED));
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

    public Optional<BaseServerFamily> family(String anchor) {
        try {
            BaseServerFamily family = this.anchors.get(anchor).get();
            if(family == null) return Optional.empty();

            return Optional.of(family);
        } catch (Exception ignore) {}

        return Optional.empty();
    }

    public static Optional<AnchorService> init(DependencyInjector.DI2<List<Component>, FamilyService> dependencies, DynamicTeleportConfig config) {
        Tinder api = Tinder.get();
        List<Component> bootOutput = dependencies.d1();
        FamilyService familyService = dependencies.d2();

        try {
            if(!config.isFamilyAnchor_enabled()) return Optional.empty();

            Map<String, WeakReference<BaseServerFamily>> anchors = new HashMap<>();
            for(Map.Entry<String, String> entry : config.getFamilyAnchor_anchors()) {
                BaseServerFamily family = familyService.find(entry.getValue());
                if(family == null){
                    bootOutput.add(Component.text("The family "+entry.getValue()+" doesn't exist! Ignoring...", NamedTextColor.RED));
                    continue;
                }

                anchors.put(entry.getKey(), new WeakReference<>(family));
            }

            return Optional.of(new AnchorService(anchors));
        } catch (Exception e) {
            bootOutput.add(Component.text("Issue initializing Family Anchors! "+ e.getMessage(), NamedTextColor.RED));
        }

        return Optional.empty();
    }

    @Override
    public void kill() {
        CommandManager commandManager = Tinder.get().velocityServer().getCommandManager();
        this.anchors.forEach((name, family) -> commandManager.unregister(name));

        this.anchors.clear();
    }
}
