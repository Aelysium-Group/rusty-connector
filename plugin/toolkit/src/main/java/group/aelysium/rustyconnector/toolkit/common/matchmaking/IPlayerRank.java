package group.aelysium.rustyconnector.toolkit.common.matchmaking;

import group.aelysium.rustyconnector.toolkit.common.JSONParseable;

public interface IPlayerRank extends JSONParseable {
    /**
     * Compiles the attributes of this rankholder and returns it's rank.
     */
    double rank();

    /**
     * Returns the string name of the ranking schema.
     */
    String schemaName();
}
