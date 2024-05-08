package group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing;

import group.aelysium.rustyconnector.core.lib.algorithm.QuickSort;
import group.aelysium.rustyconnector.core.lib.algorithm.WeightedQuickSort;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

import java.util.Collections;

public class MostConnection extends LeastConnection {
    public MostConnection(Settings settings) {
        super(settings);
    } // Extends LeastConnection to fill in some gaps that LoadBalancer leaves open

    @Override
    public void iterate() {
        try {
            IMCLoader currentItem = this.unlockedServers.get(this.index);

            if(currentItem.playerCount() + 1 > currentItem.hardPlayerCap()) this.index++;
            if(this.index >= this.unlockedServers.size()) this.index = 0;
        } catch (IndexOutOfBoundsException ignore) {}
    }

    
    @Override
    public void completeSort() {
        this.index = 0;
        if(this.weighted()) WeightedQuickSort.sort(this.unlockedServers);
        else {
            QuickSort.sort(this.unlockedServers);
            Collections.reverse(this.unlockedServers);
        }
    }

    @Override
    public String toString() {
        return "LoadBalancer (MostConnection): "+this.size()+" items";
    }
}
