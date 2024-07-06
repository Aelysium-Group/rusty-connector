package group.aelysium.rustyconnector.proxy;

import group.aelysium.rustyconnector.proxy.event_handlers.OnFamilyLeave;
import group.aelysium.rustyconnector.proxy.event_handlers.OnMCLoaderLeave;
import group.aelysium.rustyconnector.proxy.event_handlers.OnMCLoaderRegister;
import group.aelysium.rustyconnector.proxy.event_handlers.OnMCLoaderUnregister;
import group.aelysium.rustyconnector.proxy.magic_link.MagicLink;
import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;
import group.aelysium.rustyconnector.toolkit.common.events.EventManager;
import group.aelysium.rustyconnector.toolkit.common.magic_link.IMagicLink;
import group.aelysium.rustyconnector.toolkit.proxy.IProxyFlame;
import group.aelysium.rustyconnector.toolkit.proxy.ProxyAdapter;
import group.aelysium.rustyconnector.toolkit.proxy.events.mc_loader.MCLoaderRegisterEvent;
import group.aelysium.rustyconnector.toolkit.proxy.events.mc_loader.MCLoaderUnregisterEvent;
import group.aelysium.rustyconnector.toolkit.proxy.events.player.FamilyLeaveEvent;
import group.aelysium.rustyconnector.toolkit.proxy.events.player.MCLoaderLeaveEvent;
import group.aelysium.rustyconnector.toolkit.proxy.family.Families;
import group.aelysium.rustyconnector.toolkit.proxy.family.whitelist.Whitelist;
import group.aelysium.rustyconnector.toolkit.proxy.lang.ProxyLangLibrary;
import group.aelysium.rustyconnector.toolkit.proxy.storage.LocalStorage;
import group.aelysium.rustyconnector.toolkit.proxy.storage.RemoteStorage;
import group.aelysium.rustyconnector.toolkit.proxy.util.Version;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProxyFlame implements IProxyFlame {
    private final UUID uuid;
    private final Version version;
    private final ProxyAdapter<?, ?> adapter;
    private final Particle.Flux<ProxyLangLibrary> lang;
    private final Particle.Flux<Whitelist> whitelist;
    private final Particle.Flux<Families> families;
    private final Particle.Flux<IMagicLink.Proxy> magicLink;
    private final Particle.Flux<RemoteStorage> remoteStorage;
    private final LocalStorage localStorage;
    private final EventManager eventManager;
    private final List<Component> bootOutput;

    protected ProxyFlame(
            @NotNull UUID uuid,
            @NotNull Version version,
            @NotNull ProxyAdapter<?, ?> adapter,
            @NotNull Particle.Flux<ProxyLangLibrary> lang,
            @Nullable Particle.Flux<Whitelist> whitelist,
            @NotNull List<Component> bootOutput,
            @NotNull Particle.Flux<Families> families,
            @NotNull Particle.Flux<IMagicLink.Proxy> magicLink,
            @NotNull Particle.Flux<RemoteStorage> remoteStorage,
            @NotNull LocalStorage localStorage,
            @NotNull EventManager eventManager
    ) {
        this.uuid = uuid;
        this.version = version;
        this.adapter = adapter;
        this.lang = lang;
        this.whitelist = whitelist;
        this.bootOutput = bootOutput;
        this.families = families;
        this.magicLink = magicLink;
        this.remoteStorage = remoteStorage;
        this.localStorage = localStorage;
        this.eventManager = eventManager;
    }

    /**
     * Gets the uuid of this Proxy.
     * The Proxy's uuid shouldn't change between re-boots unless you delete the files on the Proxy.
     * @return {@link UUID}
     */
    public UUID uuid() {
        return this.uuid;
    }

    /**
     * Gets the current version of RustyConnector
     * @return {@link Version}
     */
    public Version version() {
        return this.version;
    }

    public ProxyAdapter<?, ?> Adapter() {
        return this.adapter;
    }
    public Particle.Flux<ProxyLangLibrary> Lang() {
        return this.lang;
    }
    public Optional<Particle.Flux<Whitelist>> Whitelist() {
        return Optional.ofNullable(this.whitelist);
    }
    public Particle.Flux<Families> Families() {
        return this.families;
    }
    public Particle.Flux<IMagicLink.Proxy> MagicLink() {
        return this.magicLink;
    }
    public Particle.Flux<RemoteStorage> RemoteStorage() {
        return this.remoteStorage;
    }
    public LocalStorage LocalStorage() {
        return this.localStorage;
    }

    public EventManager EventManager() {
        return this.eventManager;
    }

    public List<Component> bootLog() { return this.bootOutput; }
    public void close() throws Exception {
        this.families.close();
        this.magicLink.close();
        this.remoteStorage.close();
        this.localStorage.close();
        this.eventManager.close();
        this.bootOutput.clear();
    }

    public static class Tinder extends Particle.Tinder<ProxyFlame> {
        private final UUID uuid;
        private final Version version;
        private final ProxyAdapter adapter;
        private final ProxyLangLibrary.Tinder lang;
        private final Whitelist.Tinder whitelist;
        private final Families.Tinder families;
        private final MagicLink.Tinder magicLink;
        private final RemoteStorage.Tinder remoteStorage;
        private final LocalStorage localStorage;
        private final EventManager eventManager;

        public Tinder(
                @NotNull UUID uuid,
                @NotNull Version version,
                @NotNull ProxyAdapter<?, ?> adapter,
                @NotNull ProxyLangLibrary.Tinder lang,
                @Nullable Whitelist.Tinder whitelist,
                @NotNull Families.Tinder families,
                @NotNull MagicLink.Tinder magicLink,
                @NotNull RemoteStorage.Tinder remoteStorage,
                @NotNull LocalStorage localStorage,
                @NotNull EventManager eventManager
        ) {
            super();
            this.uuid = uuid;
            this.version = version;
            this.adapter = adapter;
            this.lang = lang;
            this.whitelist = whitelist;
            this.families = families;
            this.magicLink = magicLink;
            this.remoteStorage = remoteStorage;
            this.localStorage = localStorage;
            this.eventManager = eventManager;
        }

        public @NotNull ProxyFlame ignite() throws RuntimeException {
            this.eventManager.on(FamilyLeaveEvent.class, new OnFamilyLeave());
            this.eventManager.on(MCLoaderRegisterEvent.class, new OnMCLoaderRegister());
            this.eventManager.on(MCLoaderUnregisterEvent.class, new OnMCLoaderUnregister());
            this.eventManager.on(MCLoaderLeaveEvent.class, new OnMCLoaderLeave());

            Particle.Flux<Whitelist> whitelistFlux = null;
            if(this.whitelist != null) whitelistFlux = this.whitelist.flux();

            return new ProxyFlame(
                    this.uuid,
                    this.version,
                    this.adapter,
                    this.lang.flux(),
                    whitelistFlux,
                    new ArrayList<>(),
                    this.families.flux(),
                    this.magicLink.flux(),
                    this.remoteStorage.flux(),
                    this.localStorage,
                    this.eventManager
            );
        }
    }
}
