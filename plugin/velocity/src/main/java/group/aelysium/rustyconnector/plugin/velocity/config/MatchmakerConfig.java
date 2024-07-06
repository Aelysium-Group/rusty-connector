package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.rustyconnector.common.config.Config;
import group.aelysium.rustyconnector.common.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.config.ConfigService;
import group.aelysium.rustyconnector.proxy.family.matchmaking.rank.*;
import group.aelysium.rustyconnector.toolkit.common.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.common.lang.IConfig;
import group.aelysium.rustyconnector.toolkit.common.lang.LangFileMappings;
import group.aelysium.rustyconnector.toolkit.proxy.family.matchmaking.IMatchmaker;
import group.aelysium.rustyconnector.toolkit.proxy.family.matchmaking.IVelocityPlayerRank;
import group.aelysium.rustyconnector.toolkit.proxy.util.LiquidTimestamp;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class MatchmakerConfig extends Config implements group.aelysium.rustyconnector.toolkit.proxy.config.MatchMakerConfig {
    private IMatchmaker.Settings settings;

    public IMatchmaker.Settings settings() {
        return this.settings;
    }

    protected MatchmakerConfig(Path dataFolder, String target, String name, LangService lang) {
        super(dataFolder, target, name, lang, LangFileMappings.PROXY_MATCHMAKER_TEMPLATE);
    }

    @Override
    public IConfigService.ConfigKey key() {
        return new IConfigService.ConfigKey(MatchmakerConfig.class, name());
    }

    protected void register() throws IllegalStateException {
        String algorithm = IConfig.getValue(this.data,"algorithm",String.class);
        int min = IConfig.getValue(this.data,"min",Integer.class);
        int max = IConfig.getValue(this.data,"max",Integer.class);
        double variance = IConfig.getValue(this.data,"variance",Double.class);
        boolean reconnect = IConfig.getValue(this.data,"reconnect",Boolean.class);
        double varianceExpansionCoefficient = IConfig.getValue(this.data,"variance-expansion-coefficient",Double.class);
        int requiredExpansionsForAccept = IConfig.getValue(this.data,"required-expansions-for-accept",Integer.class);

        LiquidTimestamp sessionDispatchInterval = LiquidTimestamp.from(10, TimeUnit.SECONDS);
        try {
            sessionDispatchInterval = LiquidTimestamp.from(IConfig.getValue(this.data, "session-dispatch-interval", String.class));
        } catch (Exception ignore) {}

        boolean freezeActiveSessions = IConfig.getValue(this.data,"freeze-active-sessions",Boolean.class);

        int closingThreshold = IConfig.getValue(this.data,"closing-threshold",Integer.class);
        if(closingThreshold == -1) closingThreshold = min;

        boolean quittersLose = IConfig.getValue(this.data,"quitters-lose",Boolean.class);
        boolean stayersWin = IConfig.getValue(this.data,"stayers-win",Boolean.class);

        boolean leaveCommand = IConfig.getValue(this.data, "leave-command", Boolean.class);
        boolean parentFamilyOnLeave = IConfig.getValue(this.data, "parent-family-on-leave", Boolean.class);
        boolean showInfo = IConfig.getValue(this.data, "show-info", Boolean.class);

        Class<? extends IVelocityPlayerRank> actualSchema = RandomizedPlayerRank.class;
        if(algorithm.equals(WinLossPlayerRank.schema()))    actualSchema = WinLossPlayerRank.class;
        if(algorithm.equals(WinRatePlayerRank.schema()))    actualSchema = WinRatePlayerRank.class;
        if(algorithm.equals(ELOPlayerRank.schema()))        actualSchema = ELOPlayerRank.class;
        if(algorithm.equals(OpenSkillPlayerRank.schema()))  actualSchema = OpenSkillPlayerRank.class;



        double eloInitialRank = IConfig.getValue(this.data, "elo.initial-rank", Double.class);
        double eloFactor = IConfig.getValue(this.data, "elo.elo-factor", Double.class);
        double eloKFactor = IConfig.getValue(this.data, "elo.k-factor", Double.class);



        this.settings = new IMatchmaker.Settings(
                actualSchema,
                min,
                max,
                variance,
                reconnect,
                varianceExpansionCoefficient,
                requiredExpansionsForAccept,
                sessionDispatchInterval,
                freezeActiveSessions,
                closingThreshold,
                quittersLose,
                stayersWin,
                leaveCommand,
                parentFamilyOnLeave,
                showInfo,
                new IMatchmaker.ELOSettings(eloInitialRank, eloFactor, eloKFactor)
        );
    }

    public static MatchmakerConfig construct(Path dataFolder, String matchmakerName, LangService lang, ConfigService configService) {
        MatchmakerConfig config = new MatchmakerConfig(dataFolder, "matchmakers/"+matchmakerName+".yml", matchmakerName, lang);
        config.register();
        configService.put(config);
        return config;
    }
}
