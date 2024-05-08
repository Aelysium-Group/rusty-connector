package group.aelysium.rustyconnector.toolkit.velocity.util;

import java.util.NoSuchElementException;

public abstract class Reference<T, R> {
    protected R referencer;

    public Reference(R referencer) {
        this.referencer = referencer;
    }

    /**
     * Gets the owner referenced.
     * If the owner could not be found, this will throw an exception.
     * @return {@link T}
     * @throws java.util.NoSuchElementException If the owner of this reference can't be found.
     */
    public abstract <TT extends T> TT get() throws NoSuchElementException;

    public R referencer() {
        return this.referencer;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Reference<T, R> that = (Reference<T, R>) object;
        return this.referencer.equals(that.referencer());
    }
}
