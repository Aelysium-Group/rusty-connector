package group.aelysium.rustyconnector.plugin.velocity.lib.k8;

import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.K8PlayerServer;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;

import java.io.IOException;

public class K8Service extends Service {
    private ApiClient client;
    private BatchV1Api api;
    private K8AutoScalerService autoScalerService;

    public K8Service() throws IOException {
        this.client = Config.defaultClient();
        Configuration.setDefaultApiClient(this.client);

        this.api = new BatchV1Api();
    }

    public K8AutoScalerService autoscalers() {
        return this.autoScalerService;
    }

    public void createServer(String namespace) throws ApiException {
        {
            V1Pod pod = new V1Pod();
            V1ObjectMeta meta = new V1ObjectMeta();
            meta.name("rc-minecraft-" + namespace + "-");
            meta.deletionGracePeriodSeconds(0L);
            pod.metadata(meta);
        }

        V1Job job = new V1Job();
        V1ObjectMeta meta = new V1ObjectMeta();
        meta.name("rc-minecraft-"+namespace+"-");
        meta.deletionGracePeriodSeconds(0L);
        job.metadata(meta);

        V1JobSpec spec = new V1JobSpec();
        job.spec();

        this.api.createNamespacedJob(namespace, null, null, null, null, null);
    }

    public void deleteServer(K8PlayerServer server) throws ApiException {
    }

    @Override
    public void kill() {

    }
}
