package group.aelysium.rustyconnector.core.lib.model;

public abstract class IKLifecycle {
    /**
     * Kill the object. Once this has been called the object should be guaranteed to be garbage collected.
     */
    protected abstract void kill();

    /**
     * Init the object.
     */
    protected static IKLifecycle init() {
        return null;
    }

    public abstract static class Builder {
        public Builder() {}
        abstract <I extends IKLifecycle> I build();
    }
}
