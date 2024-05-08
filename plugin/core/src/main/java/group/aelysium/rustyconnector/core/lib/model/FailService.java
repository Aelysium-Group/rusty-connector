package group.aelysium.rustyconnector.core.lib.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;
import group.aelysium.rustyconnector.core.lib.crypt.Snowflake;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

public class FailService implements Service {
    protected final Cache<Long, Boolean> fails;
    protected int numberOfFails;
    protected LiquidTimestamp period;
    protected Snowflake snowflake = new Snowflake();

    /**
     * Define a new fail service.
     * @param numberOfFails The number of times that this {@link FailService} is allowed to be triggered within {@link FailService#period} before it fails.
     * @param period The amount of time to elapse before this {@link FailService} is allowed to be triggered more.
     */
    public FailService(int numberOfFails, LiquidTimestamp period) {
        super();
        this.numberOfFails = numberOfFails;
        this.period = period;
        this.fails = CacheBuilder.newBuilder()
                .maximumSize(numberOfFails + 1)
                .expireAfterWrite(this.period.value(), this.period.unit())
                .build();
    }

    /**
     * Triggers the fail service, telling it to register a new failure.
     * @param message The error message to throw if this trigger fails.
     * @throws RuntimeException When the {@link FailService} has failed to many times and can't anymore.
     */
    public void trigger(String message) throws RuntimeException {
        if(this.fails.size() > this.numberOfFails) throw new RuntimeException(message);

        this.fails.put(snowflake.nextId(), false);

        if(this.fails.size() > this.numberOfFails) throw new RuntimeException(message);
    }

    /**
     * Resets the fail service
     */
    public void reset() {
        this.fails.invalidateAll();
    }

    @Override
    public void kill() {
        this.numberOfFails = 0;
        this.fails.invalidateAll();
        this.fails.cleanUp();
    }
}
