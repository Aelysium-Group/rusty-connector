package group.aelysium.rustyconnector.toolkit.velocity.connection;

import group.aelysium.rustyconnector.toolkit.velocity.parties.IParty;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;

public interface PartyConnectable {
    /**
     * Connects the party to the specified resource.
     * This method will never return anything to the party.
     * It is the caller's job to handle outputs.
     * This method should never throw any exceptions.
     * @param party The party to connect.
     * @return A {@link Request} for the player's attempt.
     */
    Request connect(IParty party);

    record Request(@NotNull IParty party, Future<ConnectionResult> result) {}
}
