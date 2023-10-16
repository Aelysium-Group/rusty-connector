package group.aelysium.rustyconnector.core.lib.connectors.storage;

import java.sql.ResultSet;
import java.util.function.Consumer;

public abstract class StorageResponse<R> implements AutoCloseable {
    protected boolean success;
    protected int rows;
    protected R result;

    public StorageResponse(boolean success, int rows, R result) {
        this.success = success;
        this.rows = rows;
        this.result = result;
    }

    public R result() { return this.result; }
    public boolean success() { return this.success; }
    public int rows() { return this.rows; }
    public abstract Type type();

    /**
     * Iterate over each row in the result set and call the callable for each one.
     * @param consumer A callable to be run for each row in the result set.
     */
    public abstract void forEach(Consumer<Object> consumer) throws Exception;

    /**
     * Get the first row of the result set.
     * @return The first row from the result set.
     */
    public abstract Object first() throws Exception;

    /**
     * Returns a new consumer which can be used to dynamically handle this response based on what type of {@link StorageResponse} it is.
     * @return A {@link ResultConsumer}.
     */
    public ResultConsumer consumer() {
        return new ResultConsumer(this);
    }

    public enum Type {
        MYSQL
    }
}
