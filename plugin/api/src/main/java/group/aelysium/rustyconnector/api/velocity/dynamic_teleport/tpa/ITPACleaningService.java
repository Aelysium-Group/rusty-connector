package group.aelysium.rustyconnector.api.velocity.dynamic_teleport.tpa;

import group.aelysium.rustyconnector.api.core.serviceable.interfaces.Service;

public interface ITPACleaningService<TTPAService extends ITPAService<?, ?, ?, ?, ?>> extends Service {
    void startHeartbeat(TTPAService tpaService);
}
