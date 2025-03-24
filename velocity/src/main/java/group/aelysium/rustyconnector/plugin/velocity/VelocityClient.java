package group.aelysium.rustyconnector.plugin.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.util.CommandClient;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;

public interface VelocityClient {
    class Player extends CommandClient.Player<com.velocitypowered.api.proxy.Player> {
        public Player(@NotNull com.velocitypowered.api.proxy.Player source) {
            super(source);
        }
        
        public void send(Component message) {
            ((com.velocitypowered.api.proxy.Player) this.source).sendMessage((ComponentLike) message);
        }
        public void send(Error error) {
            ((com.velocitypowered.api.proxy.Player) this.source).sendMessage((ComponentLike) error.toComponent());
        }
        
        @Override
        public String id() {
            return ((com.velocitypowered.api.proxy.Player) this.source).getUniqueId().toString();
        }
        
        @Override
        public String username() {
            return ((com.velocitypowered.api.proxy.Player) this.source).getUsername();
        }
    }
    class Console extends CommandClient.Console<ConsoleCommandSource> {
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
    class Other extends CommandClient.Other<CommandSource> {
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