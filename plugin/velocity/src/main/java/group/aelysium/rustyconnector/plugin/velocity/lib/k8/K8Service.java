package group.aelysium.rustyconnector.plugin.velocity.lib.k8;

import group.aelysium.rustyconnector.api.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.K8PlayerServer;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;

import java.io.IOException;
import java.util.List;

public class K8Service implements Service {
    private ApiClient client;
    private final CoreV1Api api;
    private K8AutoScalerService autoScalerService;

    public K8Service() throws IOException {
        this.client = Config.defaultClient();
        Configuration.setDefaultApiClient(this.client);

        this.api = new CoreV1Api();
    }

    public K8AutoScalerService autoscalers() {
        return this.autoScalerService;
    }

    public void createServer(BaseFamily family, String containerName, int containerPort) throws ApiException {
        String podName = familyNameToPodPrefix(family.name());
        String namespace = familyNameToNamespace(family.name());

        V1ObjectMeta meta = new V1ObjectMeta();
        meta.name(podName);

        V1ContainerPort port = new V1ContainerPort();
        port.setContainerPort(containerPort);

        V1Container container = new V1Container();
        container.setName(containerName);
        container.setImage("nginx:latest");
        container.setPorts(List.of(port));

        V1PodSpec spec = new V1PodSpec();
        spec.setContainers(List.of(container));

        V1Pod pod = new V1Pod();
        pod.setMetadata(meta);
        pod.setSpec(spec);

        api.createNamespacedPod(namespace, pod, null, null, null, null);
    }

    public void deleteServer(K8PlayerServer server) throws ApiException {
        String namespace = familyNameToNamespace(server.family().name());
        api.deleteNamespacedPod(server.podName(), namespace, null, null, null, null, null, null);
    }

    private String familyNameToNamespace(String name) {
        return "rc-minecraft-family-"+name;
    }

    private String familyNameToPodPrefix(String name) {
        return "rc-minecraft-pod-"+name+"-";
    }

    @Override
    public void kill() {

    }
}
