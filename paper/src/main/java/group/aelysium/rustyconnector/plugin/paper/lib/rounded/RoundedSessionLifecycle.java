package group.aelysium.rustyconnector.plugin.paper.lib.rounded;

import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.config.RoundedLifecycleConfig;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoundedSessionLifecycle {
    private final Map<String, List<String>> commands;
    private final int startDelay;
    private final int endDelay;
    private final int delayBeforeClose;

    private RoundedSessionLifecycle(Map<String, List<String>> commands, int startDelay, int endDelay, int delayBeforeClose) {
        this.commands = commands;
        this.startDelay = startDelay;
        this.endDelay = endDelay;
        this.delayBeforeClose = delayBeforeClose;
    }

    public static RoundedSessionLifecycle init() {
        PaperAPI api = PaperRustyConnector.getAPI();

        RoundedLifecycleConfig config = RoundedLifecycleConfig.newConfig(new File(api.getDataFolder(), "rounded_family/session_lifecycle.yml"), "paper_rounded_lifecycle_template.yml");
        if(!config.generate()) {
            throw new IllegalStateException("Unable to load or create config.yml!");
        }
        config.register();

        Map<String, List<String>> commands = new HashMap<>();
        commands.put("start", config.getStart_commands());
        commands.put("end",   config.getEnd_commands());
        commands.put("close", config.getClose_commands());

        return new RoundedSessionLifecycle(
                commands,
                config.getStart_delay(),
                config.getEnd_delay(),
                config.getEnd_delayToClosing()
        );
    }
}
