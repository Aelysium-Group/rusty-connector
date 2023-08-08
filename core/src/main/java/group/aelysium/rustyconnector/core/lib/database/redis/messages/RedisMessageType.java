package group.aelysium.rustyconnector.core.lib.database.redis.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RedisMessageType {
    /**
     * Regarding messages, sub-servers never have any way of knowing if the Proxy is actually online or listening.
     * They simply send messages into the Redis Channel in hopes that the proxy is listening.
     * <p>
     * This is done so that we don't have the proxy's IP Address stored on the sub-servers.
     * Instead, it's the proxy's job to keep track of the IP Addresses of all sub-servers and to recognize who is speaking to it.
     * When the proxy needs to send a message to a specific server, it will include a `to` parameter which specifies which
     * sub-server is allowed to read it.
     * The sub-servers don't have a `to` parameter and instead shoot their messages into the data-channel blindly.
     * <p>
     * If a message contains the `from` parameter it is from a sub-server.
     * If a message contains the `to` parameter it is from the proxy.
     * Messages cannot contain both `to` and `from` parameters. Additionally, these parameters cannot be set manually.
     */
    public static Mapping PING = new Mapping(100, "PING");
    public static Mapping PING_RESPONSE = new Mapping(101, "PING_RESPONSE");

    /**
     * `Server > Proxy` | Request to send a player to a family
     */
    public static Mapping SEND_PLAYER = new Mapping(200, "SEND_PLAYER");

    /**
     * `Proxy > Server` | Add a player's teleportation to the TPA queue on a specific server.
     */
    public static Mapping COORDINATE_REQUEST_QUEUE = new Mapping(300, "TPA_QUEUE_PLAYER");

    public static List<Mapping> toList() {
        List<Mapping> list = new ArrayList<>();
        list.add(PING);
        list.add(PING_RESPONSE);
        list.add(SEND_PLAYER);
        list.add(COORDINATE_REQUEST_QUEUE);

        return list;
    }

    public static Mapping mapping(String name) {
        return toList().stream().filter(entry -> Objects.equals(entry.name(), name)).findFirst().orElseThrow(NullPointerException::new);
    }
    public static Mapping mapping(int id) {
        return toList().stream().filter(entry -> entry.id() == id).findFirst().orElseThrow(NullPointerException::new);
    }

    public record Mapping (Integer id, String name) {
        @Override
        public String toString() {
            return String.valueOf(id);
        }
    }
}
