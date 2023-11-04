package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.api.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.api.velocity.load_balancing.AlgorithmType;
import group.aelysium.rustyconnector.api.velocity.util.DependencyInjector;
import group.aelysium.rustyconnector.api.velocity.util.LiquidTimestamp;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.SystemFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players.PlayerRankLadder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family.config.ScalarFamilyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.events.RankedFamilyEventFactory;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LeastConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.MostConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.RoundRobin;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class RankedFamily extends SystemFocusedServerFamily {
    protected RankedFamilyEventFactory eventManager = new RankedFamilyEventFactory();
    protected RankedGameManager gameManager;
    protected PlayerRankLadder playerQueue;

    protected RankedFamily(String name, LoadBalancer loadBalancer, String parentFamily) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super(name, loadBalancer, parentFamily);
    }

    public RankedGameManager gameManager() {
        return this.gameManager;
    }

    public PlayerRankLadder playerQueue() {
        return this.playerQueue;
    }

    /**
     * Queues a player into this family's matchmaking.
     * The player will be connected once a match has been made.
     * If the player can't be matchmade before the timeout, they will dequeue from the family.
     * @param player The player to connect.
     */
    public void timeoutQueueConnect(Player player, LiquidTimestamp timeout) {
    }

    /**
     * Queues a player into this family's matchmaking.
     * The player will be connected once a match has been made.
     * The player's queue to this matchmaker will not timeout.
     * You must manually call {@link #dequeueConnect(Player)} or use {@link #timeoutQueueConnect(Player, LiquidTimestamp)} to remove a player form this queue.
     * @param player The player to connect.
     */
    public void queueConnect(Player player) {
    }

    /**
     * Dequeues a player from this family's matchmaking.
     * If the player is already connected to this family, nothing will happen.
     * @param player The player to dequeue.
     */
    public void dequeueConnect(Player player) {
    }

    /**
     * Initializes all server families based on the configs.
     * By the time this runs, the configuration file should be able to guarantee that all values are present.
     * @return A list of all server families.
     */
    public static RankedFamily init(DependencyInjector.DI2<List<Component>, LangService> dependencies, String familyName) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        Tinder api = Tinder.get();

        ScalarFamilyConfig scalarFamilyConfig = new ScalarFamilyConfig(new File(String.valueOf(api.dataFolder()), "families/"+familyName+".scalar.yml"));
        if(!scalarFamilyConfig.generate(dependencies.d1(), dependencies.d2(), LangFileMappings.VELOCITY_SCALAR_FAMILY_TEMPLATE)) {
            throw new IllegalStateException("Unable to load or create families/"+familyName+".scalar.yml!");
        }
        scalarFamilyConfig.register();

        Whitelist whitelist = null;
        if(scalarFamilyConfig.isWhitelist_enabled()) {
            whitelist = Whitelist.init(dependencies, scalarFamilyConfig.getWhitelist_name());

            api.services().whitelist().add(whitelist);
        }

        LoadBalancer loadBalancer;
        switch (Enum.valueOf(AlgorithmType.class, scalarFamilyConfig.getLoadBalancing_algorithm())) {
            case ROUND_ROBIN -> loadBalancer = new RoundRobin(
                    scalarFamilyConfig.isLoadBalancing_weighted(),
                    scalarFamilyConfig.isLoadBalancing_persistence_enabled(),
                    scalarFamilyConfig.getLoadBalancing_persistence_attempts()
            );
            case LEAST_CONNECTION -> loadBalancer = new LeastConnection(
                    scalarFamilyConfig.isLoadBalancing_weighted(),
                    scalarFamilyConfig.isLoadBalancing_persistence_enabled(),
                    scalarFamilyConfig.getLoadBalancing_persistence_attempts()
            );
            case MOST_CONNECTION -> loadBalancer = new MostConnection(
                    scalarFamilyConfig.isLoadBalancing_weighted(),
                    scalarFamilyConfig.isLoadBalancing_persistence_enabled(),
                    scalarFamilyConfig.getLoadBalancing_persistence_attempts()
            );
            default -> throw new RuntimeException("The name used for "+familyName+"'s load balancer is invalid!");
        }

        return new RankedFamily(
                familyName,
                loadBalancer,
                scalarFamilyConfig.getParent_family()
        );
    }
}