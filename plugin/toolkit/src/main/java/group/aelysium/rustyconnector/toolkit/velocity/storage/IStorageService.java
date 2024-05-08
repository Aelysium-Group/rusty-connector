package group.aelysium.rustyconnector.toolkit.velocity.storage;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

public interface IStorageService extends Service {
    IDatabase database();
}
