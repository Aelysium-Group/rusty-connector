package group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing;

import group.aelysium.rustyconnector.core.lib.algorithm.QuickSort;
import group.aelysium.rustyconnector.core.lib.algorithm.SingleSort;
import group.aelysium.rustyconnector.core.lib.algorithm.WeightedQuickSort;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;

public class LeastConnection extends LoadBalancer {

    @Override
    public void iterate() {
        try {
            PlayerServer thisItem = this.items.get(this.index);
            PlayerServer theNextItem = this.items.get(this.index + 1);

            if(thisItem.playerCount() >= theNextItem.playerCount()) this.index++;
        } catch (IndexOutOfBoundsException ignore) {}
    }

    @Override
    public void completeSort() {
        this.index = 0;
        if(this.weighted()) WeightedQuickSort.sort(this.items);
        else QuickSort.sort(this.items);
    }

    @Override
    public void singleSort() {
        this.index = 0;
        SingleSort.sort(this.items, this.index);
    }

    @Override
    public String toString() {
        return "LoadBalancer (LeastConnection): "+this.size()+" items";
    }
}
