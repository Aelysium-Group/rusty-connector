package group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces;

import java.util.Optional;

public interface IServiceHandler {
    /**
     * Find a {@link Service} based off of it's class instance.
     * Calls to this method should use {@link Object#getClass() object.class}.
     * @param type The class to search with.
     * @return {@link Optional<Service>}
     */
    <S extends Service> Optional<S> find(Class<S> type);

    /**
     * Add a {@link Service} to this {@link IServiceHandler}.
     * Only one {@link Service} of each type is allowed. Two {@link Service Services} of the same type cannot be stored.
     * @param service The {@link Service} to store.
     */
    <S extends Service> void add(S service);

    /**
     * Kills all services handled by this {@link IServiceHandler}.
     */
    void killAll();
}