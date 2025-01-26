package group.aelysium.rustyconnector.plugin.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.plugin.common.command.Client;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;

public interface VelocityClient {
    class Player extends Client.Player<com.velocitypowered.api.proxy.Player> {
        public Player(@NotNull com.velocitypowered.api.proxy.Player source) {
            super(source);
        }

        public void send(Component message) {
            ((com.velocitypowered.api.proxy.Player) this.source).sendMessage((ComponentLike) message);
        }
        public void send(Error error) {
            ((com.velocitypowered.api.proxy.Player) this.source).sendMessage((ComponentLike) error.toComponent());
        }
    }
    class Console extends Client.Console<ConsoleCommandSource> {
        public Console(@NotNull ConsoleCommandSource source) {
            super(source);
        }

        public void send(Component message) {
            ((ConsoleCommandSource) this.source).sendMessage((ComponentLike) message);
        }
        public void send(Error error) {
            ((ConsoleCommandSource) this.source).sendMessage((ComponentLike) error.toComponent());
        }
    }
    class Other extends Client.Other<CommandSource> {
        public Other(@NotNull CommandSource source) {
            super(source);
        }

        public void send(Component message) {
            ((CommandSource) this.source).sendMessage((ComponentLike) message);
        }
        public void send(Error error) {
            ((CommandSource) this.source).sendMessage((ComponentLike) error.toComponent());
        }
    }
}