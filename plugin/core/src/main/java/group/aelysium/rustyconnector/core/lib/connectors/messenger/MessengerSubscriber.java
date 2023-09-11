package group.aelysium.rustyconnector.core.lib.connectors.messenger;

import group.aelysium.rustyconnector.core.lib.hash.AESCryptor;

import java.util.Arrays;

public abstract class MessengerSubscriber {
    private final AESCryptor cryptor;

    public MessengerSubscriber(char[] privateKey) {
        this.cryptor = AESCryptor.create(Arrays.toString(privateKey));
    }

    public AESCryptor cryptor() { return this.cryptor; }

    protected abstract void onMessage(String rawMessage);
}
