package group.aelysium.rustyconnector.plugin.velocity.lib.config;

import group.aelysium.rustyconnector.core.lib.messenger.config.ConnectorsConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.configs.DataTransitConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.configs.DefaultConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.configs.LoggerConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.config.DynamicTeleportConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.config.FamiliesConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.config.FriendsConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.config.MagicMCLoaderConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.config.MatchMakerConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.config.PartyConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.webhook.config.WebhooksConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.config.WhitelistConfig;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

import java.util.HashMap;
import java.util.Map;

public class ConfigService implements Service {
    protected DefaultConfig defaultConfig;
    protected FamiliesConfig families;
    protected ConnectorsConfig connectors;
    protected Map<String, FamiliesConfig> familiesIndividual = new HashMap<>();
    protected Map<String, LoadBalancer> loadBalancers = new HashMap<>();
    protected Map<String, MagicMCLoaderConfig> magicLinkConfigs = new HashMap<>();
    protected Map<String, MatchMakerConfig> matchmakers = new HashMap<>();
    protected Map<String, WhitelistConfig> whitelists = new HashMap<>();
    protected DataTransitConfig dataTransit;
    protected DynamicTeleportConfig dynamicTeleport;
    protected FriendsConfig friends;
    protected LoggerConfig logger;
    protected PartyConfig party;
    protected WebhooksConfig webhooks;



    @Override
    public void kill() {

    }
}
