package group.aelysium.rustyconnector.plugin.paper;

import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.util.CommandClient;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

public interface PaperClient {
    class Player extends CommandClient.Player<CommandSender> {
        public Player(CommandSender source) {
            super(source);
        }
        
        @Override
        public String id() {
            return ((org.bukkit.entity.Player) this.source).getUniqueId().toString();
        }
        
        @Override
        public String username() {
            return ((org.bukkit.entity.Player) this.source).getName();
        }
        
        public void send(Component message) {
            ((org.bukkit.entity.Player) this.source).sendMessage((ComponentLike) message);
        }
        public void send(Error error) {
            ((org.bukkit.entity.Player) this.source).sendMessage((ComponentLike) error.toComponent());
        }
    }
    class Console extends CommandClient.Console<ConsoleCommandSender> {
        public Console(@NotNull ConsoleCommandSender source) {
            super(source);
        }

        public void send(Component message) {
            ((ConsoleCommandSender) this.source).sendMessage((ComponentLike) message);
        }
        public void send(Error error) {
            ((ConsoleCommandSender) this.source).sendMessage((ComponentLike) error.toComponent());
        }
    }
    class Other extends CommandClient.Other<CommandSender> {
        public Other(@NotNull CommandSender source) {
            super(source);
        }

        public void send(Component message) {
            ((CommandSender) this.source).sendMessage((ComponentLike) message);
        }
        public void send(Error error) {
            ((CommandSender) this.source).sendMessage((ComponentLike) error.toComponent());
        }
    }
}