package group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing;

import group.aelysium.rustyconnector.core.lib.util.QuickSort;
import group.aelysium.rustyconnector.core.lib.util.SingleSort;
import group.aelysium.rustyconnector.core.lib.util.WeightedQuickSort;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PaperServer;

public class LeastConnection extends PaperServerLoadBalancer {

    @Override
    public void iterate() {
        try {
            PaperServer thisItem = this.items.get(this.index);
            PaperServer theNextItem = this.items.get(this.index + 1);

            if(thisItem.getPlayerCount() >= theNextItem.getPlayerCount()) this.index++;
        } catch (IndexOutOfBoundsException ignore) {}
    }

    @Override
    public void completeSort() {
        this.index = 0;
        if(this.isWeighted()) WeightedQuickSort.sort(this.items, VelocityRustyConnector.getInstance().logger());
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
