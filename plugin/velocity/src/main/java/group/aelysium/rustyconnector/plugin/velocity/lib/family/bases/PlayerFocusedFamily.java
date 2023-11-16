package group.aelysium.rustyconnector.plugin.velocity.lib.family.bases;

import group.aelysium.rustyconnector.plugin.velocity.lib.players.RustyPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.family.bases.IPlayerFocusedFamilyBase;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;

import java.lang.reflect.InvocationTargetException;

/**
 * This class should never be used directly.
 * Player-focused families offer features such as /tpa, whitelists, load-balancing, and direct connection.
 */
public abstract class PlayerFocusedFamily extends BaseFamily implements IPlayerFocusedFamilyBase<PlayerServer, RustyPlayer> {
    protected String whitelist;

    protected PlayerFocusedFamily(String name, LoadBalancer loadBalancer, String parentName, Whitelist whitelist) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super(name, loadBalancer, parentName);

        if(whitelist == null) this.whitelist = null;
        else this.whitelist = whitelist.name();
    }
  
    /**
     * Get the whitelist for this family, or `null` if there isn't one.
     * @return The whitelist or `null` if there isn't one.
     */
    public Whitelist whitelist() {
        Tinder api = Tinder.get();
        if(this.name == null) return null;
        return api.services().whitelist().find(this.whitelist);
    }
}
