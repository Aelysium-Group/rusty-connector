package group.aelysium.rustyconnector.toolkit.common.magic_link;

import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.PacketListener;

import java.util.Optional;

/**
 * Magic Link is the dynamic messaging protocol used to handle connections between the Proxy and MCLoaders.
 */
public interface IMagicLink extends Particle {
    /**
     * Register a listener to handle particular packets.
     * @param listener The listener to use.
     */
    void on(PacketListener<? extends IPacket> listener);

    interface Proxy extends IMagicLink {
        /**
         * Fetches a Magic Link MCLoader Config based on a name.
         * `name` is considered to be the name of the file found in `magic_configs` on the Proxy, minus the file extension.
         * @param name The name to look for.
         */
        Optional<MagicLinkMCLoaderSettings> magicConfig(String name);

        record MagicLinkMCLoaderSettings(
                String family,
                int weight,
                int soft_cap,
                int hard_cap
        ) {};
    }

    interface MCLoader extends IMagicLink {
        /**
         * Gets the Magic Config that this MagicLink connection is governed by.
         */
        String magicConfig();

        /**
         * Set the ping delay for this upcoming ping.
         * @param delay The delay to set.
         */
        void setDelay(int delay);
    }
}
