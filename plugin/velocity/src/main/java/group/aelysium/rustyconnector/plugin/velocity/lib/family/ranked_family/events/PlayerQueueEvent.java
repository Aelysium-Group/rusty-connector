package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.events;

import com.velocitypowered.api.proxy.Player;

public abstract class PlayerQueueEvent extends StackedFamilyEvent {
    public abstract void execute(Player player);
}
