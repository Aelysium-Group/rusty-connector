package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage;

import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IRankResolver;

public class DefaultRankResolver implements IRankResolver {
    @Override
    public IPlayerRank resolve(String schemaName, JsonObject object) throws IllegalStateException {
        String schema = object.get("schema").getAsString();

        if(!schema.equals(schemaName)) throw new IllegalStateException("The passed rank object uses schema "+schema+", when it was expected to use "+schemaName);

        return switch (schemaName) {
            case "RANDOMIZE" -> RandomizedPlayerRank.New();
            case "WIN_LOSS" -> {
                int wins = object.get("wins").getAsInt();
                int losses = object.get("losses").getAsInt();
                yield new WinLossPlayerRank(wins, losses);
            }
            case "WIN_RATE" -> {
                int wins = object.get("wins").getAsInt();
                int losses = object.get("losses").getAsInt();
                yield new WinRatePlayerRank(wins, losses);
            }
            default -> throw new IllegalStateException("The passed rank object uses schema "+schema+" which doesn't match a supported schema!");
        };
    }
}
