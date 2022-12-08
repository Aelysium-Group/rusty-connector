package group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing;

public class RoundRobin extends PaperServerLoadBalancer {
    @Override
    public void iterate() {
        this.index += 1;
        if(this.index >= this.items.size()) this.index = 0;
    }

    @Override
    public String toString() {
        return "LoadBalancer (RoundRobin): "+this.size()+" items";
    }
}
