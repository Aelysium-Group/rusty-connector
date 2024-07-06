package group.aelysium.rustyconnector.toolkit.proxy;

import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;
import group.aelysium.rustyconnector.toolkit.common.events.EventManager;
import group.aelysium.rustyconnector.toolkit.common.magic_link.IMagicLink;
import group.aelysium.rustyconnector.toolkit.proxy.family.Families;
import group.aelysium.rustyconnector.toolkit.proxy.family.whitelist.Whitelist;
import group.aelysium.rustyconnector.toolkit.proxy.lang.ProxyLangLibrary;
import group.aelysium.rustyconnector.toolkit.proxy.storage.LocalStorage;
import group.aelysium.rustyconnector.toolkit.proxy.storage.RemoteStorage;
import group.aelysium.rustyconnector.toolkit.proxy.util.Version;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IProxyFlame extends Particle {
    UUID uuid();

    Version version();

    ProxyAdapter<?, ?> Adapter();

    Particle.Flux<ProxyLangLibrary> Lang();
    Optional<Particle.Flux<Whitelist>> Whitelist();

    Particle.Flux<Families> Families();

    Particle.Flux<IMagicLink.Proxy> MagicLink();

    Particle.Flux<RemoteStorage> RemoteStorage();

    LocalStorage LocalStorage();

    EventManager EventManager();

    List<Component> bootLog();
}
