package group.aelysium.rustyconnector.toolkit.core.serviceable;

public abstract class Serviceable<H extends ServiceHandler> {
    protected H services;

    public Serviceable(H services) {
        this.services = services;
    }

    public H services() {
        return this.services;
    }
}