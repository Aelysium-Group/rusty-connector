package group.aelysium.rustyconnector.toolkit.core.event_factory;

public abstract class Listener<E extends Event> {
    private Class<E> invoker;

    private Listener() {}
    protected Listener(Class<E> invoker) {
        this.invoker = invoker;
    }

    public Class<E> invoker() {
        return invoker;
    }

    public abstract void execute(E event);
}