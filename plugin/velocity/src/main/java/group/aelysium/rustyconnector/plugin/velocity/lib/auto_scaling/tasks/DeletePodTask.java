package group.aelysium.rustyconnector.plugin.velocity.lib.auto_scaling.tasks;

import group.aelysium.rustyconnector.plugin.velocity.lib.auto_scaling.K8Service;

import java.util.concurrent.RecursiveAction;

public class DeletePodTask extends RecursiveAction {
    private K8Service service;
    private String familyName;
    private String podName;

    public DeletePodTask(K8Service service, String familyName, String podName) {
        this.service = service;
        this.familyName = familyName;
        this.podName = podName;
    }

    protected void compute() {
        service.deleteServer(familyName, podName);
    }
}
