package group.aelysium.rustyconnector.proxy.magic_link;

import group.aelysium.rustyconnector.toolkit.RC;
import group.aelysium.rustyconnector.toolkit.common.cache.MessageCache;
import group.aelysium.rustyconnector.toolkit.common.crypt.AESCryptor;
import group.aelysium.rustyconnector.common.magic_link.MagicLinkCore;
import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;
import group.aelysium.rustyconnector.toolkit.common.magic_link.IMagicLink;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.proxy.family.Family;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MagicLink extends MagicLinkCore implements IMagicLink.Proxy {
    protected final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    protected Map<String, MagicLinkMCLoaderSettings> settingsMap;

    protected MagicLink(
            @NotNull AESCryptor cryptor,
            @NotNull MessageCache cache,
            @NotNull IPacket.Target self,
            @NotNull Map<String, MagicLinkMCLoaderSettings> magicLinkMCLoaderSettingsMap
    ) {
        super(cryptor, cache, self);
        this.settingsMap = magicLinkMCLoaderSettingsMap;
        this.heartbeat();
    }

    private void heartbeat() {
        this.executor.schedule(() -> {
            try {
                RC.P.Families().dump().forEach(f -> {
                    try {
                        Family family = f.orElseThrow();
                        family.mcloaders().forEach(mcLoader -> {
                            mcLoader.decreaseTimeout(3);

                            try {
                                if (mcLoader.stale()) family.deleteMCLoader(mcLoader);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception ignore) {}

            this.heartbeat();
        }, 3, TimeUnit.SECONDS);
    }

    public Optional<MagicLinkMCLoaderSettings> magicConfig(String name) {
        MagicLinkMCLoaderSettings settings = this.settingsMap.get(name);
        if(settings == null) return Optional.empty();
        return Optional.of(settings);
    }

    @Override
    public void close() throws Exception {
        super.close();
        this.executor.shutdownNow();
    }

    public static class Tinder extends Particle.Tinder<IMagicLink.Proxy> {
        private final AESCryptor cryptor;
        private final IPacket.Target self;
        private final Map<String, MagicLinkMCLoaderSettings> magicConfigs;
        public Tinder(
                @NotNull AESCryptor cryptor,
                @NotNull IPacket.Target self,
                @NotNull Map<String, MagicLinkMCLoaderSettings> magicConfigs
                ) {
            this.cryptor = cryptor;
            this.self = self;
            this.magicConfigs = magicConfigs;
        }

        @Override
        public @NotNull IMagicLink.Proxy ignite() throws Exception {
            return new MagicLink(
                    cryptor,
                    new MessageCache(50),
                    this.self,
                    this.magicConfigs
            );
        }

    }
}
