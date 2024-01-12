package group.aelysium.rustyconnector.plugin.velocity.lib.config.configs;

import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigService;
import group.aelysium.rustyconnector.toolkit.core.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.core.config.IYAML;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import group.aelysium.rustyconnector.toolkit.velocity.parties.ServerOverflowHandler;
import group.aelysium.rustyconnector.toolkit.velocity.parties.SwitchPower;
import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import net.kyori.adventure.text.format.NamedTextColor;

import java.nio.file.Path;

public class PartyConfig extends YAML implements group.aelysium.rustyconnector.toolkit.velocity.config.PartyConfig {
    private boolean enabled = false;
    private int maxMembers = 5;

    private boolean friendsOnly = false;
    private boolean localOnly = false;

    private boolean partyLeader_onlyLeaderCanInvite = true;
    private boolean partyLeader_onlyLeaderCanKick = true;
    private boolean partyLeader_onlyLeaderCanSwitchServers = true;
    private boolean partyLeader_disbandOnLeaderQuit = true;

    private SwitchPower switchingServers_switchPower = SwitchPower.MODERATE;

    private ServerOverflowHandler switchingServers_overflowHandler = ServerOverflowHandler.HARD_BLOCK;

    @Override
    public IConfigService.ConfigKey key() {
        return IConfigService.ConfigKey.singleton(PartyConfig.class);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getMaxMembers() {
        return maxMembers;
    }

    public boolean isFriendsOnly() {
        return friendsOnly;
    }
    public boolean isLocalOnly() {
        return localOnly;
    }

    public boolean isPartyLeader_onlyLeaderCanInvite() {
        return partyLeader_onlyLeaderCanInvite;
    }

    public boolean isPartyLeader_onlyLeaderCanKick() {
        return partyLeader_onlyLeaderCanKick;
    }

    public boolean isPartyLeader_onlyLeaderCanSwitchServers() {
        return partyLeader_onlyLeaderCanSwitchServers;
    }

    public boolean isPartyLeader_disbandOnLeaderQuit() {
        return partyLeader_disbandOnLeaderQuit;
    }

    public SwitchPower getSwitchingServers_switchPower() {
        return switchingServers_switchPower;
    }

    public ServerOverflowHandler getSwitchingServers_overflowHandler() {
        return switchingServers_overflowHandler;
    }

    protected PartyConfig(Path dataFolder, String target, String name, LangService lang) {
        super(dataFolder, target, name, lang, LangFileMappings.PROXY_PARTY_TEMPLATE);
    }

    @SuppressWarnings("unchecked")
    protected void register() throws IllegalStateException, NoOutputException {
        this.enabled = IYAML.getValue(this.data, "enabled", Boolean.class);
        if(!this.enabled) return;

        this.maxMembers = IYAML.getValue(this.data, "max-members", Integer.class);

        try {
            this.friendsOnly = IYAML.getValue(this.data, "friends-only", Boolean.class);
            if(this.friendsOnly)
                Tinder.get().services().friends().orElseThrow();
        } catch (Exception ignore) {
            Tinder.get().logger().send(ProxyLang.BOXED_MESSAGE_COLORED.build("[friends-only] in `party.yml` is set to true. But the friends module isn't enabled! Ignoring...", NamedTextColor.YELLOW));
            this.friendsOnly = false;
        }
        this.localOnly = IYAML.getValue(this.data, "local-only", Boolean.class);

        this.partyLeader_onlyLeaderCanInvite = IYAML.getValue(this.data, "party-leader.only-leader-can-invite", Boolean.class);
        this.partyLeader_onlyLeaderCanKick = IYAML.getValue(this.data, "party-leader.only-leader-can-kick", Boolean.class);
        this.partyLeader_onlyLeaderCanSwitchServers = IYAML.getValue(this.data, "party-leader.only-leader-can-switch-servers", Boolean.class);
        this.partyLeader_disbandOnLeaderQuit = IYAML.getValue(this.data, "party-leader.disband-on-leader-quit", Boolean.class);

        try {
            this.switchingServers_switchPower = Enum.valueOf(SwitchPower.class, IYAML.getValue(this.data, "switching-servers.switch-power", String.class));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Switch power isn't valid! Please review `party.yml` again!");
        }

        try {
            this.switchingServers_overflowHandler = ServerOverflowHandler.valueOf(IYAML.getValue(this.data, "switching-servers.on-server-overflow", String.class));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Overflow handler isn't valid! Please review `party.yml` again!");
        }
    }

    public static PartyConfig construct(Path dataFolder, LangService lang, ConfigService configService) {
        PartyConfig config = new PartyConfig(dataFolder, "extras/party.yml", "party", lang);
        config.register();
        configService.put(config);
        return config;
    }
}
