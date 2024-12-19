package group.aelysium.rustyconnector.plugin.paper;

import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.plugin.common.command.Client;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

public interface PaperClient {
    class Player extends Client.Player<org.bukkit.entity.Player> {
        public Player(org.bukkit.entity.Player source) {
            super(source);
        }

        public void send(Component message) {
            this.source.sendMessage(message);
        }
        public void send(Error error) {
            this.source.sendMessage(error.toComponent());
        }
    }
    class Console extends Client.Console<ConsoleCommandSender> {
        public Console(@NotNull ConsoleCommandSender source) {
            super(source);
        }

        public void send(Component message) {
            this.source.sendMessage(message);
        }
        public void send(Error error) {
            this.source.sendMessage(error.toComponent());
        }
    }
    class Other extends Client.Other<CommandSender> {
        public Other(@NotNull CommandSender source) {
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