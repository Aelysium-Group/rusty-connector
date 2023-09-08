package group.aelysium.rustyconnector.core.lib.connectors.messenger;

public abstract class MessengerSubscriber {
    protected abstract void onMessage(String rawMessage);
}
