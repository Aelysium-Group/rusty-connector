package group.aelysium.rustyconnector.toolkit.velocity.matchmaking;

import com.google.gson.JsonObject;

public interface IRankResolver {
    /**
     * Resolve the passed JsonObject as a player rank matching the provided schema.
     * @param schemaName The name of the schema to be targeted.
     * @param object The JSON object.
     * @return A Player Rank.
     * @throws IllegalStateException If the object uses a schema not matching `schemaName`.
     */
    IPlayerRank resolve(String schemaName, JsonObject object) throws IllegalStateException;
}
