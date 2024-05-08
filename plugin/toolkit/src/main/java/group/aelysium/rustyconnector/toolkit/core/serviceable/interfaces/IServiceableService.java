package group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces;

public interface IServiceableService<H extends IServiceHandler> extends Service {
    H services();
}
