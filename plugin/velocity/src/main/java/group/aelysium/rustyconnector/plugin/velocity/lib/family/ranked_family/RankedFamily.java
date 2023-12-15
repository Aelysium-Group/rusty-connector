package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family;

import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.config.RankedFamilyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.config.MatchMakerConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.matchmakers.Matchmaker;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RankedGame;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.MySQLStorage;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.WhitelistService;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.matchmakers.IMatchmaker;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

import static group.aelysium.rustyconnector.toolkit.velocity.family.Metadata.RANKED_FAMILY_META;
import static group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector.inject;

public class RankedFamily extends Family implements group.aelysium.rustyconnector.toolkit.velocity.family.ranked_family.RankedFamily<MCLoader, Player, LoadBalancer> {
    protected final Matchmaker<? extends IPlayerRank<?>> matchmaker;

    protected RankedFamily(Settings settings) {
        super(settings.id(), new Family.Settings(settings.displayName(), null, settings.parentFamily(), settings.whitelist()), RANKED_FAMILY_META);
        this.matchmaker = settings.matchmaker();
    }

    public MCLoader connect(Player player) {
        this.matchmaker.add(player);
        return null;
    }

    public void dequeue(Player player) {
        this.matchmaker.remove(player);
    }

    /**
     * Start the family's matchmaking system.
     */
    public void start() {
        this.matchmaker.start(this.loadBalancer());
    }

    @Override
    public long playerCount() {
        return super.playerCount() + this.waitingPlayers();
    }

    public Matchmaker<? extends IPlayerRank<?>> matchmaker() {
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
    public static RankedFamily init(DependencyInjector.DI4<List<Component>, LangService, MySQLStorage, WhitelistService> dependencies, String familyName) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        Tinder api = Tinder.get();
        List<Component> bootOutput = dependencies.d1();
        LangService lang = dependencies.d2();
        MySQLStorage mySQLStorage = dependencies.d3();
        WhitelistService whitelistService = dependencies.d4();

        RankedFamilyConfig config = RankedFamilyConfig.construct(api.dataFolder(), familyName, lang);

        Matchmaker<? extends IPlayerRank<?>> matchmaker;
        {
            MatchMakerConfig matchMakerConfig = MatchMakerConfig.construct(api.dataFolder(), config.matchmaker(), lang);

            Optional<RankedGame> fetched = mySQLStorage.root().getGame(config.getName());
            if(fetched.isEmpty()) {
                RankedGame game = new RankedGame(config.gamemodeName(), matchMakerConfig.getAlgorithm());
                mySQLStorage.root().saveGame(mySQLStorage, game);

                fetched = Optional.of(game);
            }
            IMatchmaker.Settings matchmakerSettings = new IMatchmaker.Settings(mySQLStorage, matchMakerConfig.getAlgorithm(), fetched.orElseThrow(), matchMakerConfig.getTeams(), matchMakerConfig.getVariance(), matchMakerConfig.getMatchmakingInterval());

            matchmaker = Matchmaker.from(matchmakerSettings);
        }

        Whitelist.Reference whitelist = null;
        if (config.isWhitelist_enabled())
            whitelist = Whitelist.init(inject(bootOutput, lang, whitelistService), config.getWhitelist_name());

        Settings settings = new Settings(familyName, config.displayName(), config.getParent_family(), whitelist, matchmaker);
        return new RankedFamily(settings);
    }

    @Override
    public void kill() {
        this.matchmaker.kill();
    }

    public record Settings(
            String id,
            Component displayName,
            Family.Reference parentFamily,
            Whitelist.Reference whitelist,
            Matchmaker<? extends IPlayerRank<?>> matchmaker
    ) {}
}