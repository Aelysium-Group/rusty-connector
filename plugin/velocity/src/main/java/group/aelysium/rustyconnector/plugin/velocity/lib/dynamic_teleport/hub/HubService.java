package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.hub;

import com.velocitypowered.api.command.CommandManager;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.core.lib.util.DependencyInjector;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.hub.commands.CommandHub;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;

public class HubService extends Service {
    private List<String> enabledFamilies;

    public HubService(List<String> enabledFamilies) {
        this.enabledFamilies = enabledFamilies;
    }

    public boolean isEnabled(String familyName) {
        return this.enabledFamilies.contains(familyName);
    }

    public void initCommand(DependencyInjector.DI2<FamilyService, ServerService> dependencies) {
        CommandManager commandManager = Tinder.get().velocityServer().getCommandManager();

        Tinder.get().logger().send(Component.text("Building hub service commands...", NamedTextColor.DARK_GRAY));
        if(!commandManager.hasCommand("hub"))
            try {
                commandManager.register(
                        commandManager.metaBuilder("hub").build(),
                        CommandHub.create(DependencyInjector.inject(dependencies.d1(), dependencies.d2(), this))
                );

                Tinder.get().logger().send(Component.text(" | Registered: /hub", NamedTextColor.YELLOW));
            } catch (Exception e) {
                e.printStackTrace();
            }

        Tinder.get().logger().send(Component.text("Finished building hub service commands.", NamedTextColor.GREEN));
    }

    @Override
    public void kill() {
        CommandManager commandManager = Tinder.get().velocityServer().getCommandManager();
        commandManager.unregister("hub");
    }
}
