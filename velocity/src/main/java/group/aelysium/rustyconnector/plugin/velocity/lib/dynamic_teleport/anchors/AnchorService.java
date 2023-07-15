package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors;

import com.velocitypowered.api.command.CommandManager;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.model.Service;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.config.DynamicTeleportConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.DynamicTeleportService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors.commands.CommandAnchor;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.TPAService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.TPASettings;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedServerFamily;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.lang.ref.WeakReference;
import java.util.*;

import static group.aelysium.rustyconnector.plugin.velocity.central.Processor.ValidServices.FAMILY_SERVICE;

public class AnchorService extends Service {
    private Map<String, WeakReference<BaseServerFamily>> anchors;

    private AnchorService(Map<String, WeakReference<BaseServerFamily>> anchors) {
        this.anchors = anchors;
    }

    public void initCommands() {
        CommandManager commandManager = VelocityRustyConnector.getAPI().getServer().getCommandManager();
        VelocityAPI api = VelocityRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();

        this.anchors.forEach((name, family) -> {
            if(commandManager.hasCommand(name)) {
                logger.send(Component.text("Issue initializing Family Anchors! A command called "+name+" already exists! Please find another name for this anchor!", NamedTextColor.RED));
                return;
            }

            try {
                commandManager.register(
                        commandManager.metaBuilder(name).build(),
                        CommandAnchor.create(name)
                );
            } catch (Exception e) {
                logger.send(Component.text("Issue initializing Family Anchors! "+ e.getMessage(), NamedTextColor.RED));
            }
        });
    }

    public Optional<BaseServerFamily> getFamily(String anchor) {
        try {
            BaseServerFamily family = this.anchors.get(anchor).get();
            if(family == null) return Optional.empty();

            return Optional.of(family);
        } catch (Exception ignore) {}

        return Optional.empty();
    }

    public static Optional<AnchorService> init(DynamicTeleportConfig config) {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();
        FamilyService familyService = api.getService(FAMILY_SERVICE).orElseThrow();

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
