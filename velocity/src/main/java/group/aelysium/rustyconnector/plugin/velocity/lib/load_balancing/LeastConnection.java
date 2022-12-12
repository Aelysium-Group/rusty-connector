package group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing;

import group.aelysium.rustyconnector.core.lib.util.QuickSort;
import group.aelysium.rustyconnector.core.lib.util.SingleSort;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PaperServer;

public class LeastConnection extends PaperServerLoadBalancer {
    /*
     * Used to speed up calculation times.
     * When we run iterate, we first check to see if `index` is still smaller than `secondComingIndex`
     * If it is, we don't do anything because the current server should still be the lowest.
     * If not, we run the iteration algorithm.

    protected int secondComingIndex = 0;

    @Override
    public void iterate() {
        int currentSecondComingIndex = 0;
        int currentIndex = 0;
        int lowestPlayerCount = 0;
        int lowestIndex = 0;

        if(this.items.get(this.index).getPlayerCount() < this.items.get(this.secondComingIndex).getPlayerCount()) return;

        for(PaperServer item : this.items) {
            int playerCount = item.getPlayerCount();
            if(currentIndex == 0 || playerCount < lowestPlayerCount) {
                currentSecondComingIndex = lowestIndex;

                lowestIndex = currentIndex;
                lowestPlayerCount = playerCount;
            }

            currentIndex++;
        }

        this.secondComingIndex = currentSecondComingIndex;
        this.index = lowestIndex;
    }*/

    @Override
    public void completeSort() {
        this.index = 0;
        QuickSort.sort(this.items);
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
