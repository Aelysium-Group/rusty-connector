package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.rank;

import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IRankResolver;

public class DefaultRankResolver implements IRankResolver {
    protected static final DefaultRankResolver singleton = new DefaultRankResolver();
    protected DefaultRankResolver() {}

    @Override
    public IPlayerRank resolve(JsonObject object) throws IllegalStateException {
        String schema = object.get("schema").getAsString();

        return switch (schema) {
            case "RANDOMIZE" -> RandomizedPlayerRank.New();
            case "WIN_LOSS" -> {
                int wins = object.get("wins").getAsInt();
                int losses = object.get("losses").getAsInt();
                yield new WinLossPlayerRank(wins, losses);
            }
            case "WIN_RATE" -> {
                int wins = object.get("wins").getAsInt();
                int losses = object.get("losses").getAsInt();
                int ties = object.get("ties").getAsInt();
                yield new WinRatePlayerRank(wins, losses, ties);
            }
            case "ELO" -> {
                double elo = object.get("elo").getAsDouble();
                yield new ELOPlayerRank(elo);
            }
            default -> throw new IllegalStateException("The passed rank object uses schema "+schema+" which doesn't match a supported schema!");
        };
    }

    public static DefaultRankResolver New() {
        return singleton;
    }
}
