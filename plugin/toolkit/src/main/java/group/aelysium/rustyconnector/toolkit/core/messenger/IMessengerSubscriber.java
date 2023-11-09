package group.aelysium.rustyconnector.toolkit.core.messenger;

import group.aelysium.rustyconnector.toolkit.core.crypt.IAESCryptor;

public interface IMessengerSubscriber {
    /**
     * Gets the AESCrypto being used by this subscriber.
     * @return {@link IAESCryptor}
     */
    IAESCryptor cryptor();
}
