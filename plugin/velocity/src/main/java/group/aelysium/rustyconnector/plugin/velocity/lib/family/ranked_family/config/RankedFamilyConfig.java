package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.config;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.RankedMatchmakerSettings;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.RankedGameRankerType;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.RankedGameScoringType;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.solo.RankedSoloGame;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.teams.RankedTeam;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.teams.RankedTeamGame;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;
import ninja.leaping.configurate.ConfigurationNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RankedFamilyConfig extends YAML {

    private RankedMatchmakerSettings matchmakingSettings;
    private String loadBalancer = "default";
    private String parent_family = "";
    private boolean whitelist_enabled = false;
    private String whitelist_name = "whitelist-template";

    public RankedFamilyConfig(String dataFolder, String familyName) {
        super(new File(dataFolder, "families/"+familyName+".ranked.yml"));
    }

    public RankedMatchmakerSettings getMatchmakingSettings() {
        return matchmakingSettings;
    }
    public String getParent_family() { return parent_family; }
    public String loadBalancer() { return loadBalancer; }

    public boolean isWhitelist_enabled() {
        return whitelist_enabled;
    }

    public String getWhitelist_name() {
        return whitelist_name;
    }

    public void register(String familyName) throws IllegalStateException {
        {
            String gamemodeName = this.getNode(this.data, "ranking.gamemode-name", String.class);
            if (gamemodeName.equals("default")) gamemodeName = familyName;
            RankedGameScoringType scoring = RankedGameScoringType.valueOf(this.getNode(this.data, "ranking.scoring", String.class));

            RankedGameRankerType matchmakingConfiguration = RankedGameRankerType.valueOf(this.getNode(this.data, "ranking.matchmaking.configuration", String.class));

            RankedSoloGame.Settings soloSettings = null;
            RankedTeamGame.Settings coopSettings = null;
            switch (matchmakingConfiguration) {
                case SOLO -> {
                    int max = this.getNode(this.data, "ranking.matchmaking.solo.max-players", Integer.class);
                    int min = this.getNode(this.data, "ranking.matchmaking.solo.min-players", Integer.class);
                    soloSettings = new RankedSoloGame.Settings(max, min);
                }
                case CO_OP -> {
                    int min = this.getNode(this.data, "ranking.matchmaking.co_op.min-players", Integer.class);

                    List<? extends ConfigurationNode> teamNodes = get(this.data, "ranking.matchmaking.co_op.teams").getChildrenList();

                    List<RankedTeam.Settings> teams = new ArrayList<>();
                    if (teamNodes.size() == 0)
                        throw new IllegalStateException("You must list teams in your co_op configuration!");

                    for (ConfigurationNode team : teamNodes)
                        teams.add(new RankedTeam.Settings(
                                this.getNode(team, "name", String.class),
                                this.getNode(team, "players", Integer.class)
                        ));

                    coopSettings = new RankedTeamGame.Settings(teams, min);
                }
                default -> throw new IllegalStateException("ranking.matchmaking.configuration is of an invalid type!");
            }

            double variance = this.getNode(this.data, "ranking.matchmaking.variance", Double.class);
            LiquidTimestamp interval;
            try {
                String intervalString = this.getNode(this.data, "ranking.matchmaking.interval", String.class);
                interval = LiquidTimestamp.from(intervalString);
                if (interval.compareTo(LiquidTimestamp.from(3, TimeUnit.SECONDS)) < 0)
                    interval = LiquidTimestamp.from(3, TimeUnit.SECONDS);
            } catch (Exception e) {
                interval = LiquidTimestamp.from(10, TimeUnit.SECONDS);
            }

            this.matchmakingSettings = new RankedMatchmakerSettings(matchmakingConfiguration, soloSettings, coopSettings, scoring, variance, interval);
        }

        try {
            this.parent_family = this.getNode(this.data, "parent-family", String.class);
        } catch (Exception ignore) {
            this.parent_family = "";
        }

        try {
            this.loadBalancer = this.getNode(this.data, "load-balancer", String.class);
        } catch (Exception ignore) {
            this.loadBalancer = "default";
        }
        this.loadBalancer = this.loadBalancer.replaceFirst("\\.yml$|\\.yaml$","");

        this.whitelist_enabled = this.getNode(this.data,"whitelist.enabled",Boolean.class);
        this.whitelist_name = this.getNode(this.data,"whitelist.name",String.class);
        if(this.whitelist_enabled && this.whitelist_name.equals(""))
            throw new IllegalStateException("whitelist.name cannot be empty in order to use a whitelist in a family!");

        this.whitelist_name = this.whitelist_name.replaceFirst("\\.yml$|\\.yaml$","");
    }
}