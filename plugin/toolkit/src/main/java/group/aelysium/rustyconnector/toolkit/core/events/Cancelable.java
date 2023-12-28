package group.aelysium.rustyconnector.toolkit.core.events;

public class Cancelable implements Event {
    protected boolean canceled = false;

    public boolean isCanceled() {
        return this.canceled;
    }
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
