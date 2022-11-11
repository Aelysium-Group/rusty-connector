package group.aelysium.rustyconnector.plugin.paper.lib.message.handling;

import group.aelysium.rustyconnector.core.lib.message.MessageHandler;
import group.aelysium.rustyconnector.core.lib.message.RedisMessage;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;

import java.net.InetSocketAddress;
import java.security.InvalidAlgorithmParameterException;

public class PingHandler implements MessageHandler {
    private final RedisMessage message;

    public PingHandler(RedisMessage message) {
        this.message = message;
    }

    @Override
    public void execute() {
        PaperRustyConnector plugin = PaperRustyConnector.getInstance();

        plugin.getVirtualServer().pong();
    }
}
