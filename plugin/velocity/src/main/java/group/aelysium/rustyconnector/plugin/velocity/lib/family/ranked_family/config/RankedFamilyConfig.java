package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.config;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import ninja.leaping.configurate.ConfigurationNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RankedFamilyConfig extends YAML {
    private Component displayName;

    private Family.Reference parent_family = Family.Reference.rootFamily();
    private boolean whitelist_enabled = false;
    private String whitelist_name = "whitelist-template";

    public RankedFamilyConfig(String dataFolder, String familyName) {
        super(new File(dataFolder, "families/"+familyName+".ranked.yml"));
    }

    public Component displayName() { return displayName; }
    public Family.Reference getParent_family() { return parent_family; }

    public boolean isWhitelist_enabled() {
        return whitelist_enabled;
    }

    public String getWhitelist_name() {
        return whitelist_name;
    }

    public void register(String familyName) throws IllegalStateException {
        /*
        try {
            String name = this.getNode(this.data, "display-id", String.class);
            this.displayName = MiniMessage.miniMessage().deserialize(name);
        } catch (Exception ignore) {}

        {
            String gamemodeName = this.getNode(this.data, "ranking.gamemode-id", String.class);
            if (gamemodeName.equals("default")) gamemodeName = familyName;
            RankedGame.ScoringType scoring = RankedGame.ScoringType.valueOf(this.getNode(this.data, "ranking.scoring", String.class));

            RankedGame.RankerType matchmakingConfiguration = RankedGame.RankerType.valueOf(this.getNode(this.data, "ranking.matchmaking.configuration", String.class));

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
                                this.getNode(team, "id", String.class),
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

            this.matchmakingSettings = new RankedMatchmaker.Settings(gamemodeName, matchmakingConfiguration, soloSettings, coopSettings, scoring, variance, interval);
        }

        try {
            this.parent_family = new Family.Reference(this.getNode(this.data, "parent-family", String.class));
        } catch (Exception ignore) {}

        this.whitelist_enabled = this.getNode(this.data,"whitelist.enabled",Boolean.class);
        this.whitelist_name = this.getNode(this.data,"whitelist.id",String.class);
        if(this.whitelist_enabled && this.whitelist_name.equals(""))
            throw new IllegalStateException("whitelist.id cannot be empty in order to use a whitelist in a family!");

        this.whitelist_name = this.whitelist_name.replaceFirst("\\.yml$|\\.yaml$","");*/
    }
}