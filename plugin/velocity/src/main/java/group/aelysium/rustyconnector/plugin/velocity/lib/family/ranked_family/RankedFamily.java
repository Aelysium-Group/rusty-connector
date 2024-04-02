package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family;

import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.configs.RankedFamilyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.configs.MatchMakerConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.RoundRobin;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.Matchmaker;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.Party;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageService;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.WhitelistService;
import group.aelysium.rustyconnector.toolkit.velocity.connection.ConnectionResult;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.family.ranked_family.IRankedFamily;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.server.IRankedMCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import group.aelysium.rustyconnector.toolkit.velocity.whitelist.IWhitelist;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static group.aelysium.rustyconnector.toolkit.velocity.family.Metadata.RANKED_FAMILY_META;
import static group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector.inject;

public class RankedFamily extends Family implements IRankedFamily {
    protected final Matchmaker matchmaker;

    protected RankedFamily(Settings settings, RoundRobin roundRobin) {
        super(settings.id(), new Family.Settings(settings.displayName(), roundRobin, settings.parentFamily(), settings.whitelist(), new Connector(roundRobin, settings.matchmaker(), settings.whitelist())), RANKED_FAMILY_META);
        this.matchmaker = settings.matchmaker();
    }

    public boolean dequeue(IPlayer player) {
        return this.matchmaker.remove(player);
    }

    /**
     * Start the family's matchmaking system.
     */
    public void start() {
        this.matchmaker.start(this.loadBalancer());
    }

    public void addServer(@NotNull IMCLoader server) {
        if(!(server instanceof IRankedMCLoader)) throw new IllegalArgumentException();
        this.settings.loadBalancer().add(server);
    }

    public void removeServer(@NotNull IMCLoader server) {
        if(!(server instanceof IRankedMCLoader)) throw new IllegalArgumentException();
        this.settings.loadBalancer().remove(server);
    }

    @Override
    public long playerCount() {
        return this.matchmaker.playerCount();
    }

    public Matchmaker matchmaker() {
        return this.matchmaker;
    }

    public int queuedPlayers() {
        return this.matchmaker.queuedPlayerCount();
    }

    public long activePlayers() {
        return super.playerCount();
    }

    @Override
    public void leave(IPlayer player) {
        this.settings.connector().leave(player);
    }

    /**
     * Initializes all server families based on the configs.
     * By the time this runs, the configuration file should be able to guarantee that all values are present.
     * @return A list of all server families.
     */
    public static RankedFamily init(DependencyInjector.DI5<List<Component>, LangService, StorageService, WhitelistService, ConfigService> deps, String familyName) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        Tinder api = Tinder.get();
        List<Component> bootOutput = deps.d1();
        LangService lang = deps.d2();
        StorageService storage = deps.d3();
        WhitelistService whitelistService = deps.d4();

        RankedFamilyConfig config = RankedFamilyConfig.construct(api.dataFolder(), familyName, lang, deps.d5());

        MatchMakerConfig matchMakerConfig = MatchMakerConfig.construct(api.dataFolder(), config.matchmaker_name(), lang, deps.d5());

        Matchmaker matchmaker = new Matchmaker(matchMakerConfig.settings(), storage, config.gameId());

        Whitelist.Reference whitelist = null;
        if (config.isWhitelist_enabled())
            whitelist = Whitelist.init(inject(bootOutput, lang, whitelistService, deps.d5()), config.getWhitelist_name());

        Settings settings = new Settings(familyName, config.displayName(), config.getParent_family(), whitelist, matchmaker);
        return new RankedFamily(settings, new RoundRobin(new LoadBalancer.Settings(false, false, 0)));
    }

    @Override
    public void kill() {
        this.matchmaker.kill();
    }

    public record Settings(
            String id,
            String displayName,
            Family.Reference parentFamily,
            Whitelist.Reference whitelist,
            Matchmaker matchmaker
    ) {}

    public static class Connector extends IFamily.Connector.Core {
        protected final Matchmaker matchmaker;

        public Connector(@NotNull LoadBalancer loadBalancer, @NotNull Matchmaker matchmaker, IWhitelist.Reference whitelist) {
            super(loadBalancer, whitelist);

            this.matchmaker = matchmaker;
        }

        @Override
        public void leave(IPlayer player) {
            this.matchmaker.remove(player);
        }

        @Override
        public Request connect(IPlayer player) {
            CompletableFuture<ConnectionResult> result = new CompletableFuture<>();
            Request request = new Request(player, result);

            if(Party.locate(player).isPresent()) {
                result.complete(ConnectionResult.failed(ProxyLang.RANKED_FAMILY_PARTY_DENIAL.build()));
                return request;
            }

            this.matchmaker.queue(request, result);

            return request;
        }
    }
}