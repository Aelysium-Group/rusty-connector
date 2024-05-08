package group.aelysium.rustyconnector.toolkit.velocity.config;

import group.aelysium.rustyconnector.toolkit.velocity.parties.SwitchPower;

public interface PartyConfig {
    boolean isEnabled();
    int getMaxMembers();
    boolean isFriendsOnly();
    boolean isLocalOnly();
    boolean isPartyLeader_onlyLeaderCanInvite();
    boolean isPartyLeader_onlyLeaderCanKick();
    boolean isPartyLeader_onlyLeaderCanSwitchServers();
    boolean isPartyLeader_disbandOnLeaderQuit();
    SwitchPower getSwitchingServers_switchPower();
}
