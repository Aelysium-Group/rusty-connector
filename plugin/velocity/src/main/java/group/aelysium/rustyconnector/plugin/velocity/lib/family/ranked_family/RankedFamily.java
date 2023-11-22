package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family;

import group.aelysium.rustyconnector.plugin.velocity.lib.family.FamilyReference;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.config.RankedFamilyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.RustyPlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.SystemFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.events.RankedFamilyEventFactory;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class RankedFamily extends SystemFocusedServerFamily {
    protected RankedFamilyEventFactory eventManager = new RankedFamilyEventFactory();
    protected RankedGameManager gameManager;

    protected RankedFamily(Settings settings) {
        super(settings.name(), null, settings.parentFamily());
        this.gameManager = new RankedGameManager(settings.matchmakerSettings(), this);
    }

    public RankedGameManager gameManager() {
        return this.gameManager;
    }

    /**
     * Queues a player into this family's matchmaking.
     * The player will be connected once a match has been made.
     * The player's queue to this matchmaker will not timeout.
     * You must manually call {@link #dequeueConnect(RustyPlayer)} to remove a player from this queue.
     * @param player The player to connect.
     * @return null. Always.
     */
    public PlayerServer connect(RustyPlayer player) {
        this.gameManager.playerQueue().add(player.ranked(this.gameManager().name()));
        return null;
    }

    /**
     * Dequeues a player from this family's matchmaking.
     * If the player is already connected to this family, nothing will happen.
     * @param player The player to dequeue.
     */
    public void dequeueConnect(RustyPlayer player) {
        this.gameManager.playerQueue().remove(player.ranked(this.gameManager().name()));
    }

    /**
     * Initializes all server families based on the configs.
     * By the time this runs, the configuration file should be able to guarantee that all values are present.
     * @return A list of all server families.
     */
    public static RankedFamily init(DependencyInjector.DI2<List<Component>, LangService> dependencies, String familyName) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        Tinder api = Tinder.get();

        RankedFamilyConfig config = new RankedFamilyConfig(api.dataFolder(), familyName);
        if(!config.generate(dependencies.d1(), dependencies.d2(), LangFileMappings.VELOCITY_RANKED_FAMILY_TEMPLATE)) {
            throw new IllegalStateException("Unable to load or create families/"+familyName+".scalar.yml!");
        }
        config.register(familyName);

        Whitelist whitelist;
        if(config.isWhitelist_enabled()) {
            whitelist = Whitelist.init(dependencies, config.getWhitelist_name());

            api.services().whitelist().add(whitelist);
        }

        Settings settings = new Settings(familyName, config.getParent_family(), config.getMatchmakingSettings());
        return new RankedFamily(settings);
    }

    public record Settings(String name, FamilyReference parentFamily, RankedMatchmaker.Settings matchmakerSettings) {}
}