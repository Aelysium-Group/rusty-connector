package group.aelysium.rustyconnector.toolkit.proxy.util;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class LiquidTimestamp implements Comparable<LiquidTimestamp> {
    protected TimeUnit unit;
    protected int value;

    private LiquidTimestamp(int value, TimeUnit unit) {
        this.value = value;
        this.unit = unit;
    }
    public static LiquidTimestamp from(String stamp) throws ParseException {
        stamp = stamp.toLowerCase();
        int value;
        try {
            value = Integer.parseInt(stamp.replaceAll("(\\d*).*","$1"));
        } catch (Exception e) {
            throw new ParseException("Unable to parse string. No valid integer value to extract!", 0);
        }

        TimeUnit unit = null;
        try {
            if(stamp.contains("s")) unit = TimeUnit.SECONDS;
            if(stamp.contains("m")) unit = TimeUnit.MINUTES;
            if(stamp.contains("h")) unit = TimeUnit.HOURS;
            if(stamp.contains("d")) unit = TimeUnit.DAYS;

            if(stamp.contains("second")) unit = TimeUnit.SECONDS;
            if(stamp.contains("minute")) unit = TimeUnit.MINUTES;
            if(stamp.contains("hour")) unit = TimeUnit.HOURS;
            if(stamp.contains("day")) unit = TimeUnit.DAYS;

            if(stamp.contains("seconds")) unit = TimeUnit.SECONDS;
            if(stamp.contains("minutes")) unit = TimeUnit.MINUTES;
            if(stamp.contains("hours")) unit = TimeUnit.HOURS;
            if(stamp.contains("days")) unit = TimeUnit.DAYS;
        } catch (Exception ignore) {}

        if(stamp.contains("month")) throw new ParseException("Found `month` being used as a LiquidTimestamp. This is no longer allowed!", 0);
        if(stamp.contains("months")) throw new ParseException("Found `months` being used as a LiquidTimestamp. This is no longer allowed!", 0);

        if(unit == null) throw new ParseException("Unable to parse string. No valid unit to extract!", 0);

        return new LiquidTimestamp(value, unit);
    }

    public TimeUnit unit() {
        return this.unit;
    }
    public int value() {
        return this.value;
    }

    /**
     * Returns a unix timestamp set `value` number of `units` away from now.
     * @return The Unix timestamp in seconds.
     */
    public long epochFromNow() {
        if(this.unit() == null) return 0;
        long time = Instant.now().getEpochSecond();
        switch (this.unit) {
            case DAYS -> time += ((long) this.value * 24 * 60 * 60);
            case HOURS -> time += ((long) this.value * 60 * 60);
            case MINUTES -> time += ((long) this.value * 60);
            case SECONDS -> time += ((long) this.value);
        }

        return time * 1000;
    }

    /**
     * Returns a unix timestamp set `value` number of `units` before from now.
     * @return The Unix timestamp in seconds.
     */
    public long epochBeforeNow() {
        long time = Instant.now().getEpochSecond();
        switch (this.unit) {
            case DAYS -> time -= ((long) this.value * 24 * 60 * 60);
            case HOURS -> time -= ((long) this.value * 60 * 60);
            case MINUTES -> time -= ((long) this.value * 60);
            case SECONDS -> time -= ((long) this.value);
        }

        return time * 1000;
    }

    @Override
    public String toString() {
        return this.value + " " + this.unit.toString();
    }

    @Override
    public int compareTo(@NotNull LiquidTimestamp o) {
        return Long.compare(this.epochFromNow(), o.epochFromNow());
    }

    public static LiquidTimestamp from(int value, TimeUnit unit) {
        return new LiquidTimestamp(value, unit);
    }
}
