package group.aelysium.rustyconnector.plugin.velocity.lib.auto_scaling;

import group.aelysium.rustyconnector.core.lib.algorithm.QuickSort;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.K8MCLoader;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import io.fabric8.kubernetes.api.model.Pod;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.eclipse.serializer.math.XMath.round;

class Calculator {
    protected static LocalDateTime now() {
        return LocalDateTime.now();
    }
    protected static DayOfWeek currentWeekday() {
        return LocalDate.now().getDayOfWeek();
    }
    protected static int currentMonthDay() {
        return LocalDate.now().getDayOfMonth();
    }

    public static AutoScalerSettings.Capacity getCurrentCapacity(AutoScalerSettings settings) {
        AutoScalerSettings.Capacity defaultCapacity = settings.capacity();

        /*if(!settings.trafficCompensator().enabled()) */return defaultCapacity;
        /*
        LocalDateTime now = now();
        AutoScalerSettings.Capacity output = defaultCapacity;
        AutoScalerSettings.TimeStopAt lastMatch = null;

        for (AutoScalerSettings.TimeStop stop : settings.trafficCompensator().stops()) {
            LocalTime timeStopAt = stop.at().time();
            AutoScalerSettings.TrafficCompensator.Timespan timespan = settings.trafficCompensator().timespan();

            // Check if the month day matches, if applicable.
            if(timespan == AutoScalerSettings.TrafficCompensator.Timespan.MONTHLY)
                if(!stop.at().monthday().equals(currentMonthDay())) continue;

            // Check if weekday matches, if applicable.
            if(timespan == AutoScalerSettings.TrafficCompensator.Timespan.WEEKLY || timespan == AutoScalerSettings.TrafficCompensator.Timespan.MONTHLY)
                if(!stop.at().weekday().equals(currentWeekday())) continue;

            // Check for if timestop matches.
            if(now.isAfter(ChronoLocalDateTime.from(timeStopAt))) continue;
            if(timeStopAt.isBefore(lastMatch.time())) continue;

            lastMatch = stop.at();
            output = stop.capacity();
        }

        return output;*/
    }

    public static long playerCount(Family family, List<Pod> pods) {
        final long[] players = {0};
        pods.forEach(pod -> {
            String name = pod.getMetadata().getName();

            try {
                players[0] = players[0] + new K8MCLoader.Reference(name, family.id()).get().playerCount();
            } catch (Exception ignore) {}
        });
        return players[0];
    }

    /**
     * Calculates how much the family's pods should be adjusted based on what {@link group.aelysium.rustyconnector.plugin.velocity.lib.auto_scaling.AutoScalingSupervisor.FamilyStatus FamilyStatus} is.
     * If status is `CRITICAL_HIGH` the returned int is equal to how many pods should be created.
     * If status is `CRITICAL_LOW` the returned int is equal to how many pods could be deleted.
     */
    public static int podAdjustment(AutoScalingSupervisor.FamilyStatus status, AutoScalerSettings.Capacity capacity, double saturation, int podCount, long playerCount, AutoScalerSettings settings) {
        if(status == AutoScalingSupervisor.FamilyStatus.HEALTHY) return 0;

        int adjustPodsBy = 0;

        // Calculate how we should address the criticality
        switch (status) {
            case CRITIAL_LOW -> { // Remove pods until saturation is no longer critical
                double updatedSaturation = saturation;
                int i = 1;

                for (int j = 0; j < podCount; j++) {
                    updatedSaturation = saturation(podCount - i, playerCount);
                    if(updatedSaturation > settings.shavingRatio()) break;
                    i++;
                }

                adjustPodsBy = i;
            }
            case CRITIAL_HIGH -> { // Create pods until saturation is no longer critical
                double updatedSaturation = saturation;
                int i = 1;

                for (int j = 0; j < podCount; j++) {
                    updatedSaturation = saturation(podCount + i, playerCount);
                    if(updatedSaturation < settings.generatingRatio()) break;
                    i++;
                }

                adjustPodsBy = i;
            }
        }

        int simulatedPodAdjustment = podCount;
        if(status == AutoScalingSupervisor.FamilyStatus.CRITIAL_LOW)
            simulatedPodAdjustment = podCount + adjustPodsBy;
        if(status == AutoScalingSupervisor.FamilyStatus.CRITIAL_HIGH)
            simulatedPodAdjustment = podCount - adjustPodsBy;

        if(simulatedPodAdjustment > capacity.max()) adjustPodsBy = adjustPodsBy - (simulatedPodAdjustment - capacity.max());
        if(simulatedPodAdjustment < capacity.min()) adjustPodsBy = adjustPodsBy + (capacity.min() - simulatedPodAdjustment);

        return adjustPodsBy;
    }

    public static double saturation(int podCount, long playerCount) {
        return round((double) playerCount / podCount, 2);
    }

    public static List<Pod> podsToKill(List<Pod> pods, int podsToKill) {
        if(pods.size() <= podsToKill) return pods;

        List<SortablePod> sortablePods = new ArrayList<>();
        pods.forEach(pod -> {
            String name = pod.getMetadata().getName();

            try {
                MCLoader server = new K8MCLoader.Reference(name).get();

                sortablePods.add(new SortablePod(pod, server.playerCount()));
                return;
            } catch (NoSuchElementException ignore) {}
            sortablePods.add(new SortablePod(pod, 0));
        });

        QuickSort.sort(sortablePods);

        List<SortablePod> killablePods = sortablePods.subList(0, podsToKill).stream().toList();
        sortablePods.clear();

        List<Pod> output = new ArrayList<>();
        killablePods.forEach(pod -> output.add(pod.pod()));

        return output;
    }
}