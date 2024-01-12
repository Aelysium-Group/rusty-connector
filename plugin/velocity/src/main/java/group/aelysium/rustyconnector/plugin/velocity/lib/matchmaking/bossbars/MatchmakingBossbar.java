package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.bossbars;

import net.kyori.adventure.bossbar.BossBar;

public class MatchmakingBossbar {
    public static void WAITING_FOR_PLAYERS(BossBar bossbar, int players, int max) {
        float percentage = (float) players / max;

        BossBar.Color color = BossBar.Color.WHITE;
        if(percentage > 0.5) color = BossBar.Color.YELLOW;
        if(percentage >= 1) color = BossBar.Color.GREEN;

        bossbar.color(color);
        bossbar.progress(percentage);
    }

    public static void WAITING_FOR_SERVERS(BossBar bossbar, int closedServers, int openServers) {
        int totalServers = closedServers + openServers;
        float percentage = (float) openServers / totalServers;

        bossbar.color(BossBar.Color.BLUE);
        bossbar.progress(percentage);
    }
}
