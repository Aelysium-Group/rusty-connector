package group.aelysium.rustyconnector.plugin.velocity.lib.config.configs;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigService;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.rank.*;
import group.aelysium.rustyconnector.toolkit.core.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.core.config.IYAML;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchmaker;
import group.aelysium.rustyconnector.toolkit.core.matchmaking.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IVelocityPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class MatchmakerConfig extends YAML implements group.aelysium.rustyconnector.toolkit.velocity.config.MatchMakerConfig {
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
        String algorithm = IYAML.getValue(this.data,"algorithm",String.class);
        int min = IYAML.getValue(this.data,"min",Integer.class);
        int max = IYAML.getValue(this.data,"max",Integer.class);
        double variance = IYAML.getValue(this.data,"variance",Double.class);
        double varianceExpansionCoefficient = IYAML.getValue(this.data,"variance-expansion-coefficient",Double.class);
        int requiredExpansionsForAccept = IYAML.getValue(this.data,"required-expansions-for-accept",Integer.class);

        LiquidTimestamp sessionDispatchInterval = LiquidTimestamp.from(10, TimeUnit.SECONDS);
        try {
            sessionDispatchInterval = LiquidTimestamp.from(IYAML.getValue(this.data, "session-dispatch-interval", String.class));
        } catch (Exception ignore) {}

        boolean freezeActiveSessions = IYAML.getValue(this.data,"freeze-active-sessions",Boolean.class);

        int closingThreshold = IYAML.getValue(this.data,"closing-threshold",Integer.class);
        if(closingThreshold == -1) closingThreshold = min;

        boolean quittersLose = IYAML.getValue(this.data,"quitters-lose",Boolean.class);
        boolean stayersWin = IYAML.getValue(this.data,"stayers-win",Boolean.class);

        boolean leaveCommand = IYAML.getValue(this.data, "leave-command", Boolean.class);
        boolean parentFamilyOnLeave = IYAML.getValue(this.data, "parent-family-on-leave", Boolean.class);
        boolean showInfo = IYAML.getValue(this.data, "show-info", Boolean.class);

        Class<? extends IVelocityPlayerRank> actualSchema = RandomizedPlayerRank.class;
        if(algorithm.equals(WinLossPlayerRank.schema()))    actualSchema = WinLossPlayerRank.class;
        if(algorithm.equals(WinRatePlayerRank.schema()))    actualSchema = WinRatePlayerRank.class;
        if(algorithm.equals(ELOPlayerRank.schema()))        actualSchema = ELOPlayerRank.class;
        if(algorithm.equals(OpenSkillPlayerRank.schema()))  actualSchema = OpenSkillPlayerRank.class;



        double eloInitialRank = IYAML.getValue(this.data, "elo.initial-rank", Double.class);
        double eloFactor = IYAML.getValue(this.data, "elo.elo-factor", Double.class);
        double eloKFactor = IYAML.getValue(this.data, "elo.k-factor", Double.class);



        this.settings = new IMatchmaker.Settings(
                actualSchema,
                min,
                max,
                variance,
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
