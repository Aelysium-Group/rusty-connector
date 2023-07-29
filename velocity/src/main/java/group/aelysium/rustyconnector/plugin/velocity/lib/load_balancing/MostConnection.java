package group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing;

import group.aelysium.rustyconnector.core.lib.algorithm.QuickSort;
import group.aelysium.rustyconnector.core.lib.algorithm.WeightedQuickSort;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;

import java.util.Collections;

public class MostConnection extends LeastConnection { // Extends LeastConnection to fill in some gaps that LoadBalancer leaves open

    @Override
    public void iterate() {
        try {
            PlayerServer currentItem = this.items.get(this.index);

            if(currentItem.playerCount() + 1 >= currentItem.hardPlayerCap()) this.index++;
            if(this.index >= this.items.size()) this.index = 0;
        } catch (IndexOutOfBoundsException ignore) {}
    }

    
    @Override
    public void completeSort() {
        this.index = 0;
        if(this.weighted()) WeightedQuickSort.sort(this.items);
        else {
            QuickSort.sort(this.items);
            Collections.reverse(this.items);
        }
    }

    @Override
    public String toString() {
        return "LoadBalancer (MostConnection): "+this.size()+" items";
    }
}
