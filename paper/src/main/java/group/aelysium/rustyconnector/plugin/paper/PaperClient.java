package group.aelysium.rustyconnector.plugin.paper;

import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.plugin.common.command.Client;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PaperClient implements Client<CommandSender> {
    private final CommandSender sender;

    public PaperClient(@NotNull CommandSender sender) {
        this.sender = sender;
    }
    @Override
    public void enforceConsole() throws RuntimeException {
        if(this.sender instanceof ConsoleCommandSender) return;
        throw new RuntimeException("This command can only be used from the console.");
    }

    @Override
    public void enforcePlayer() throws RuntimeException {
        if(this.sender instanceof ConsoleCommandSender) throw new RuntimeException("This command can only be used from the console.");
    }

    @Override
    public void send(Component message) {
        this.sender.sendMessage(message);
    }

    @Override
    public void send(Error error) {
        this.sender.sendMessage(error.toComponent());
    }


    @Override
    public CommandSender toSender() {
        return this.sender;
    }
}
