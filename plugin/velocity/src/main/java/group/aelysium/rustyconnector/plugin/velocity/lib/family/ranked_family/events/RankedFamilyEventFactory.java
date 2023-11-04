package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.events;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.api.core.serviceable.interfaces.Service;

import java.util.Vector;

public class RankedFamilyEventFactory implements Service {
    private final Vector<PlayerQueueEvent> queueEvents = new Vector<>();
    private final Vector<PlayerDeQueueEvent> deQueueEvents = new Vector<>();

    public void on(StackedFamilyEvent event) {
        if(event instanceof PlayerQueueEvent) this.queueEvents.add((PlayerQueueEvent) event);
        if(event instanceof PlayerDeQueueEvent) this.deQueueEvents.add((PlayerDeQueueEvent) event);
    }

    public void off(StackedFamilyEvent event) {
        if(event instanceof PlayerQueueEvent) this.queueEvents.remove((PlayerQueueEvent) event);
        if(event instanceof PlayerDeQueueEvent) this.deQueueEvents.remove((PlayerDeQueueEvent) event);
    }

    public EventCanon fire() {
        return new EventCanon();
    }

    @Override
    public void kill() {
        this.queueEvents.clear();
        this.deQueueEvents.clear();
    }

    public class EventCanon {
        public void playerQueueEvent(Player player) {
            RankedFamilyEventFactory.this.queueEvents.forEach(event -> event.execute(player));
        }
        public void playerDequeueEvent(Player player) {
            RankedFamilyEventFactory.this.deQueueEvents.forEach(event -> event.execute(player));
        }
    }
}