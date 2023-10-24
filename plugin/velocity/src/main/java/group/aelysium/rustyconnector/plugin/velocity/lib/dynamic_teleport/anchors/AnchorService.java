package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors;

import com.velocitypowered.api.command.CommandManager;
import group.aelysium.rustyconnector.api.velocity.dynamic_teleport.anchors.IAnchorService;
import group.aelysium.rustyconnector.api.velocity.util.DependencyInjector;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.config.DynamicTeleportConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors.commands.CommandAnchor;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.lang.ref.WeakReference;
import java.util.*;

public class AnchorService implements IAnchorService<PlayerServer, PlayerFocusedFamily> {
    private final Map<String, PlayerFocusedFamily> anchors;

    private AnchorService(Map<String, PlayerFocusedFamily> anchors) {
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

    public Optional<PlayerFocusedFamily> familyOf(String anchor) {
        try {
            PlayerFocusedFamily family = this.anchors.get(anchor);
            if(family == null) return Optional.empty();

            return Optional.of(family);
        } catch (Exception ignore) {}

        return Optional.empty();
    }

    public void create(String name, PlayerFocusedFamily target) {
        this.anchors.put(name, target);
    }

    public void delete(String name) {
        this.anchors.remove(name);
    }

    public List<String> anchorsFor(PlayerFocusedFamily target) {
        List<String> anchors = new ArrayList<>();
        this.anchors.entrySet().stream().filter(anchor -> anchor.getValue().equals(target)).forEach(item -> anchors.add(item.getKey()));
        return anchors;
    }

    public static Optional<AnchorService> init(DependencyInjector.DI2<List<Component>, FamilyService> dependencies, DynamicTeleportConfig config) {
        Tinder api = Tinder.get();
        List<Component> bootOutput = dependencies.d1();
        FamilyService familyService = dependencies.d2();

        try {
            if(!config.isFamilyAnchor_enabled()) return Optional.empty();

            Map<String, PlayerFocusedFamily> anchors = new HashMap<>();
            for(Map.Entry<String, String> entry : config.getFamilyAnchor_anchors()) {
                BaseFamily family = familyService.find(entry.getValue());
                if(family == null){
                    bootOutput.add(Component.text("The family "+entry.getValue()+" doesn't exist! Ignoring...", NamedTextColor.RED));
                    continue;
                }
                if(!(family instanceof PlayerFocusedFamily)){
                    bootOutput.add(Component.text("The family "+entry.getValue()+" doesn't respect players! Ignoring...", NamedTextColor.RED));
                    continue;
                }

                anchors.put(entry.getKey(), (PlayerFocusedFamily) family);
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
