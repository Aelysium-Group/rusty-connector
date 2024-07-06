package group.aelysium.rustyconnector.proxy.family.dynamic_scale;

import group.aelysium.rustyconnector.toolkit.proxy.family.Family;
import group.aelysium.rustyconnector.toolkit.proxy.family.mcloader.IMCLoader;
import group.aelysium.rustyconnector.toolkit.proxy.util.LiquidTimestamp;
import io.fabric8.kubernetes.api.model.Pod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class Scaler {
    private static final K8Service k8 = new K8Service();
    protected final Map<String, CompletableFuture<IMCLoader>> pendingMCLoaders = new ConcurrentHashMap<>();
    protected final Settings settings;
    protected final Family family;

    public Scaler(Settings settings, Family family) {
        this.settings = settings;
        this.family = family;
    }

    /**
     * Graduates a pod to become an MCLoader.
     * This method doesn't actually alter any internal state.
     * The graduate methods query Kubernetes directly.
     * This method is just a mask which resolves the {@link CompletableFuture} that's waiting for a
     * specific MCLoader to registerProxy.
     * @param podName The name of the pod to graduate.
     * @param mcLoader The MCLoader that the pod has graduated as.
     * @return `true` if the pod was successfully graduated. `false` otherwise (includes if the podName doesn't exist as an undergraduate)
     */
    public boolean graduatePod(String podName, IMCLoader mcLoader) {
        CompletableFuture<IMCLoader> future = this.pendingMCLoaders.get(podName);
        if(future == null) return false;
        future.complete(mcLoader);
        return true;
    }

    /**
     * Creates a new MCLoader.
     * @return A future that will resolve into the created MCLoader once it's booted and registered to the proxy.
     *         If the MCLoader fails to boot. The returned future will fail instantly.
     *         Otherwise, the future will wait for as long as it needs to for the MCLoader to boot.
     */
    public CompletableFuture<IMCLoader> createMCLoader() {
        String podName = k8.createPod(this.family.id(), this.settings.helmChart());

        CompletableFuture<IMCLoader> future = new CompletableFuture<>();
        this.pendingMCLoaders.put(podName, future);

        return future;
    }

    /**
     * Deletes the MCLoader from the Kubernetes cluster.
     * If the MCLoader is not contained within a pod (specifically if it is not contained within a pod with the env variable "POD_NAME" set)
     * this method won't do anything.
     */
    public void deleteMCLoader(IMCLoader mcLoader) {
        if(mcLoader.podName().isEmpty()) return;
        k8.deletePod(this.family.id(), mcLoader.podName().get());
    }

    /**
     * Returns a list of all the Pods that are being managed by the Dynamic Scaler.
     */
    public List<Pod> pods() {
        return k8.familyPods(this.family.id());
    }

    /**
     * Returns a list of the Pods which have fully graduated and are now MCLoaders.
     * In more technical terms, these pods have finished booting up and their copy of RustyConnector has successfully
     * registered to the proxy.
     */
    public List<IMCLoader> graduatedPods() {
        List<Pod> pods = k8.familyPods(this.family.id());
        List<IMCLoader> mcLoaders = this.family.loadBalancer().servers();

        List<IMCLoader> output = new ArrayList<>();

        if(pods.size() < mcLoaders.size())
            pods.forEach(p -> output.addAll(mcLoaders.stream().filter(m -> {
                if(m.podName().isEmpty()) return false;
                return m.podName().get().equals(p.getMetadata().getName());
            }).toList()));
        else
            mcLoaders.forEach(m -> {
                if(m.podName().isEmpty()) return;
                if(pods.stream().noneMatch(p -> p.getMetadata().getName().equals(m.podName().get()))) return;

                output.add(m);
            });

        return output;
    }

    /**
     * Returns a list of Pods which haven't graduated to becoming MCLoaders.
     * In more technical terms, these pods have not finished booting up and the copy of RustyConnector
     * running on them has not yet booted/registered to the proxy.
     */
    public List<Pod> undergraduatePods() {
        List<Pod> pods = k8.familyPods(this.family.id());
        List<IMCLoader> mcLoaders = this.family.loadBalancer().servers();

        List<Pod> output = new ArrayList<>(pods);

        pods.forEach(p -> {
            if(mcLoaders.stream().anyMatch(m -> m.podName().orElse("").equals(p.getMetadata().getName()))) return;
            output.add(p);
        });

        return output;
    }

    public void collectTelemetry() {
        long count = this.family.playerCount();
    }

    public record Settings(
            boolean enabled,
            String helmChart,
            int maxPods,
            Reactive reactive,
            ProactiveAlgorithm proactiveAlgorithm,
            Scheduled scheduled,
            Predictive predictive
    ) {
        public record Reactive(
                Generation generation,
                Degeneration degeneration
        ) {
            public record Generation(
                    int ratio,
                    LiquidTimestamp delay,
                    int count
            ){}
            public record Degeneration(
                    int ratio,
                    LiquidTimestamp delay
            ) {}
        }

        public enum ProactiveAlgorithm {
            NONE,
            SCHEDULED,
            PREDICTIVE
        }

        public record Scheduled(
                String start,
                Object timespans
        ) {}

        public record Predictive(
                String keepTelemetryFor,
                String resolution,
                double idealRatio
        ) {}
    }
}
