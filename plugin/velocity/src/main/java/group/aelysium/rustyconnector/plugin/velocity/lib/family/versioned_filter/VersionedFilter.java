package group.aelysium.rustyconnector.plugin.velocity.lib.family.versioned_filter;

import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.family.IRootConnectable;
import group.aelysium.rustyconnector.toolkit.velocity.family.versioned_filter.IVersionedFilter;

import java.util.HashMap;
import java.util.Map;

public class VersionedFilter implements IVersionedFilter<MCLoader, Player, Family> {
    protected Map<ProtocolVersion, Family> families = new HashMap<>();
    protected String id;

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

    public MCLoader connect(Player player) {
        try {
            Family family = this.families.get(player.resolve().orElseThrow().getProtocolVersion());
            if (family == null) return null;

            family.connect(player);
        } catch (Exception ignore) {}
        return null;
    }

    @Override
    public MCLoader connect(PlayerChooseInitialServerEvent event) {
        try {
            Family family = this.families.get(event.getPlayer().getProtocolVersion());
            if (family == null) return null;

            if(!family.metadata().supportsInitialEventConnections() || !(family instanceof IRootConnectable<?,?>))
                throw new RuntimeException("Attempted to use a Versioned Filter as the root connection even tho it contains families which don't support this! Make sure that if you use a Versioned Filter as your root connection, it ONLY contains scalar families!");

            ((IRootConnectable<MCLoader, Player>) family).connect(event);
        } catch (Exception ignore) {}
        return null;
    }
}
