package group.aelysium.rustyconnector.plugin.fabric;

import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.plugin.common.command.Client;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

public interface FabricClient {
    class Player extends Client.Player<ServerCommandSource> {
        public Player(ServerCommandSource source) {
            super(source);
        }

        public void send(Component message) {
            ((Audience) this.source).sendMessage(message);
        }
        public void send(Error error) {
            ((Audience) this.source).sendMessage(error.toComponent());
        }
    }
    class Console extends Client.Console<ServerCommandSource> {
        public Console(@NotNull ServerCommandSource source) {
            super(source);
        }

        public void send(Component message) {
            ((Audience) this.source).sendMessage(message);
        }
        public void send(Error error) {
            ((Audience) this.source).sendMessage(error.toComponent());
        }
    }
    class Other extends Client.Other<ServerCommandSource> {
        public Other(@NotNull ServerCommandSource source) {
            super(source);
        }

        public void send(Component message) {
            ((Audience) this.source).sendMessage(message);
        }
        public void send(Error error) {
            ((Audience) this.source).sendMessage(error.toComponent());
        }
    }
}