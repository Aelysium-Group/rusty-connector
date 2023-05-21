package group.aelysium.rustyconnector.plugin.paper.lib.rounded;

import group.aelysium.rustyconnector.core.lib.database.redis.RedisPublisher;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageRoundedSessionCloseRequest;
import group.aelysium.rustyconnector.core.lib.model.ClockService;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.config.RoundedLifecycleConfig;
import group.aelysium.rustyconnector.plugin.paper.lib.VirtualServerProcessor;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RoundedSessionLifecycle {
    private final ClockService clockService = new ClockService(3);
    private UUID currentSession = null;
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

    public boolean isOpen() {
        return this.currentSession == null;
    }

    /**
     * Start a session.
     * @throws IllegalStateException If an exception is already running.
     */
    public void start(UUID currentSession) {
        if(this.currentSession != null) throw new IllegalStateException("A session is already running! You must call .end() before you can start a new session!");

        this.clockService.scheduleDelayed(() -> {
            RoundedSessionLifecycle.this.currentSession = currentSession;

            ConsoleCommandSender sender = Bukkit.getConsoleSender();

            this.commands.get("start").forEach(command -> {
                try {
                    Bukkit.dispatchCommand(sender, command);
                } catch (Exception e) {
                    PaperRustyConnector.getAPI().getLogger().log("There was an issue executing the command: `"+command+"` when starting the session!");
                }
            });
        }, this.startDelay);
    }

    /**
     * End the current session.
     * This will run the ending commands and schedule the closing of the session.
     *
     * No players will disconnect from the server until {@link RoundedSessionLifecycle#close()} has been called.
     * @throws IllegalStateException If there is no session to end.
     */
    public void end() {
        if(this.currentSession != null) throw new IllegalStateException("A session is already running! You must call .end() before you can start a new session!");

        this.clockService.scheduleDelayed(() -> {
            ConsoleCommandSender sender = Bukkit.getConsoleSender();

            this.commands.get("end").forEach(command -> {
                try {
                    Bukkit.dispatchCommand(sender, command);
                } catch (Exception e) {
                    PaperRustyConnector.getAPI().getLogger().log("There was an issue executing the command: `"+command+"` when ending the session!");
                }
            });

            RoundedSessionLifecycle.this.clockService.scheduleDelayed(this::close, this.delayBeforeClose);
        }, this.endDelay);
    }

    /**
     * Close the session.
     * This will request the proxy to close the session before running the closing commands.
     */
    private void close() {
        this.currentSession = null;


        VirtualServerProcessor processor = PaperRustyConnector.getAPI().getVirtualProcessor();
        RedisPublisher publisher = processor.getRedisService().getMessagePublisher();
        GenericRedisMessage message = new GenericRedisMessage.Builder()
                .setType(RedisMessageType.ROUNDED_SESSION_CLOSE_REQUEST)
                .setOrigin(MessageOrigin.PROXY)
                .setAddress(processor.getAddress())
                .setParameter(RedisMessageRoundedSessionCloseRequest.ValidParameters.SERVER_NAME, processor.getName())
                .setParameter(RedisMessageRoundedSessionCloseRequest.ValidParameters.FAMILY_NAME, processor.getFamily())
                .buildSendable();

        publisher.publish(message);


        ConsoleCommandSender sender = Bukkit.getConsoleSender();

        this.commands.get("close").forEach(command -> {
            try {
                Bukkit.dispatchCommand(sender, command);
            } catch (Exception e) {
                PaperRustyConnector.getAPI().getLogger().log("There was an issue executing the command: `"+command+"` when closing the session!");
            }
        });
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
