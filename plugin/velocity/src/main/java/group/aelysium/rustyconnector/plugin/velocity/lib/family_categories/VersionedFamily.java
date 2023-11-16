package group.aelysium.rustyconnector.plugin.velocity.lib.family_categories;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.RustyPlayer;

import java.util.HashMap;
import java.util.Map;

public class VersionedFamily<TFamily extends BaseFamily> implements FamilyCategory<TFamily> {
    protected Map<ProtocolVersion, TFamily> families = new HashMap<>();

    /**
     * Registers a family to be associated with the specified version.
     * You can only associate one family with a version at a time.
     * You can map the same server to as many versions as you want.
     * @param version The version to be targeted.
     * @param family The family to associate with the version.
     */
    public void register(ProtocolVersion version, TFamily family) {
        this.families.put(version, family);
    }

    /**
     * Attempts to connect the player to a family based on version.
     * @param rustyPlayer The player to connect.
     * @throws IllegalAccessException If no family exists for the version the player is using.
     */
    @Override
    public void connect(RustyPlayer rustyPlayer) throws IllegalAccessException {
        Player player = rustyPlayer.resolve().orElseThrow();

        TFamily family = this.families.get(player.getProtocolVersion());
        if(family == null) throw new IllegalAccessException();

        family.connect(rustyPlayer);
    }
}
