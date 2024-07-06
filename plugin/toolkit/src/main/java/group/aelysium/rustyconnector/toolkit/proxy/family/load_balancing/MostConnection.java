package group.aelysium.rustyconnector.toolkit.proxy.family.load_balancing;

import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;
import group.aelysium.rustyconnector.toolkit.common.algorithm.QuickSort;
import group.aelysium.rustyconnector.toolkit.common.algorithm.WeightedQuickSort;
import group.aelysium.rustyconnector.toolkit.proxy.family.mcloader.IMCLoader;
import group.aelysium.rustyconnector.toolkit.proxy.util.LiquidTimestamp;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class MostConnection extends LeastConnection {
    public MostConnection(boolean weighted, boolean persistence, int attempts, @NotNull LiquidTimestamp rebalance) {
        super(weighted, persistence, attempts, rebalance);
    }

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
        return "LoadBalancer (MostConnection): "+this.mcloaders.size()+" items";
    }

    public static class Tinder extends Particle.Tinder<LoadBalancer> {
        private final LoadBalancer.Settings settings;

        public Tinder(@NotNull LoadBalancer.Settings settings) {
            this.settings = settings;
        }

        @Override
        public @NotNull LoadBalancer ignite() throws Exception {
            return new MostConnection(
                    this.settings.weighted(),
                    this.settings.persistence(),
                    this.settings.attempts(),
                    this.settings.rebalance()
            );
        }
    }
}
