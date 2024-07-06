package group.aelysium.rustyconnector.toolkit;

import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;
import group.aelysium.rustyconnector.toolkit.common.events.EventManager;
import group.aelysium.rustyconnector.toolkit.common.magic_link.IMagicLink;
import group.aelysium.rustyconnector.toolkit.mc_loader.MCLoaderAdapter;
import group.aelysium.rustyconnector.toolkit.mc_loader.lang.MCLoaderLangLibrary;
import group.aelysium.rustyconnector.toolkit.proxy.ProxyAdapter;
import group.aelysium.rustyconnector.toolkit.proxy.family.Families;
import group.aelysium.rustyconnector.toolkit.proxy.family.Family;
import group.aelysium.rustyconnector.toolkit.proxy.family.mcloader.MCLoader;
import group.aelysium.rustyconnector.toolkit.proxy.family.whitelist.Whitelist;
import group.aelysium.rustyconnector.toolkit.proxy.lang.ProxyLangLibrary;
import group.aelysium.rustyconnector.toolkit.proxy.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.proxy.storage.RemoteStorage;
import group.aelysium.rustyconnector.toolkit.proxy.storage.LocalStorage;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

/**
 * This interface provides shorthand fetch operations for common requests.
 * All fetch operations are non-thread locking and will either return the desired outcome immediately or throw an Exception.
 * For more control over handling edge cases and issues, see {@link RustyConnector}.
 */
public interface RC {
    /**
     * The interface containing Proxy based operations.
     */
    interface P {
        static Families Families() throws NoSuchElementException {
            return RustyConnector.Toolkit.Proxy().orElseThrow().orElseThrow().Families().orElseThrow();
        }

        static Optional<Whitelist> Whitelist() throws NoSuchElementException {
            Optional<Particle.Flux<Whitelist>> whitelistOptional = RustyConnector.Toolkit.Proxy().orElseThrow().orElseThrow().Whitelist();
            if(whitelistOptional.isEmpty()) return Optional.empty();
            return Optional.of(whitelistOptional.orElseThrow().orElseThrow());
        }

        static IMagicLink.Proxy MagicLink() throws NoSuchElementException {
            return RustyConnector.Toolkit.Proxy().orElseThrow().orElseThrow().MagicLink().orElseThrow();
        }

        static RemoteStorage RemoteStorage() throws NoSuchElementException {
            return RustyConnector.Toolkit.Proxy().orElseThrow().orElseThrow().RemoteStorage().orElseThrow();
        }

        static LocalStorage LocalStorage() throws NoSuchElementException {
            return RustyConnector.Toolkit.Proxy().orElseThrow().orElseThrow().LocalStorage();
        }

        static EventManager EventManager() throws NoSuchElementException {
            return RustyConnector.Toolkit.Proxy().orElseThrow().orElseThrow().EventManager();
        }

        static ProxyAdapter<?, ?> Adapter() throws NoSuchElementException {
            return RustyConnector.Toolkit.Proxy().orElseThrow().orElseThrow().Adapter();
        }

        static ProxyLangLibrary Lang() throws NoSuchElementException {
            return RustyConnector.Toolkit.Proxy().orElseThrow().orElseThrow().Lang().orElseThrow();
        }

        static Optional<Family> Family(String id) throws NoSuchElementException {
            Family family = null;
            try {
                family = RustyConnector.Toolkit.Proxy().orElseThrow().orElseThrow().Families().orElseThrow().find(id).orElseThrow().orElseThrow();
            } catch (Exception ignore) {}
            return Optional.ofNullable(family);
        }

        static Optional<MCLoader> MCLoader(UUID uuid) throws NoSuchElementException {
            return RustyConnector.Toolkit.Proxy().orElseThrow().orElseThrow().LocalStorage().mcloaders().fetch(uuid);
        }

        static Optional<IPlayer> Player(UUID uuid) throws NoSuchElementException {
            return RustyConnector.Toolkit.Proxy().orElseThrow().orElseThrow().LocalStorage().players().fetch(uuid);
        }
    }

    /**
     * The interface containing MCLoader based operations.
     */
    interface M {
        static IMagicLink.MCLoader MagicLink() throws NoSuchElementException {
            return RustyConnector.Toolkit.MCLoader().orElseThrow().orElseThrow().MagicLink().orElseThrow();
        }

        static MCLoaderAdapter Adapter() throws NoSuchElementException {
            return RustyConnector.Toolkit.MCLoader().orElseThrow().orElseThrow().Adapter();
        }

        static MCLoaderLangLibrary Lang() throws NoSuchElementException {
            return RustyConnector.Toolkit.MCLoader().orElseThrow().orElseThrow().Lang().orElseThrow();
        }

        static EventManager EventManager() throws NoSuchElementException {
            return RustyConnector.Toolkit.MCLoader().orElseThrow().orElseThrow().EventManager();
        }
    }
}
