package group.aelysium.rustyconnector.toolkit.velocity.family.versioned_filter;

import com.velocitypowered.api.network.ProtocolVersion;
import group.aelysium.rustyconnector.toolkit.velocity.family.IConnectable;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.family.IInitialEventConnectable;
import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

public interface IVersionedFilter<TMCLoader extends IMCLoader, TPlayer extends IPlayer, TFamily extends IFamily<TMCLoader, TPlayer>> extends IInitialEventConnectable<TMCLoader, TPlayer> {
    /**
     * Registers a family to be associated with the specified version.
     * You can only associate one family with a version at a time.
     * You can map the same server to as many versions as you want.
     * @param version The version to be targeted.
     * @param family The family to associate with the version.
     */
    void register(ProtocolVersion version, TFamily family);

    @Override
    String id();
}
