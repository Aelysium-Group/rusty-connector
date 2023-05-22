package group.aelysium.rustyconnector.core.lib.model;

public abstract class IRKLifecycle {
    public abstract static class Builder {
        public Builder() {}
        abstract IRKLifecycle build();
    }

    public abstract static class Lifecycle {
        /**
         * Init the object.
         */
        abstract void init();

        /**
         * Should reload the object.
         * If the object has state generated as a result of a config file, this should completely reload based on the config file.
         */
        abstract void reload();

        /**
         * Kill the object. Once this has been called the object should be guaranteed to be garbage collected.
         */
        abstract void kill();
    }
}
