package group.aelysium.rustyconnector.plugin.velocity.lib.managers;

import group.aelysium.rustyconnector.core.lib.util.logger.Lang;
import group.aelysium.rustyconnector.core.lib.model.NodeManager;
import group.aelysium.rustyconnector.core.lib.util.logger.LangMessage;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.ServerFamily;

import java.security.InvalidAlgorithmParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FamilyManager implements NodeManager<ServerFamily> {
    private final Map<String, ServerFamily> registeredFamilies = new HashMap<>();

    /**
     * Get a family via its name.
     * @param name The name of the family to get.
     * @return A family.
     */
    @Override
    public ServerFamily find(String name) {
        return this.registeredFamilies.get(name);
    }

    /**
     * Add a family to this manager.
     * @param family The family to add to this manager.
     */
    @Override
    public void add(ServerFamily family) {
        this.registeredFamilies.put(family.getName(),family);
    }

    /**
     * Remove a family from this manager.
     * @param family The family to remove from this manager.
     */
    @Override
    public void remove(ServerFamily family) {
        this.registeredFamilies.remove(family.getName());
    }

    @Override
    public List<ServerFamily> dump() {
        return this.registeredFamilies.values().stream().toList();
    }


    /**
     * Print the families and their info to the console.
     */
    public void printFamilies() {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        LangMessage langMessage = (new LangMessage(plugin.logger()))
                .insert(Lang.registeredFamilies())
                .insert(Lang.spacing());

        this.registeredFamilies.forEach((key, family) -> {
            langMessage.insert("   ---| "+family.getName());
        });

        langMessage
                .insert(Lang.spacing())
                .insert("To see more details about a particular family use:")
                .insert("/rc family info <family name>")
                .insert("To see all servers currently saved to a family use:")
                .insert("/rc family info <family name> servers")
                .insert(Lang.spacing())
                .insert(Lang.border())
                .print();
    }
}
