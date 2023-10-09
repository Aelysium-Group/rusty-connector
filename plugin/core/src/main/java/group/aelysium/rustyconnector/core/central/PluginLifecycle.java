package group.aelysium.rustyconnector.core.central;

import group.aelysium.rustyconnector.core.lib.exception.DuplicateLifecycleException;

public abstract class PluginLifecycle {
    protected boolean isRunning = false;

    public boolean isRunning() {
        return this.isRunning;
    }

    public abstract boolean start() throws DuplicateLifecycleException;
    public abstract void stop();

    protected abstract boolean loadConfigs();
    protected abstract boolean loadCommands();
    protected abstract boolean loadEvents();
}
