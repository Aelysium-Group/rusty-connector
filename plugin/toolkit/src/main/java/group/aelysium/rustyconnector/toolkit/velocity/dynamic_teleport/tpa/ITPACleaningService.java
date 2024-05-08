package group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.tpa;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

public interface ITPACleaningService extends Service {
    void startHeartbeat(ITPAService tpaService);
}
