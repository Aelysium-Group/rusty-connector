package group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing;

import group.aelysium.rustyconnector.core.lib.util.QuickSort;
import group.aelysium.rustyconnector.core.lib.util.SingleSort;
import group.aelysium.rustyconnector.core.lib.util.WeightedQuickSort;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PlayerServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MostConnection extends LoadBalancer {

    @Override
    public void iterate() {
        try {
            PlayerServer thisItem = this.items.get(this.index);
            PlayerServer theNextItem = this.items.get(this.index + 1);

            if(thisItem.getPlayerCount() >= theNextItem.getPlayerCount()) this.index++;
        } catch (IndexOutOfBoundsException ignore) {}
    }

    @Override
    public void completeSort() {
        this.index = 0;
        if(this.isWeighted()) WeightedQuickSort.sort(this.items);
        else {
            QuickSort.sort(this.items);
            Collections.reverse(this.items);
        }
    }

    @Override
    public void singleSort() {
        this.index = 0;
        SingleSort.sort(this.items, this.index);
    }

    @Override
    public String toString() {
        return "LoadBalancer (MostConnection): "+this.size()+" items";
    }
}
