package group.aelysium.rustyconnector.api.core.messenger;

import group.aelysium.rustyconnector.api.core.crypt.IAESCryptor;

public interface IMessengerSubscriber {
    /**
     * Gets the AESCrypto being used by this subscriber.
     * @return {@link IAESCryptor}
     */
    IAESCryptor cryptor();
}
