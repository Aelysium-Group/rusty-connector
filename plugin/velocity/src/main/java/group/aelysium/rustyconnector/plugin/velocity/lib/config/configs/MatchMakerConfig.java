package group.aelysium.rustyconnector.plugin.velocity.lib.config.configs;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigService;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RandomizedPlayerRank;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.WinLossPlayerRank;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.WinRatePlayerRank;
import group.aelysium.rustyconnector.toolkit.core.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.core.config.IYAML;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchmaker;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.eclipse.serializer.math.XMath.round;

public class MatchMakerConfig extends YAML implements group.aelysium.rustyconnector.toolkit.velocity.config.MatchMakerConfig {
    private IMatchmaker.Settings settings;

    public IMatchmaker.Settings settings() {
        return this.settings;
    }

    protected MatchMakerConfig(Path dataFolder, String target, String name, LangService lang) {
        super(dataFolder, target, name, lang, LangFileMappings.PROXY_MATCHMAKER_TEMPLATE);
    }

    @Override
    public IConfigService.ConfigKey key() {
        return new IConfigService.ConfigKey(MatchMakerConfig.class, name());
    }

    protected void register() throws IllegalStateException {
        String algorithm = IYAML.getValue(this.data,"ranking.algorithm",String.class);
        double variance = IYAML.getValue(this.data,"ranking.variance",Double.class);
        variance = round(variance, 2);

        boolean freezeActiveSessions = IYAML.getValue(this.data,"session.freeze-active-sessions",Boolean.class);
        int min = IYAML.getValue(this.data,"session.min",Integer.class);
        int max = IYAML.getValue(this.data,"session.max",Integer.class);

        LiquidTimestamp matchmakingInterval = LiquidTimestamp.from(10, TimeUnit.SECONDS);
        try {
            matchmakingInterval = LiquidTimestamp.from(IYAML.getValue(this.data, "session.building.interval", String.class));
        } catch (Exception ignore) {}

        int session_closing_threshold = IYAML.getValue(this.data,"session.closing.threshold",Integer.class);
        if(session_closing_threshold == -1) session_closing_threshold = min;

        boolean session_closing_ranks_quittersLose = IYAML.getValue(this.data,"session.quitters-lose",Boolean.class);
        boolean session_closing_ranks_stayersWin = IYAML.getValue(this.data,"session.stayers-win",Boolean.class);

        boolean queue_joining_showInfo = IYAML.getValue(this.data, "queue.joining.show-info", Boolean.class);
        boolean queue_joining_reconnect = IYAML.getValue(this.data, "queue.joining.reconnect", Boolean.class);

        boolean session_leaving_command = IYAML.getValue(this.data, "queue.leaving.command", Boolean.class);
        boolean session_leaving_boot = IYAML.getValue(this.data, "queue.leaving.boot", Boolean.class);

        Class<? extends IPlayerRank> actualSchema = switch (algorithm) {
            case "WIN_LOSS" -> WinLossPlayerRank.class;
            case "WIN_RATE" -> WinRatePlayerRank.class;
            default -> RandomizedPlayerRank.class;
        };

        this.settings = new IMatchmaker.Settings(
                new IMatchmaker.Settings.Ranking(actualSchema, variance),
                new IMatchmaker.Settings.Session(
                        freezeActiveSessions,
                        min,
                        max,
                        matchmakingInterval,
                        session_closing_threshold,
                        session_closing_ranks_quittersLose,
                        session_closing_ranks_stayersWin
                ),
                new IMatchmaker.Settings.Queue(
                        new IMatchmaker.Settings.Queue.Joining(queue_joining_showInfo, queue_joining_reconnect),
                        new IMatchmaker.Settings.Queue.Leaving(session_leaving_command, session_leaving_boot)
                )
        );
    }

    public static MatchMakerConfig construct(Path dataFolder, String matchmakerName, LangService lang, ConfigService configService) {
        MatchMakerConfig config = new MatchMakerConfig(dataFolder, "matchmakers/"+matchmakerName+".yml", matchmakerName, lang);
        config.register();
        configService.put(config);
        return config;
    }
}
