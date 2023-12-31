package group.aelysium.rustyconnector.plugin.velocity.lib.config.configs;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigService;
import group.aelysium.rustyconnector.toolkit.core.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.core.config.IYAML;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IScoreCard;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.eclipse.serializer.math.XMath.round;

public class MatchMakerConfig extends YAML implements group.aelysium.rustyconnector.toolkit.velocity.config.MatchMakerConfig {
    private IScoreCard.IRankSchema.Type<?> algorithm = IScoreCard.IRankSchema.RANDOMIZED;
    private int min;
    private int max;
    private LiquidTimestamp matchmakingInterval;
    private double variance;

    public IScoreCard.IRankSchema.Type<?> getAlgorithm() {
        return algorithm;
    }
    public int min() {
        return this.min;
    }
    public int max() {
        return this.max;
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

        this.min = IYAML.getValue(this.data,"min",Integer.class);
        this.max = IYAML.getValue(this.data,"max",Integer.class);

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
