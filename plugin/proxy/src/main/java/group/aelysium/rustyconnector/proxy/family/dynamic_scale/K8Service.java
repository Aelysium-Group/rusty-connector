package group.aelysium.rustyconnector.proxy.family.dynamic_scale;

import group.aelysium.rustyconnector.toolkit.common.crypt.Token;
import group.aelysium.rustyconnector.proxy.family.Family;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.*;

import java.security.SecureRandom;
import java.util.List;
import java.util.NoSuchElementException;

public class K8Service implements AutoCloseable {
    protected KubernetesClient client = new KubernetesClientBuilder().build();
    protected Token token = new Token(16, new SecureRandom(), Token.digits);

    public K8Service() {}

    public List<Pod> familyPods(String familyName) {
        try {
            new Family.Reference(familyName).get();
        } catch (Exception ignore) {
            throw new NoSuchElementException("A family with the id "+familyName+" doesn't exist!");
        }

        String namespace = getNamespaceName(familyName);

        try (KubernetesClient client = new KubernetesClientBuilder().build()) {
            return client.pods().inNamespace(namespace).list().getItems();
        }
    }

    public String createPod(String familyName, String containerImage) {
        String podName = newPodName(familyName);
        String namespace = getNamespaceName(familyName);

        Pod pod = new PodBuilder()
                .withNewMetadata()
                    .withName(podName)
                    .withNamespace(namespace)
                .endMetadata()
                .withNewSpec()
                    .withServiceAccount("rusty-connector")
                    .addNewContainer()
                        .withName(familyName)
                        .withImage(containerImage)
                        .withEnv(new EnvVar("POD_NAME", podName, null))
                    .endContainer()
                .endSpec()
                .build();
        client.pods().inNamespace(namespace).resource(pod).create();

        return podName;
    }

    public Pod fetchPod(String familyName, String podName) {
        String namespace = getNamespaceName(familyName);

        return client.pods().inNamespace(namespace).withName(podName).get();
    }

    public void deletePod(String familyName, String podName) {
        String namespace = getNamespaceName(familyName);

        client.pods().inNamespace(namespace).withName(podName.toLowerCase()).delete();
    }

    public void createNamespace(String familyName) {
        Namespace namespace = new NamespaceBuilder()
                .withNewMetadata()
                .withName(getNamespaceName(familyName))
                .endMetadata()
                .build();

        client.namespaces().createOrReplace(namespace);
        System.out.println("Namespace created successfully!");
    }

    public void deleteNamespace(String familyName) {
        Namespace namespace = new NamespaceBuilder()
                .withNewMetadata()
                .withName(getNamespaceName(familyName))
                .endMetadata()
                .build();

        client.namespaces().createOrReplace(namespace);
        System.out.println("Namespace created successfully!");
    }

    private String getNamespaceName(String familyName) {
        return ("rcf-"+familyName).toLowerCase();
    }

    private String newPodName(String familyName) {
        return (familyName+"-"+token.nextString()).toLowerCase();
    }

    @Override
    public void close() throws Exception {
        this.client.close();
    }
}
