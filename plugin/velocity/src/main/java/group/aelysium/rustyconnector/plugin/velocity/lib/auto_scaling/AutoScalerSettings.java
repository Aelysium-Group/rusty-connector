package group.aelysium.rustyconnector.plugin.velocity.lib.auto_scaling;

import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record AutoScalerSettings (
        String container,
        String image,
        Capacity capacity,
        double generatingRatio,
        double shavingRatio
) {
    record TrafficCompensator(boolean enabled, Timespan timespan, List<TimeStop> stops) {
        enum Timespan {
            DAILY,
            WEEKLY,
            MONTHLY
        }
    }
    record TimeStop(@NotNull TimeStopAt at, Capacity capacity) {}

    record TimeStopAt (@NotNull LocalTime time, DayOfWeek weekday, Integer monthday) {}
    record Capacity(int min, int max) {
        /**
         * Checks if the current count is within the capacity range that's defined.
         * @param count The count to validate with.
         * @return -1 if {@param count} is less than {@link Capacity#min}. 1 if {@param count} is more than {@link Capacity#max}. 0 if {@param count} is within the range.
         */
         public int compare(int count) {
             if(count < min) return -1;
             if(count > max) return 1;
             return 0;
         }
    }
}
