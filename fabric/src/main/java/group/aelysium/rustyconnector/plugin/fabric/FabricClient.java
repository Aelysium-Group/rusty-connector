package group.aelysium.rustyconnector.plugin.fabric;

import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.util.CommandClient;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface FabricClient {
    class Player extends CommandClient.Player<ServerCommandSource> {
        public Player(ServerCommandSource source) {
            super(source);
        }

        public void send(Component message) {
            ((Audience) this.source).sendMessage(message);
        }
        public void send(Error error) {
            ((Audience) this.source).sendMessage((ComponentLike) error.toComponent());
        }
        
        @Override
        public String id() {
            return Objects.requireNonNull(((ServerCommandSource) this.source).getPlayer()).getUuidAsString();
        }
        
        @Override
        public String username() {
            return ((ServerCommandSource) this.source).getName();
        }
    }
    class Console extends CommandClient.Console<ServerCommandSource> {
        public Console(@NotNull ServerCommandSource source) {
            super(source);
        }

        public void send(Component message) {
            ((Audience) this.source).sendMessage(message);
        }
        public void send(Error error) {
            ((Audience) this.source).sendMessage((ComponentLike) error.toComponent());
        }
    }
    class Other extends CommandClient.Other<ServerCommandSource> {
        public Other(@NotNull ServerCommandSource source) {
            super(source);
        }

        public void send(Component message) {
            ((Audience) this.source).sendMessage(message);
        }
        public void send(Error error) {
            ((Audience) this.source).sendMessage((ComponentLike) error.toComponent());
        }
    }
}