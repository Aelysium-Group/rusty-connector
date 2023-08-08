package group.aelysium.rustyconnector.plugin.velocity.lib.family.bases;

import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;

import java.lang.reflect.InvocationTargetException;

/**
 * This class should never be used directly.
 * System-focused families are not intended to be directly connectable by a player.
 * Instead, the player must invoke some other system feature which will then allow connection to this family via a side-effect.
 *
 * These families don't have load balancers, whitelists
 */
public abstract class SystemFocusedServerFamily<S extends PlayerServer> extends BaseServerFamily<S> {
    protected SystemFocusedServerFamily(String name) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super(name);
    }
}
