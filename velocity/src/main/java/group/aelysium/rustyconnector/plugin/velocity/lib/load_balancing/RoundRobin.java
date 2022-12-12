package group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing;

public class RoundRobin extends PaperServerLoadBalancer {
    @Override
    public String toString() {
        return "LoadBalancer (RoundRobin): "+this.size()+" items";
    }

    @Override
    public void completeSort() {}

    @Override
    public void singleSort() {}
}
