package group.aelysium.rustyconnector.core.lib.model;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;

public class LiquidTimestamp {
    protected String initialValue;
    protected LiquidUnit unit;
    protected int value;

    public LiquidTimestamp(String stamp) throws ParseException {
        this.initialValue = stamp;
        stamp = stamp.toLowerCase();
        int value;
        try {
            value = Integer.parseInt(stamp.replaceAll("(\\d*).*","$1"));
        } catch (Exception e) {
            throw new ParseException("Unable to parse string. No valid integer value to extract!", 0);
        }

        LiquidUnit unit = null;
        try {
            if(stamp.contains("s")) unit = LiquidUnit.SECONDS;
            if(stamp.contains("m")) unit = LiquidUnit.MINUTES;
            if(stamp.contains("h")) unit = LiquidUnit.HOURS;
            if(stamp.contains("d")) unit = LiquidUnit.DAYS;

            if(stamp.contains("second")) unit = LiquidUnit.SECONDS;
            if(stamp.contains("minute")) unit = LiquidUnit.MINUTES;
            if(stamp.contains("hour")) unit = LiquidUnit.HOURS;
            if(stamp.contains("day")) unit = LiquidUnit.DAYS;

            if(stamp.contains("seconds")) unit = LiquidUnit.SECONDS;
            if(stamp.contains("minutes")) unit = LiquidUnit.MINUTES;
            if(stamp.contains("hours")) unit = LiquidUnit.HOURS;
            if(stamp.contains("days")) unit = LiquidUnit.DAYS;
        } catch (Exception ignore) {}

        if(stamp.contains("month"))  throw new ParseException("Found `months` being used as a LiquidTimestamp. This is no longer allowed!", 0);

        if(unit == null) throw new ParseException("Unable to parse string. No valid unit to extract!", 0);

        this.value = value;
        this.unit = unit;
    }

    public LiquidUnit getUnit() {
        return this.unit;
    }
    public int getValue() {
        return this.value;
    }

    /**
     * Returns a unix timestamp set `value` number of `units` away from now.
     * @return The Unix timestamp in milliseconds.
     */
    public long getEpochFromNow() {
        long time = Instant.now().getEpochSecond();
        switch (this.unit) {
            case DAYS -> time += ((long) this.value * 24 * 60 * 60);
            case HOURS -> time += ((long) this.value * 60 * 60);
            case MINUTES -> time += ((long) this.value * 60);
            case SECONDS -> time += ((long) this.value);
        }

        return time * 1000;
    }

    @Override
    public String toString() {
        return this.initialValue;
    }

    public enum LiquidUnit {
        SECONDS,
        MINUTES,
        HOURS,
        DAYS
    }
}
