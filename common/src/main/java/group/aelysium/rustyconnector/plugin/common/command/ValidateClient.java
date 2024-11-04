package group.aelysium.rustyconnector.plugin.common.command;

import org.incendo.cloud.execution.preprocessor.CommandPreprocessingContext;
import org.incendo.cloud.execution.preprocessor.CommandPreprocessor;

public class ValidateClient<C extends Client<?>> implements CommandPreprocessor<C> {
        public void accept(CommandPreprocessingContext<C> context) {
            context.commandContext().sender().enforceConsole();
        }
}
