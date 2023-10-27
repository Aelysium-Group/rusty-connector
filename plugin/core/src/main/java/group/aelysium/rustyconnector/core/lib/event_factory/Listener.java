package group.aelysium.rustyconnector.core.lib.event_factory;

public abstract class Listener<E extends Event> {
    protected Class<? extends Event> invoker;

    private Listener() {}
    public Listener(Class<? extends Event> invoker) {
        this.invoker = invoker;
    }

    public Class<? extends Event> invoker() {
        return invoker;
    }

    public abstract void execute(E event);
}