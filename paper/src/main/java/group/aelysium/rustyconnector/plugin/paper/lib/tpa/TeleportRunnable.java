package group.aelysium.rustyconnector.plugin.paper.lib.tpa;

import group.aelysium.rustyconnector.plugin.paper.lib.lang_messaging.PaperLang;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TeleportRunnable extends BukkitRunnable {

    private final Player client;
    private final Player target;

    public TeleportRunnable(Player client, Player target) {
        this.client = client;
        this.target = target;
    }

    @Override
    public void run() {
        this.client.teleport(this.target.getLocation());
        this.client.sendMessage(PaperLang.TPA_COMPLETE);
    }
}