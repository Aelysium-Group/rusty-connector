package group.aelysium.rustyconnector.plugin.common.command;

import group.aelysium.rustyconnector.common.errors.Error;
import net.kyori.adventure.text.Component;

public interface Client<T> {
    /**
     * Enforces that the client is a console client.
     * @throws RuntimeException If the client isn't a console.
     */
    void enforceConsole() throws RuntimeException;

    /**
     * Enforces that the client is a player client.
     * @throws RuntimeException If the client isn't a player.
     */
    void enforcePlayer() throws RuntimeException;
    void send(Component message);
    void send(Error error);
    T toSender();
}
