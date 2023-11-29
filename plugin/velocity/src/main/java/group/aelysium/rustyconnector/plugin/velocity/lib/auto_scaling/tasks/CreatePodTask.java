package group.aelysium.rustyconnector.plugin.velocity.lib.auto_scaling.tasks;

import group.aelysium.rustyconnector.plugin.velocity.lib.auto_scaling.K8Service;

import java.util.concurrent.RecursiveAction;

public class CreatePodTask extends RecursiveAction {
    private K8Service service;
    private String familyName;
    private String containerName;
    private String containerImage;

    public CreatePodTask(K8Service service, String familyName, String containerName, String containerImage) {
        this.service = service;
        this.familyName = familyName;
        this.containerName = containerName;
        this.containerImage = containerImage;
    }

    protected void compute() {
        service.createServer(familyName, containerName, containerImage);
    }
}
