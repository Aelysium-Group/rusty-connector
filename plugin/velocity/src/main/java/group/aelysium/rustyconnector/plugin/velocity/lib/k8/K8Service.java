package group.aelysium.rustyconnector.plugin.velocity.lib.k8;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.*;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;

import java.io.IOException;
import java.util.List;

public class K8Service implements Service {
    private K8AutoScalerService autoScalerService;

    public K8Service() throws IOException {
    }

    public K8AutoScalerService autoscalers() {
        return this.autoScalerService;
    }

    public void createServer(String familyName, String containerName, int containerPort, String containerImage) {
        String podName = familyNameToPodPrefix(familyName);
        String namespace = familyNameToNamespace(familyName);
        String serviceAccountName = "rustyconnector";
        String image = "your-image-name";

        try (KubernetesClient client = new KubernetesClientBuilder().build()) {
            Pod pod = new PodBuilder()
                    .withNewMetadata()
                    .withName(podName)
                    .endMetadata()
                    .withNewSpec()
                    .addNewContainer()
                    .withName(containerName)
                    .withImage(containerImage)
                    .endContainer()
                    .endSpec()
                    .build();
            client.pods().inNamespace(namespace).resource(pod).create();
        }
    }

    public void deleteServer(String familyName, String podName) {
        String namespace = familyNameToNamespace(familyName);

        try (KubernetesClient client = new KubernetesClientBuilder().build()) {
            client.pods().inNamespace(namespace).withName(podName).delete();
        }
    }

    public List<Pod> getPods(String familyName) {
        String namespace = familyNameToNamespace(familyName);

        try (KubernetesClient client = new KubernetesClientBuilder().build()) {
            return client.pods().inNamespace(namespace).list().getItems();
        }
    }

    private String familyNameToNamespace(String name) {
        return "rc-minecraft-family-"+name;
    }

    private String familyNameToPodPrefix(String name) {
        return "rc-minecraft-pod-"+name;
    }

    @Override
    public void kill() {

    }
}
