package group.aelysium.rustyconnector.plugin.velocity.lib.family.bases;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.api.velocity.family.bases.IPlayerFocusedFamilyBase;
import group.aelysium.rustyconnector.core.lib.annotations.Initializer;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyService;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class should never be used directly.
 * Player-focused families offer features such as /tpa, whitelists, load-balancing, and direct connection.
 */
public abstract class PlayerFocusedFamily extends BaseFamily implements IPlayerFocusedFamilyBase<PlayerServer> {
    protected String whitelist;

    protected PlayerFocusedFamily(String name, LoadBalancer loadBalancer, String parentName, Whitelist whitelist) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super(name, loadBalancer, parentName);

        if(whitelist == null) this.whitelist = null;
        else this.whitelist = whitelist.name();
    }

    /**
     * Connect a player to this family
     * @param player The player to connect
     * @return A PlayerServer on successful connection.
     * @throws RuntimeException If the connection cannot be made.
     */
    public abstract PlayerServer connect(Player player);
  
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
