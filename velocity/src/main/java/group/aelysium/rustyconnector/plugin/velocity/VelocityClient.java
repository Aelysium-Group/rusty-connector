package group.aelysium.rustyconnector.plugin.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.plugin.common.command.Client;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class VelocityClient implements Client<CommandSource> {
    private final CommandSource sender;

    public VelocityClient(@NotNull CommandSource sender) {
        this.sender = sender;
    }
    @Override
    public void enforceConsole() throws RuntimeException {
        if(this.sender instanceof ConsoleCommandSource) return;
        throw new RuntimeException("This command can only be used from the console.");
    }

    @Override
    public void enforcePlayer() throws RuntimeException {
        if(this.sender instanceof ConsoleCommandSource) throw new RuntimeException("This command can only be used from the console.");
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
    public CommandSource toSender() {
        return this.sender;
    }
}

