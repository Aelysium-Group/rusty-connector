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
    protected Map<String, Connector<Connection>> connectors = new HashMap<>();

    public void add(String name, Connector<Connection> connector) throws DuplicateRequestException {
        if(connectors.containsKey(name)) throw new DuplicateRequestException("You can't set the same name for different connectors!");
        connectors.put(name, connector);
    }

    public Connector<Connection> get(String name) {
        return connectors.get(name);
    }

    public boolean containsKey(String name) {
        return connectors.containsKey(name);
    }

    public void forEach(BiConsumer<String, Connector<Connection>> action) {
        this.connectors.forEach(action);
    }

    public List<MessengerConnector<? extends MessengerConnection>> messengers() {
        List<MessengerConnector<? extends MessengerConnection>> output = new ArrayList<>();
        this.connectors.values().stream().filter(connector -> connector instanceof MessengerConnector<?>).toList().forEach(item -> output.add((MessengerConnector) output));
        return output;
    }

    public List<StorageConnector<? extends StorageConnection>> storage() {
        List<StorageConnector<? extends StorageConnection>> output = new ArrayList<>();
        this.connectors.values().stream().filter(connector -> connector instanceof StorageConnector<?>).toList().forEach(item -> output.add((StorageConnector) output));
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
