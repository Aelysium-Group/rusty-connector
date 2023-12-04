package group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing;

import group.aelysium.rustyconnector.core.lib.algorithm.QuickSort;
import group.aelysium.rustyconnector.core.lib.algorithm.WeightedQuickSort;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;

import java.util.Collections;

public class MostConnection extends LeastConnection {
    public MostConnection(Settings settings) {
        super(settings);
    } // Extends LeastConnection to fill in some gaps that LoadBalancer leaves open

    @Override
    public void iterate() {
        try {
            MCLoader currentItem = this.servers.get(this.index);

            if(currentItem.playerCount() + 1 > currentItem.hardPlayerCap()) this.index++;
            if(this.index >= this.servers.size()) this.index = 0;
        } catch (IndexOutOfBoundsException ignore) {}
    }

    
    @Override
    public void completeSort() {
        this.index = 0;
        if(this.weighted()) WeightedQuickSort.sort(this.servers);
        else {
            QuickSort.sort(this.servers);
            Collections.reverse(this.servers);
        }
    }

    @Override
    public String toString() {
        return "LoadBalancer (MostConnection): "+this.size()+" items";
    }
}
