package group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing;

import group.aelysium.rustyconnector.core.lib.algorithm.QuickSort;
import group.aelysium.rustyconnector.core.lib.algorithm.SingleSort;
import group.aelysium.rustyconnector.core.lib.algorithm.WeightedQuickSort;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

public class LeastConnection extends LoadBalancer {

    public LeastConnection(Settings settings) {
        super(settings);
    }

    @Override
    public void iterate() {
        try {
            IMCLoader thisItem = this.unlockedServers.get(this.index);
            IMCLoader theNextItem = this.unlockedServers.get(this.index + 1);

            if(thisItem.playerCount() >= theNextItem.playerCount()) this.index++;
        } catch (IndexOutOfBoundsException ignore) {}
    }

    @Override
    public void completeSort() {
        this.index = 0;
        if(this.weighted()) WeightedQuickSort.sort(this.unlockedServers);
        else QuickSort.sort(this.unlockedServers);
    }

    @Override
    public void singleSort() {
        this.index = 0;
        SingleSort.sortDesc(this.unlockedServers, this.index);
    }

    @Override
    public String toString() {
        return "LoadBalancer (LeastConnection): "+this.size()+" items";
    }
}
