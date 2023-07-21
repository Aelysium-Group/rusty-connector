package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.hub;

import com.velocitypowered.api.command.CommandManager;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.hub.commands.CommandHub;

import java.util.*;

public class HubService extends Service {
    private List<String> enabledFamilies;

    public HubService(List<String> enabledFamilies) {
        this.enabledFamilies = enabledFamilies;
    }

    public boolean isEnabled(String familyName) {
        return this.enabledFamilies.contains(familyName);
    }

    public void initCommand() {
        CommandManager commandManager = VelocityAPI.get().getServer().getCommandManager();
        if(!commandManager.hasCommand("hub"))
            try {
                commandManager.register(
                        commandManager.metaBuilder("hub").build(),
                        CommandHub.create()
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void kill() {

    }
}
