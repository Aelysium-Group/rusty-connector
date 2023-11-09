package group.aelysium.rustyconnector.toolkit.velocity.players;

import com.velocitypowered.api.proxy.Player;

import java.util.Optional;
import java.util.UUID;

public interface IResolvablePlayer {
    UUID uuid();
    String username();
    Optional<Player> resolve();
}