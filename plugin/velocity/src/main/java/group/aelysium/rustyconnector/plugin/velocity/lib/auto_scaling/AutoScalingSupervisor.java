package group.aelysium.rustyconnector.plugin.velocity.lib.auto_scaling;

import group.aelysium.rustyconnector.plugin.velocity.lib.auto_scaling.tasks.AutoScaleDispatch;
import group.aelysium.rustyconnector.plugin.velocity.lib.auto_scaling.tasks.CreatePodTask;
import group.aelysium.rustyconnector.plugin.velocity.lib.auto_scaling.tasks.DeletePodTask;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.k8.K8Service;
import group.aelysium.rustyconnector.toolkit.core.serviceable.ClockService;
import io.fabric8.kubernetes.api.model.Pod;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import static group.aelysium.rustyconnector.plugin.velocity.lib.auto_scaling.Calculator.*;

public class AutoScalingSupervisor extends ClockService {
    protected K8Service service;
    protected ForkJoinPool pool = ForkJoinPool.commonPool();
    protected Optional<AutoScaleDispatch> task = Optional.empty();
    protected Map<Family, AutoScalerSettings> families = new HashMap<>();

    public AutoScalingSupervisor(K8Service service) {
        super(2);
        this.service = service;
    }

    public void dispatchPodCommands(Family family, AutoScalerSettings settings, List<Pod> pods, FamilyStatus status, int adjustBy) {
        switch (status) {
            case CRITIAL_LOW -> {
                List<Pod> killable = podsToKill(pods, adjustBy);

                killable.forEach(pod -> {
                    try {
                        this.task.orElseThrow().queue(new DeletePodTask(service, family.id(), pod.getMetadata().getName()));
                    } catch (Exception ignore) {
                        this.task = Optional.of(new AutoScaleDispatch());
                    }
                });
            }
            case CRITIAL_HIGH -> {
                for (int i = 0; i < adjustBy; i++) {
                    try {
                        this.task.orElseThrow().queue(new CreatePodTask(service, family.id(), settings.container(), settings.image()));
                    } catch (Exception ignore) {
                        this.task = Optional.of(new AutoScaleDispatch());
                    }
                }
            }
        }
    }

    public void start() {
        this.executorService.schedule(()->{
            this.task = Optional.of(new AutoScaleDispatch());

            for(Map.Entry<Family, AutoScalerSettings> entry : families.entrySet()) {
                Family family = entry.getKey();
                AutoScalerSettings settings = entry.getValue();
                List<Pod> pods = service.familyPods(family.id());

                // Calculate some numbers
                int podCount = pods.size();
                AutoScalerSettings.Capacity capacity = Calculator.getCurrentCapacity(settings);
                long totalPlayerCount = Calculator.playerCount(family, pods);
                double saturation = saturation(podCount, totalPlayerCount);

                // Calculate family status
                FamilyStatus status = FamilyStatus.HEALTHY;
                if(saturation < settings.shavingRatio()) status = FamilyStatus.CRITIAL_LOW;
                else if(saturation > settings.generatingRatio()) status = FamilyStatus.CRITIAL_HIGH;

                // Calculate saturation levels and create/delete pods if needed.
                int adjustPodsBy = podAdjustment(status, capacity, saturation, podCount, totalPlayerCount, settings);
                if(adjustPodsBy == 0) continue;
                dispatchPodCommands(family, settings, pods, status, adjustPodsBy);
            }

            if(task.isPresent())
                try {
                    pool.invoke(task.get());
                } catch (Exception ignore) {}

            this.task = Optional.empty();
            this.start();
        }, 1, TimeUnit.MINUTES);
    }

    @Override
    public void kill() {
        pool.shutdownNow();
        super.kill();
        this.service.kill();
    }

    enum FamilyStatus {
        CRITIAL_HIGH,
        HEALTHY,
        CRITIAL_LOW
    }
}
