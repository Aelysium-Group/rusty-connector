package group.aelysium.rustyconnector.plugin.velocity.lib.auto_scaling.tasks;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

public class AutoScaleDispatch extends RecursiveAction {
    List<RecursiveAction> tasks = new ArrayList<>();

    public void queue(CreatePodTask task) {
        this.tasks.add(task);
    }
    public void queue(DeletePodTask task) {
        this.tasks.add(task);
    }

    protected void compute() {
        for (RecursiveAction task : tasks) task.fork();
    }
}