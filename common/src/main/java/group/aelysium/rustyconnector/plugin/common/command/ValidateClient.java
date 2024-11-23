package group.aelysium.rustyconnector.plugin.common.command;

import net.kyori.adventure.text.Component;
import org.incendo.cloud.exception.InvalidCommandSenderException;
import org.incendo.cloud.execution.preprocessor.CommandPreprocessingContext;
import org.incendo.cloud.execution.preprocessor.CommandPreprocessor;
import org.incendo.cloud.services.type.ConsumerService;

public class ValidateClient<C extends Client<?>> implements CommandPreprocessor<C> {
        public void accept(CommandPreprocessingContext<C> context) {
            try {
                context.commandContext().sender().enforceConsole();
            } catch (Exception ignore) {
                context.commandContext().sender().send(Component.text("This command must be sent from the console."));
                ConsumerService.interrupt();
            }/*
            try {
                context.commandContext().sender().enforcePlayer();
            } catch (Exception ignore) {
                context.commandContext().sender().send(Component.text("This command must be sent as a player."));
                ConsumerService.interrupt();
            }*/
        }
}
