package group.aelysium.rustyconnector.plugin.fabric;

import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.plugin.common.command.Client;
import net.kyori.adventure.text.Component;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

public interface FabricClient {
    class Player extends Client.Player<ServerCommandSource> {
        public Player(ServerCommandSource source) {
            super(source);
        }

        public void send(Component message) {
            this.source.sendMessage(message);
        }
        public void send(Error error) {
            this.source.sendMessage(error.toComponent());
        }
    }
    class Console extends Client.Console<ServerCommandSource> {
        public Console(@NotNull CommandSource source) {
            super(source);
        }

        public void send(Component message) {
            this.source.sendMessage(message);
        }
        public void send(Error error) {
            this.source.sendMessage(error.toComponent());
        }
    }
    class Other extends Client.Other<ServerCommandSource> {
        public Other(@NotNull ServerCommandSource source) {
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