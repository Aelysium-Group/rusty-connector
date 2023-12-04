package group.aelysium.rustyconnector.toolkit.velocity.family.version_filter;

import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayer;

/**
 * A family category is a collection of other families.
 * Categories are only ever allowed to include the same type of family.
 */
public interface IFamilyCategory<TPlayer extends IPlayer> {
    String id();

    /**
     * Connects a player to one of the families in this category.
     * The method by which a family is chosen is strictly reliant on the type of category used.
     * @param player The player to connect.
     */
    void connect(TPlayer player) throws Exception;
}
