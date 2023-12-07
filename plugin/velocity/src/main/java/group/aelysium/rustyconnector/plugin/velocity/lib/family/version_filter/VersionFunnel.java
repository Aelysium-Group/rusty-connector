package group.aelysium.rustyconnector.plugin.velocity.lib.family.version_filter;

import com.velocitypowered.api.network.ProtocolVersion;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.toolkit.velocity.family.version_filter.IFamilyCategory;

import java.util.HashMap;
import java.util.Map;

public class VersionFunnel implements IFamilyCategory<Player> {
    protected final String id;
    protected final Map<ProtocolVersion, Family> families = new HashMap<>();

    public VersionFunnel(String id) {
        this.id = id;
    }

    /**
     * Registers a family to be associated with the specified version.
     * You can only associate one family with a version at a time.
     * You can map the same server to as many versions as you want.
     * @param version The version to be targeted.
     * @param family The family to associate with the version.
     */
    public void register(ProtocolVersion version, Family family) {
        this.families.put(version, family);
    }

    @Override
    public String id() {
        return this.id;
    }

    /**
     * Attempts to connect the player to a family based on version.
     * @param rustyPlayer The player to connect.
     * @throws IllegalAccessException If no family exists for the version the player is using.
     */
    @Override
    public void connect(Player rustyPlayer) throws IllegalAccessException {
        com.velocitypowered.api.proxy.Player player = rustyPlayer.resolve().orElseThrow();

        Family family = this.families.get(player.getProtocolVersion());
        if(family == null) throw new IllegalAccessException();

        family.connect(rustyPlayer);
    }
}
