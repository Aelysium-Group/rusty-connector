package group.aelysium.rustyconnector.toolkit.velocity.parties;

public record PartyServiceSettings(
        int maxMembers,
        boolean friendsOnly,
        boolean localOnly,
        boolean onlyLeaderCanInvite,
        boolean onlyLeaderCanKick,
        boolean onlyLeaderCanSwitchServers,
        boolean disbandOnLeaderQuit,
        SwitchPower switchPower,
        ServerOverflowHandler overflowHandler
) {}