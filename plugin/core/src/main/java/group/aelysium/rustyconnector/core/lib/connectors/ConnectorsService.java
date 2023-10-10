package group.aelysium.rustyconnector.core.lib.connectors;

import com.sun.jdi.request.DuplicateRequestException;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnection;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnector;
import group.aelysium.rustyconnector.core.lib.connectors.storage.StorageConnection;
import group.aelysium.rustyconnector.core.lib.connectors.storage.StorageConnector;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectorsService extends Service {
    protected Map<String, MessengerConnector<? extends MessengerConnection>> messengers = new HashMap<>();
    protected Map<String, StorageConnector<? extends StorageConnection>> storage = new HashMap<>();

    public void add(String name, MessengerConnector<? extends MessengerConnection> connector) throws DuplicateRequestException {
        if(this.containsKey(name)) throw new DuplicateRequestException("You can't set the same name for different connectors!");
        messengers.put(name, connector);
    }
    public void add(String name, StorageConnector<? extends StorageConnection> connector) throws DuplicateRequestException {
        if(this.containsKey(name)) throw new DuplicateRequestException("You can't set the same name for different connectors!");
        storage.put(name, connector);
    }

    public MessengerConnector<? extends MessengerConnection> getMessenger(String name) {
        return messengers.get(name);
    }
    public StorageConnector<? extends StorageConnection> getStorage(String name) {
        return storage.get(name);
    }

    public boolean containsKey(String name) {
        return messengers.containsKey(name) ||
               storage.containsKey(name);
    }

    public List<MessengerConnector<?>> messengers() {
        return this.messengers.values().stream().toList();
    }

    public List<StorageConnector<?>> storage() {
        return this.storage.values().stream().toList();
    }

    @Override
    public void kill() {
        this.messengers.forEach((key, value) -> value.kill());
        this.storage.forEach((key, value) -> value.kill());
    }

    public enum MessengerConnectors {
        REDIS,
        RABBITMQ,
        WEBSOCKET,
    }

    public enum StorageConnectors {
        MYSQL,
    }
}
