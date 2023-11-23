package group.aelysium.rustyconnector.plugin.velocity.lib.auto_scaling.tasks;

import group.aelysium.rustyconnector.plugin.velocity.lib.k8.K8Service;

import java.util.concurrent.RecursiveAction;

public class CreatePodTask extends RecursiveAction {
    private K8Service service;
    private String familyName;
    private String containerName;
    private int containerPort;

    public CreatePodTask(K8Service service, String familyName, String containerName, int containerPort) {
        this.service = service;
        this.familyName = familyName;
        this.containerName = containerName;
        this.containerPort = containerPort;
    }

    protected void compute() {
        int size = service.getPods(familyName).size();
    }
}
