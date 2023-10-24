package group.aelysium.rustyconnector.api.velocity.parties;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.api.velocity.players.IResolvablePlayer;
import group.aelysium.rustyconnector.api.velocity.server.IPlayerServer;
import group.aelysium.rustyconnector.api.core.serviceable.interfaces.Service;

import java.util.List;
import java.util.Optional;

public interface IPartyService<TResolvablePlayer extends IResolvablePlayer, TPlayerServer extends IPlayerServer, TParty extends IParty<TPlayerServer>, TPartyInvite extends IPartyInvite<TResolvablePlayer>> extends Service {
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
    TParty create(Player host, TPlayerServer server);

    /**
     * Deletes an existing {@link IParty}.
     * The {@link IParty} in question will first be decomposed and then removed from the Party service.
     * Once delete has been called, no other references to this party should be made.
     * @param party The party to delete.
     */
    void delete(TParty party);

    /**
     * Find a party based on its member.
     * @return A party.
     */
    Optional<TParty> find(Player member);

    /**
     * Gracefully deletes a party.
     * Disband will notify all the party's members that their server has been disbanded.
     * It will then remove the players, and call {@link IPartyService#delete(IParty)}.
     * @param party The party to disband.
     */
    void disband(TParty party);

    /**
     * Invites a player to join the party.
     * @param party The party to join.
     * @param sender The player sending the invite.
     * @param target The player receiving the invite.
     * @return {@link IPartyInvite}
     */
    TPartyInvite invitePlayer(TParty party, Player sender, Player target);

    /**
     * Fetches the party invites which have been made to a specific player.
     * @param target The player who has received the invites.
     * @return {@link List<IPartyInvite>}
     */
    List<TPartyInvite> findInvitesToTarget(TResolvablePlayer target);

    /**
     * Fetches a specific party invite based on the two players involved in it.
     * @param target The player who received the invite.
     * @param sender The player who sent the invite.
     * @return {@link Optional<IPartyInvite>}
     */
    Optional<TPartyInvite> findInvite(TResolvablePlayer target, TResolvablePlayer sender);

    /**
     * Closes an invitation.
     * This method will remove the invite from the party service and decompose it.
     * @param invite The invite to close.
     */
    void closeInvite(TPartyInvite invite);

    /**
     * Dumps all currently open parties and returns them as a list.
     * @return {@link List<IParty>}
     */
    List<TParty> dump();
}