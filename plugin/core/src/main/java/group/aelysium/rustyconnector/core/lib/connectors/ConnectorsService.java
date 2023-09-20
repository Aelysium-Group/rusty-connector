package group.aelysium.rustyconnector.core.lib.connectors;

import com.sun.jdi.request.DuplicateRequestException;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnection;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnector;
import group.aelysium.rustyconnector.core.lib.connectors.storage.StorageConnection;
import group.aelysium.rustyconnector.core.lib.connectors.storage.StorageConnector;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ConnectorsService extends Service {
    protected Map<String, Connector<? extends Connection>> connectors = new HashMap<>();

    public <C extends Connector<? extends Connection>> void add(String name, C connector) throws DuplicateRequestException {
        if(connectors.containsKey(name)) throw new DuplicateRequestException("You can't set the same name for different connectors!");
        connectors.put(name, connector);
    }

    public Connector<? extends Connection> get(String name) {
        return connectors.get(name);
    }

    public boolean containsKey(String name) {
        return connectors.containsKey(name);
    }

    public void forEach(BiConsumer<String, Connector<? extends Connection>> action) {
        this.connectors.forEach(action);
    }

    public List<MessengerConnector<?>> messengers() {
        List<MessengerConnector<?>> output = new ArrayList<>();
        this.connectors.values().stream().filter(connector -> connector instanceof MessengerConnector<?>).toList().forEach(item -> output.add((MessengerConnector<?>) item));
        return output;
    }

    public List<StorageConnector<?>> storage() {
        List<StorageConnector<?>> output = new ArrayList<>();
        this.connectors.values().stream().filter(connector -> connector instanceof StorageConnector<?>).toList().forEach(item -> output.add((StorageConnector<?>) item));
        return output;
    }

    @Override
    public void kill() {
        this.connectors.forEach((key, value) -> value.kill());
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
