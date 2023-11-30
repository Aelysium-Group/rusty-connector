package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family;

import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.config.RankedFamilyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.WhitelistService;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.events.RankedFamilyEventFactory;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static group.aelysium.rustyconnector.toolkit.velocity.family.Metadata.RANKED_FAMILY_META;
import static group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector.inject;

public class RankedFamily extends Family {
    protected RankedFamilyEventFactory eventManager = new RankedFamilyEventFactory();
    protected RankedGameManager gameManager;

    protected RankedFamily(Settings settings) {
        super(settings.id(), new Family.Settings(settings.displayName(), null, settings.parentFamily(), settings.whitelist()), RANKED_FAMILY_META);
        this.gameManager = new RankedGameManager(settings.matchmakerSettings(), this);
    }

    public RankedGameManager gameManager() {
        return this.gameManager;
    }

    /**
     * Queues a player into this family's matchmaking.
     * The player will be connected once a match has been made.
     * The player's queue to this matchmaker will not timeout.
     * You must manually call {@link #dequeueConnect(Player)} to remove a player from this queue.
     * @param player The player to connect.
     * @return null. Always.
     */
    public MCLoader connect(Player player) {
        this.gameManager.playerQueue().add(player.ranked(this.gameManager().name()));
        return null;
    }

    /**
     * Dequeues a player from this family's matchmaking.
     * If the player is already connected to this family, nothing will happen.
     * @param player The player to dequeue.
     */
    public void dequeueConnect(Player player) {
        this.gameManager.playerQueue().remove(player.ranked(this.gameManager().name()));
    }

    /**
     * Initializes all server families based on the configs.
     * By the time this runs, the configuration file should be able to guarantee that all values are present.
     * @return A list of all server families.
     */
    public static RankedFamily init(DependencyInjector.DI3<List<Component>, LangService, WhitelistService> dependencies, String familyName) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        Tinder api = Tinder.get();
        List<Component> bootOutput = dependencies.d1();
        LangService lang = dependencies.d2();
        WhitelistService whitelistService = dependencies.d3();

        RankedFamilyConfig config = new RankedFamilyConfig(api.dataFolder(), familyName);
        if(!config.generate(dependencies.d1(), dependencies.d2(), LangFileMappings.VELOCITY_RANKED_FAMILY_TEMPLATE)) {
            throw new IllegalStateException("Unable to load or create families/"+familyName+".scalar.yml!");
        }
        config.register(familyName);

        Whitelist.Reference whitelist = null;
        if (config.isWhitelist_enabled())
            whitelist = Whitelist.init(inject(bootOutput, lang, whitelistService), config.getWhitelist_name());

        Settings settings = new Settings(familyName, config.displayName(), config.getParent_family(), whitelist, config.getMatchmakingSettings());
        return new RankedFamily(settings);
    }

    public record Settings(
            String id,
            Component displayName,
            Family.Reference parentFamily,
            Whitelist.Reference whitelist,
            RankedMatchmaker.Settings matchmakerSettings
    ) {}
}