package group.aelysium.rustyconnector.toolkit.velocity.whitelist;

import group.aelysium.rustyconnector.toolkit.RustyConnector;
import group.aelysium.rustyconnector.toolkit.velocity.central.VelocityTinder;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;

import java.util.List;

public interface IWhitelist {
    /**
     * Does this {@link IWhitelist} specifically validate players based of their connection details?
     * @return {@link Boolean}
     */
    boolean usesPlayers();
    /**
     * Does this {@link IWhitelist} specifically validate players based on their possession of a permission?
     * @return {@link Boolean}
     */
    boolean usesPermission();

    /**
     * Gets the name of this {@link IWhitelist}.
     * @return {@link String}
     */
    String name();

    /**
     * Gets the message that'll be sent to players that don't match this {@link IWhitelist}.
     * @return {@link String}
     */
    String message();

    /**
     * Checks if this {@link IWhitelist} is inverted.
     * An inverted {@link IWhitelist} will behave as a denylist. Where players that meet the criteria that are defined, will be blocked.
     * @return {@link Boolean}
     */
    boolean inverted();

    /**
     * Fetches a list of player filters.
     * @return {@link List<IWhitelistPlayerFilter>}
     */
    List<? extends IWhitelistPlayerFilter> playerFilters();

    /**
     * Validate a player against the {@link IWhitelist}.
     * @param player The {@link IPlayer} to validate.
     * @return `true` if the player is whitelisted. `false` otherwise.
     */
    boolean validate(IPlayer player);


    class Reference extends group.aelysium.rustyconnector.toolkit.velocity.util.Reference<IWhitelist, String> {
        public Reference(String name) {
            super(name);
        }

        public <TWhitelist extends IWhitelist> TWhitelist get() {
            VelocityTinder tinder = RustyConnector.Toolkit.proxy().orElseThrow();
            return (TWhitelist) tinder.services().whitelist().find(this.referencer).orElseThrow();
        }
    }
}
