package group.aelysium.rustyconnector.plugin.fabric;

import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.plugin.common.command.Client;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

public class FabricClient implements Client<ServerCommandSource> {
    private final ServerCommandSource sender;

    public FabricClient(@NotNull ServerCommandSource sender) {
        this.sender = sender;
    }

    @Override
    public void enforceConsole() throws RuntimeException {
        if (this.sender.getEntity() == null) return;
        throw new RuntimeException("This command can only be used from the console.");
    }

    @Override
    public void enforcePlayer() throws RuntimeException {
        if (this.sender.getEntity() == null) throw new RuntimeException("This command can only be used by a player.");
    }

    @Override
    public void send(Component message) {
        ((Audience) this.sender).sendMessage(message);
    }

    @Override
    public void send(Error error) {
        ((Audience) this.sender).sendMessage(error.toComponent());
    }

    @Override
    public ServerCommandSource toSender() {
        return this.sender;
    }
}