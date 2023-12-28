package group.aelysium.rustyconnector.plugin.velocity.lib.config.configs;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigService;
import group.aelysium.rustyconnector.toolkit.core.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.core.config.IYAML;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.gameplay.ITeam;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IScoreCard;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.eclipse.serializer.math.XMath.round;

public class MatchMakerConfig extends YAML implements group.aelysium.rustyconnector.toolkit.velocity.config.MatchMakerConfig {
    private IScoreCard.IRankSchema.Type<?> algorithm = IScoreCard.IRankSchema.RANDOMIZED;
    private final List<ITeam.Settings> teams = new ArrayList<>();
    private LiquidTimestamp matchmakingInterval;
    private double variance;

    public IScoreCard.IRankSchema.Type<?> getAlgorithm() {
        return algorithm;
    }
    public List<ITeam.Settings> getTeams() {
        return teams;
    }
    public LiquidTimestamp getMatchmakingInterval() {
        return matchmakingInterval;
    }
    public double getVariance() {
        return variance;
    }

    protected MatchMakerConfig(Path dataFolder, String target, String name, LangService lang) {
        super(dataFolder, target, name, lang, LangFileMappings.PROXY_MATCHMAKER_TEMPLATE);
    }

    @Override
    public IConfigService.ConfigKey key() {
        return new IConfigService.ConfigKey(MatchMakerConfig.class, name());
    }

    protected void register() throws IllegalStateException {
        this.algorithm = IScoreCard.IRankSchema.valueOf(IYAML.getValue(this.data,"algorithm",String.class));

        this.variance = IYAML.getValue(this.data,"variance",Double.class);
        this.variance = round(this.variance, 2);

        IYAML.get(this.data,"teams").getChildrenList().forEach(node -> {
            String name = IYAML.getValue(node, "name", String.class);
            int min = IYAML.getValue(node, "min-players", Integer.class);
            int max = IYAML.getValue(node, "max-players", Integer.class);
            this.teams.add(new ITeam.Settings(name, min, max));
        });

        try {
            this.matchmakingInterval = LiquidTimestamp.from(IYAML.getValue(this.data, "matchmaking-interval", String.class));
        } catch (Exception ignore) {
            this.matchmakingInterval = LiquidTimestamp.from(10, TimeUnit.SECONDS);
        }
    }

    public static MatchMakerConfig construct(Path dataFolder, String matchmakerName, LangService lang, ConfigService configService) {
        MatchMakerConfig config = new MatchMakerConfig(dataFolder, "matchmakers/"+matchmakerName+".yml", matchmakerName, lang);
        config.register();
        configService.put(config);
        return config;
    }
}
