package group.aelysium.rustyconnector.api.velocity.lib.players;

import com.velocitypowered.api.proxy.Player;

import java.util.Optional;
import java.util.UUID;

public interface IResolvablePlayer {
    UUID uuid();
    String username();
    Optional<Player> resolve();
}