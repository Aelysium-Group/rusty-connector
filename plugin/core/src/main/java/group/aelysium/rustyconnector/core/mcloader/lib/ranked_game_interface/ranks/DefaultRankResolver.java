package group.aelysium.rustyconnector.core.mcloader.lib.ranked_game_interface.ranks;

import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.toolkit.core.matchmaking.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IRankResolver;

public class DefaultRankResolver implements IRankResolver {
    protected static final DefaultRankResolver singleton = new DefaultRankResolver();
    protected DefaultRankResolver() {}

    @Override
    public IPlayerRank resolve(JsonObject object) throws IllegalStateException {
        String schema = object.get("schema").getAsString();

        if(schema.equals(RandomizedPlayerRank.schema()))
            return RandomizedPlayerRank.New();
        if(schema.equals(WinLossPlayerRank.schema())) {
            int wins = object.get("wins").getAsInt();
            int losses = object.get("losses").getAsInt();
            return new WinLossPlayerRank(wins, losses);
        }
        if(schema.equals(WinRatePlayerRank.schema())) {
            int wins = object.get("wins").getAsInt();
            int losses = object.get("losses").getAsInt();
            int ties = object.get("ties").getAsInt();
            return new WinRatePlayerRank(wins, losses, ties);
        }
        if(schema.equals(ELOPlayerRank.schema())) {
            int elo = object.get("elo").getAsInt();
            return new ELOPlayerRank(elo);
        }
        if(schema.equals(OpenSkillPlayerRank.schema())) {
            double mu = object.get("mu").getAsDouble();
            double sigma = object.get("sigma").getAsDouble();
            return new OpenSkillPlayerRank(mu, sigma);
        }
        throw new IllegalStateException("The passed rank object uses schema "+schema+" which doesn't match a supported schema!");
    }

    public static DefaultRankResolver New() {
        return singleton;
    }
}
