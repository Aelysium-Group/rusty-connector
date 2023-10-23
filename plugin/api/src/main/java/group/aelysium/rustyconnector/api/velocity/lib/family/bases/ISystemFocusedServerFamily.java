package group.aelysium.rustyconnector.api.velocity.lib.family.bases;

/**
 * This class should never be used directly.
 * System-focused families are not intended to be directly connectable by a player.
 * Instead, the player must invoke some other system feature which will then allow connection to this family via a side effect.
 *
 * These families don't have load balancers, whitelists, or other player-centric features.
 */
public interface ISystemFocusedServerFamily extends IBaseFamily {
}
