package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors;

import com.velocitypowered.api.command.CommandManager;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.config.DynamicTeleportConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors.commands.CommandAnchor;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.lang.ref.WeakReference;
import java.util.*;

public class AnchorService extends Service {
    private Map<String, WeakReference<BaseServerFamily>> anchors;

    private AnchorService(Map<String, WeakReference<BaseServerFamily>> anchors) {
        this.anchors = anchors;
    }

    public void initCommands() {
        CommandManager commandManager = VelocityAPI.get().velocityServer().getCommandManager();
        VelocityAPI api = VelocityAPI.get();
        PluginLogger logger = api.logger();

        VelocityAPI.get().logger().send(Component.text("Building anchor service commands...", NamedTextColor.DARK_GRAY));
        this.anchors.forEach((name, family) -> {
            if(commandManager.hasCommand(name)) {
                logger.send(Component.text("Issue initializing Family Anchors! A command called /"+name+" already exists! Please find another name for this anchor!", NamedTextColor.RED));
                return;
            }

            try {
                commandManager.register(
                        commandManager.metaBuilder(name).build(),
                        CommandAnchor.create(name)
                );

                VelocityAPI.get().logger().send(Component.text(" | Registered: /"+name, NamedTextColor.YELLOW));
            } catch (Exception e) {
                logger.send(Component.text("Issue initializing Family Anchors! "+ e.getMessage(), NamedTextColor.RED));
            }
        });

        VelocityAPI.get().logger().send(Component.text("Finished building anchor service commands.", NamedTextColor.GREEN));
    }

    public Optional<BaseServerFamily> family(String anchor) {
        try {
            BaseServerFamily family = this.anchors.get(anchor).get();
            if(family == null) return Optional.empty();

            return Optional.of(family);
        } catch (Exception ignore) {}

        return Optional.empty();
    }

    public static Optional<AnchorService> init(DynamicTeleportConfig config) {
        VelocityAPI api = VelocityAPI.get();
        PluginLogger logger = api.logger();
        FamilyService familyService = api.services().familyService();

        try {
            if(!config.isFamilyAnchor_enabled()) return Optional.empty();

            Map<String, WeakReference<BaseServerFamily>> anchors = new HashMap<>();
            for(Map.Entry<String, String> entry : config.getFamilyAnchor_anchors()) {
                BaseServerFamily family = familyService.find(entry.getValue());
                if(family == null){
                    logger.send(Component.text("The family "+entry.getValue()+" doesn't exist! Ignoring...", NamedTextColor.RED));
                    continue;
                }

                anchors.put(entry.getKey(), new WeakReference<>(family));
            }

            return Optional.of(new AnchorService(anchors));
        } catch (Exception e) {
            logger.send(Component.text("Issue initializing Family Anchors! "+ e.getMessage(), NamedTextColor.RED));
        }

        return Optional.empty();
    }

    @Override
    public void kill() {
        this.anchors.clear();
    }
}
