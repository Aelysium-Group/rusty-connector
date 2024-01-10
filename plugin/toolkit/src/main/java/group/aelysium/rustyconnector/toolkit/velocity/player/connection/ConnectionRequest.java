package group.aelysium.rustyconnector.toolkit.velocity.player.connection;

import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.Future;

public record ConnectionRequest(@NotNull IPlayer player, Future<Result> result) {
    /**
     * The result of the connection request.
     * The returned message is always safe to send directly to the player.
     */
    public record Result(Status status, Component message, Optional<IMCLoader> server) {
        public boolean connected() {
            return this.status == Status.SUCCESS;
        }
        public static Result failed(Component message) {
            return new Result(Status.FAILED, message, Optional.empty());
        }
        public static Result success(Component message, IMCLoader server) {
            return new Result(Status.SUCCESS, message, Optional.of(server));
        }
    }
    public enum Status {
        FAILED,
        SUCCESS
    }
}