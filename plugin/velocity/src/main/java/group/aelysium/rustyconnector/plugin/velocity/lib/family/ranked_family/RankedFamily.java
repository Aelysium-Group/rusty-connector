package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family;

import group.aelysium.rustyconnector.plugin.velocity.event_handlers.EventDispatch;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.configs.RankedFamilyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.configs.MatchMakerConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.RoundRobin;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.matchmakers.Matchmaker;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RankedGame;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageService;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.WhitelistService;
import group.aelysium.rustyconnector.toolkit.velocity.events.player.FamilyPreJoinEvent;
import group.aelysium.rustyconnector.toolkit.velocity.family.ranked_family.IRankedFamily;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.matchmakers.IMatchmaker;
import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.server.IRankedMCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

import static group.aelysium.rustyconnector.toolkit.velocity.family.Metadata.RANKED_FAMILY_META;
import static group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector.inject;

public class RankedFamily extends Family implements IRankedFamily {
    protected final Matchmaker matchmaker;

    protected RankedFamily(Settings settings) {
        super(settings.id(), new Family.Settings(settings.displayName(), new RoundRobin(new LoadBalancer.Settings(false, false, 0)), settings.parentFamily(), settings.whitelist()), RANKED_FAMILY_META);
        this.matchmaker = settings.matchmaker();
    }

    public IMCLoader connect(IPlayer player) {
        EventDispatch.Safe.fireAndForget(new FamilyPreJoinEvent(this, player));

        this.matchmaker.add(player);
        return null;
    }

    public void dequeue(IPlayer player) {
        this.matchmaker.remove(player);
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
        return super.playerCount() + this.waitingPlayers();
    }

    public Matchmaker matchmaker() {
        return this.matchmaker;
    }

    public int waitingPlayers() {
        return this.matchmaker.waitingPlayers().size();
    }

    public long activePlayers() {
        return super.playerCount();
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
        StorageService mySQLStorage = deps.d3();
        WhitelistService whitelistService = deps.d4();

        RankedFamilyConfig config = RankedFamilyConfig.construct(api.dataFolder(), familyName, lang, deps.d5());

        Matchmaker matchmaker;
        {
            MatchMakerConfig matchMakerConfig = MatchMakerConfig.construct(api.dataFolder(), config.matchmaker_name(), lang, deps.d5());

            Optional<RankedGame> fetched = mySQLStorage.database().getGame(config.name());
            if(fetched.isEmpty()) {
                RankedGame game = new RankedGame(config.gamemodeName(), matchMakerConfig.getAlgorithm());
                mySQLStorage.database().saveGame(mySQLStorage, game);

                fetched = Optional.of(game);
            }
            IMatchmaker.Settings matchmakerSettings = new IMatchmaker.Settings(mySQLStorage, matchMakerConfig.getAlgorithm(), fetched.orElseThrow(), matchMakerConfig.min(), matchMakerConfig.max(), matchMakerConfig.getVariance(), matchMakerConfig.getMatchmakingInterval());

            matchmaker = Matchmaker.from(matchmakerSettings);
        }

        Whitelist.Reference whitelist = null;
        if (config.isWhitelist_enabled())
            whitelist = Whitelist.init(inject(bootOutput, lang, whitelistService, deps.d5()), config.getWhitelist_name());

        Settings settings = new Settings(familyName, config.displayName(), config.getParent_family(), whitelist, matchmaker);
        return new RankedFamily(settings);
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
}