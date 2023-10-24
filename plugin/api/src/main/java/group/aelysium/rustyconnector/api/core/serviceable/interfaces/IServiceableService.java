package group.aelysium.rustyconnector.api.core.serviceable.interfaces;

public interface IServiceableService<H extends IServiceHandler> extends Service {
    H services();
}
