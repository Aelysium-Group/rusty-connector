package group.aelysium.rustyconnector.plugin.velocity.lib.auto_scaling;

import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ISortable;
import io.fabric8.kubernetes.api.model.Pod;

public class SortablePod implements ISortable {
    private Pod pod;
    private int playerCount;

    public SortablePod(Pod pod, int playerCount) {
        this.pod = pod;
        this.playerCount = playerCount;
    }

    public Pod pod() {
        return this.pod;
    }

    @Override
    public double sortIndex() {
        return this.playerCount;
    }

    @Override
    public int weight() {
        return 0;
    }
}
