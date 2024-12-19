package group.aelysium.rustyconnector.plugin.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.plugin.common.command.Client;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface VelocityClient {
    class Player extends Client.Player<com.velocitypowered.api.proxy.Player> {
        public Player(com.velocitypowered.api.proxy.Player source) {
            super(source);
        }

        public void send(Component message) {
            this.source.sendMessage(message);
        }
        public void send(Error error) {
            this.source.sendMessage(error.toComponent());
        }
    }
    class Console extends Client.Console<ConsoleCommandSource> {
        public Console(@NotNull ConsoleCommandSource source) {
            super(source);
        }

        public void send(Component message) {
            this.source.sendMessage(message);
        }
        public void send(Error error) {
            this.source.sendMessage(error.toComponent());
        }
    }
    class Other extends Client.Other<CommandSource> {
        public Other(@NotNull CommandSource source) {
            super(source);
        }

        public void send(Component message) {
            this.source.sendMessage(message);
        }
        public void send(Error error) {
            this.source.sendMessage(error.toComponent());
        }
    }
}

