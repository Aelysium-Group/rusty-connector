package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.hub;

import com.velocitypowered.api.command.CommandManager;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.hub.IHubService;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.hub.commands.CommandHub;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;

public class HubService implements IHubService {
    private final List<String> enabledFamilies;

    public HubService(List<String> enabledFamilies) {
        this.enabledFamilies = enabledFamilies;
    }

    public boolean isEnabled(String familyName) {
        return this.enabledFamilies.contains(familyName);
    }

    public void initCommand(DependencyInjector.DI3<FamilyService, ServerService, List<Component>> dependencies) {
        CommandManager commandManager = Tinder.get().velocityServer().getCommandManager();
        List<Component> bootOutput = dependencies.d3();

        bootOutput.add(Component.text("Building hub service commands...", NamedTextColor.DARK_GRAY));
        if(!commandManager.hasCommand("hub"))
            try {
                commandManager.register(
                        commandManager.metaBuilder("hub").build(),
                        CommandHub.create(DependencyInjector.inject(dependencies.d1(), dependencies.d2(), this))
                );

                bootOutput.add(Component.text(" | Registered: /hub", NamedTextColor.YELLOW));
            } catch (Exception e) {
                e.printStackTrace();
            }

        bootOutput.add(Component.text("Finished building hub service commands.", NamedTextColor.GREEN));
    }

    @Override
    public void kill() {
        CommandManager commandManager = Tinder.get().velocityServer().getCommandManager();
        commandManager.unregister("hub");
    }
}
