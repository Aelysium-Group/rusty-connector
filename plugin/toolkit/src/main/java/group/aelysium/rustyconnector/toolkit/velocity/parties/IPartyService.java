package group.aelysium.rustyconnector.toolkit.velocity.parties;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

import java.util.List;
import java.util.Optional;

public interface IPartyService extends Service {
    /**
     * Gets the settings that this {@link IPartyService} abides by.
     * @return {@link PartyServiceSettings}
     */
    PartyServiceSettings settings();

    /**
     * Creates a new {@link IParty party}.
     * @param host The host of the party.
     * @param server The server that the party is currently residing in.
     * @return {@link IParty}
     */
    IParty create(IPlayer host, IMCLoader server);

    /**
     * Deletes an existing {@link IParty}.
     * The {@link IParty} in question will first be decomposed and then removed from the Party service.
     * Once delete has been called, no other references to this party should be made.
     * @param party The party to delete.
     */
    void delete(IParty party);

    /**
     * Find a party based on its member.
     * @return A party.
     */
    Optional<IParty> find(IPlayer member);

    /**
     * Gracefully deletes a party.
     * Disband will notify all the party's members that their server has been disbanded.
     * It will then remove the players, and call {@link IPartyService#delete(IParty)}.
     * @param party The party to disband.
     */
    void disband(IParty party);

    /**
     * Invites a player to join the party.
     * @param party The party to join.
     * @param sender The player sending the invite.
     * @param target The player receiving the invite.
     * @return {@link IPartyInvite}
     */
    IPartyInvite invitePlayer(IParty party, IPlayer sender, IPlayer target);

    /**
     * Fetches the party invites which have been made to a specific player.
     * @param target The player who has received the invites.
     * @return {@link List< IPartyInvite >}
     */
    List<IPartyInvite> findInvitesToTarget(IPlayer target);

    /**
     * Fetches a specific party invite based on the two players involved in it.
     * @param target The player who received the invite.
     * @param sender The player who sent the invite.
     * @return {@link Optional< IPartyInvite >}
     */
    Optional<IPartyInvite> findInvite(IPlayer target, IPlayer sender);

    /**
     * Closes an invitation.
     * This method will remove the invite from the party service and decompose it.
     * @param invite The invite to close.
     */
    void closeInvite(IPartyInvite invite);

    /**
     * Dumps all currently open parties and returns them as a list.
     * @return {@link List< IParty >}
     */
    List<IParty> dump();
}