package group.aelysium.rustyconnector.toolkit.velocity.matchmaking;

import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.toolkit.core.matchmaking.IPlayerRank;

public interface IRankResolver {
    /**
     * Resolve the passed JsonObject as a player rank.
     * @param object The JSON object.
     * @return A Player Rank.
     * @throws IllegalStateException If the object uses a schema not matching `schemaName`.
     */
    IPlayerRank resolve(JsonObject object) throws IllegalStateException;
}
