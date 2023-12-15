package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.config;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.config.PartyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.DiscordWebhook;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookAlertFlag;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookEventManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.WebhookScope;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.AlgorithmType;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.gameplay.ITeam;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IScoreCard;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static one.microstream.math.XMath.round;

public class MatchMakerConfig extends YAML {
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

    protected MatchMakerConfig(Path dataFolder, String matchmakerName, LangService lang) {
        super(dataFolder, "matchmakers/"+matchmakerName+".yml", lang, LangFileMappings.PROXY_MATCHMAKER_TEMPLATE);
    }

    protected void register() throws IllegalStateException {
        this.algorithm = IScoreCard.IRankSchema.valueOf(this.getNode(this.data,"algorithm",String.class));

        this.variance = this.getNode(this.data,"variance",Double.class);
        this.variance = round(this.variance, 2);

        get(this.data,"teams").getChildrenList().forEach(node -> {
            String name = this.getNode(node, "name", String.class);
            int min = this.getNode(node, "min-players", Integer.class);
            int max = this.getNode(node, "max-players", Integer.class);
            this.teams.add(new ITeam.Settings(name, min, max));
        });

        try {
            this.matchmakingInterval = LiquidTimestamp.from(this.getNode(this.data, "matchmaking-interval", String.class));
        } catch (Exception ignore) {
            this.matchmakingInterval = LiquidTimestamp.from(10, TimeUnit.SECONDS);
        }
    }

    public static MatchMakerConfig construct(Path dataFolder, String matchmakerName, LangService lang) {
        MatchMakerConfig config = new MatchMakerConfig(dataFolder, matchmakerName, lang);
        config.register();
        return config;
    }
}
