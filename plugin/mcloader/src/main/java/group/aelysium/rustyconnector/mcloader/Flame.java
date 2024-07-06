package group.aelysium.rustyconnector.mcloader;

import group.aelysium.rustyconnector.common.events.EventManager;
import group.aelysium.rustyconnector.common.magic_link.Packet;
import group.aelysium.rustyconnector.common.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.common.packets.SendPlayerPacket;
import group.aelysium.rustyconnector.mcloader.magic_link.MagicLink;
import group.aelysium.rustyconnector.toolkit.common.magic_link.IMagicLink;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.mc_loader.IMCLoaderFlame;
import group.aelysium.rustyconnector.toolkit.mc_loader.MCLoaderAdapter;
import group.aelysium.rustyconnector.toolkit.mc_loader.lang.MCLoaderLangLibrary;
import group.aelysium.rustyconnector.toolkit.proxy.util.Version;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.*;

public class Flame implements IMCLoaderFlame {
    private final UUID uuid;
    private final Version version;
    private final MCLoaderAdapter adapter;
    private final Flux<MCLoaderLangLibrary> lang;
    private final String displayName;
    private final InetSocketAddress address;
    private final Flux<IMagicLink.MCLoader> magicLink;
    private final EventManager eventManager;

    protected Flame(
            @NotNull UUID uuid,
            @NotNull Version version,
            @NotNull MCLoaderAdapter adapter,
            @NotNull Flux<MCLoaderLangLibrary> lang,
            @NotNull String displayName,
            @NotNull InetSocketAddress address,
            @NotNull Flux<IMagicLink.MCLoader> magicLink,
            @NotNull EventManager eventManager
    ) {
        this.uuid = uuid;
        this.version = version;
        this.adapter = adapter;
        this.lang = lang;
        this.displayName = displayName;
        this.address = address;
        this.magicLink = magicLink;
        this.eventManager = eventManager;
    }

    @Override
    public UUID uuid() {
        return this.uuid;
    }

    @Override
    public Version version() {
        return this.version;
    }

    @Override
    public String displayName() {
        return this.displayName;
    }

    @Override
    public InetSocketAddress address() {
        return this.address;
    }

    @Override
    public int playerCount() {
        return this.adapter.onlinePlayerCount();
    }

    @Override
    public void lock() {
        Packet.New()
                .identification(BuiltInIdentifications.LOCK_SERVER)
                .addressedTo(IPacket.Target.allAvailableProxies())
                .send();
    }

    @Override
    public void unlock() {
        Packet.New()
                .identification(BuiltInIdentifications.UNLOCK_SERVER)
                .addressedTo(IPacket.Target.allAvailableProxies())
                .send();
    }

    @Override
    public void send(UUID player, String familyID) {
        Packet.New()
                .identification(BuiltInIdentifications.SEND_PLAYER)
                .parameter(SendPlayerPacket.Parameters.PLAYER_UUID, player.toString())
                .parameter(SendPlayerPacket.Parameters.TARGET_FAMILY_NAME, familyID)
                .addressedTo(IPacket.Target.allAvailableProxies())
                .send();
    }

    @Override
    public void send(UUID player, UUID mcloader) {

    }

    @Override
    public Flux<IMagicLink.MCLoader> MagicLink() {
        return this.magicLink;
    }

    public MCLoaderAdapter Adapter() {
        return this.adapter;
    }

    public Flux<MCLoaderLangLibrary> Lang() {
        return this.lang;
    }

    public EventManager EventManager() {
        return this.eventManager;
    }

    @Override
    public void close() throws Exception {
        this.magicLink.close();
    }

    public static class Tinder extends IMCLoaderFlame.Tinder {
        private final UUID uuid;
        private final Version version;
        private final MCLoaderAdapter adapter;
        private final MCLoaderLangLibrary.Tinder lang;
        private final String displayName;
        private final InetSocketAddress address;
        private final MagicLink.Tinder magicLink;
        private final EventManager eventManager;

        public Tinder(
                @NotNull UUID uuid,
                @NotNull Version version,
                @NotNull MCLoaderAdapter adapter,
                @NotNull MCLoaderLangLibrary.Tinder lang,
                @NotNull String displayName,
                @NotNull InetSocketAddress address,
                @NotNull MagicLink.Tinder magicLink,
                @NotNull EventManager eventManager
                ) {
            this.uuid = uuid;
            this.version = version;
            this.adapter = adapter;
            this.lang = lang;
            this.displayName = displayName;
            this.address = address;
            this.magicLink = magicLink;
            this.eventManager = eventManager;
        }

        @Override
        public @NotNull IMCLoaderFlame ignite() throws Exception {
            return new Flame(
                    uuid,
                    version,
                    adapter,
                    lang.flux(),
                    displayName,
                    address,
                    magicLink.flux(),
                    eventManager
            );
        }
    }
}