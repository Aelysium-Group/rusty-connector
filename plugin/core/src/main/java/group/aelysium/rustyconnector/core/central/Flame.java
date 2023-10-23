package group.aelysium.rustyconnector.core.central;

import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnector;
import group.aelysium.rustyconnector.core.plugin.central.CoreServiceHandler;

public interface Flame {

    String versionAsString();

    void exhaust();

    MessengerConnector<?> backbone();

    CoreServiceHandler services();
}
