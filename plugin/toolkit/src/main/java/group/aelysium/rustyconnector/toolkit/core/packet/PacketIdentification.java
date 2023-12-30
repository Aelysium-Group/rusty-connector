package group.aelysium.rustyconnector.toolkit.core.packet;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PacketIdentification {
    protected String id;

    protected PacketIdentification(String id) {
        this.id = id;
    }

    public String get() {
        return this.toString();
    }

    @Override
    public String toString() {
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PacketIdentification mapping = (PacketIdentification) o;
        return Objects.equals(get(), mapping.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(get());
    }

    /**
     * Create a new Packet Mapping from a pluginID and a packetID.
     * @param pluginID
     *        Should be a name representing your plugin.<br>
     *        Should be in the format of UPPER_SNAKE_CASE.<br>
     *        Should start with the prefix `RC_`.<br>
     *        Example: `RC_COMMAND_SYNC`.<br>
     * @param packetID
     *        The ID you want to assign this packet.<br>
     *        Should be in the format of UPPER_SNAKE_CASE.<br>
     *        Can be whatever you want.<br>
     * @return {@link PacketIdentification}
     * @throws IllegalArgumentException If illegal names are passed.
     */
    public static PacketIdentification from(@NotNull String pluginID, @NotNull String packetID) throws IllegalArgumentException {
        String idToCheck = pluginID.toUpperCase();
        if(idToCheck.isEmpty()) throw new IllegalArgumentException("pluginID can't be empty!");
        if(packetID.isEmpty()) throw new IllegalArgumentException("packetID can't be empty!");
        if(idToCheck.equals("ROOT")) throw new IllegalArgumentException("pluginID can't be 'ROOT'");
        if(idToCheck.equals("RC")) throw new IllegalArgumentException("pluginID can't be 'RC'");
        if(idToCheck.equals("RUSTYCONNECTOR")) throw new IllegalArgumentException("pluginID can't be 'RUSTYCONNECTOR'");
        if(idToCheck.equals("RUSTY-CONNECTOR")) throw new IllegalArgumentException("pluginID can't be 'RUSTY-CONNECTOR'");
        if(idToCheck.equals("RUSTY_CONNECTOR")) throw new IllegalArgumentException("pluginID can't be 'RUSTY_CONNECTOR'");

        return new PacketIdentification(pluginID + "-" + packetID);
    }

    public static List<PacketIdentification> toList() {
        List<PacketIdentification> list = new ArrayList<>();
        list.add(Predefined.MAGICLINK_HANDSHAKE);
        list.add(Predefined.MAGICLINK_HANDSHAKE_SUCCESS);
        list.add(Predefined.SEND_PLAYER);
        list.add(Predefined.QUEUE_TPA);
        list.add(Predefined.UNLOCK_SERVER);
        list.add(Predefined.LOCK_SERVER);

        return list;
    }

    public static PacketIdentification mapping(String id) {
        return toList().stream().filter(entry -> Objects.equals(entry.get(), id)).findFirst().orElseThrow(NullPointerException::new);
    }

    public interface Predefined {
        /**
         * `MCLoader > Proxy` | MCLoader requesting to interface with Proxy.
         *                    | If the MCLoader is new, it will attempt to be registered.
         *                    | If the MCLoader is already registered, it's connection will refresh.
         *                    
         *                    | This packet is simultaneously a handshake initializer and a keep-alive packet.
         */
        PacketIdentification MAGICLINK_HANDSHAKE = new PacketIdentification("RC-MLHK");

        /**
         * `Proxy > MCLoader` | Tells the MCLoader it couldn't be registered
         */
        PacketIdentification MAGICLINK_HANDSHAKE_FAIL = new PacketIdentification("RC-MLHF");

        /**
         * `Proxy > MCLoader` | Tells the MCLoader it was registered and how it should configure itself
         */
        PacketIdentification MAGICLINK_HANDSHAKE_SUCCESS = new PacketIdentification("RC-MLHS");

        /**
         * `MCLoader > Proxy` | Tells the Proxy to drop the Magic Link between this MCLoader.
         *                    | Typically used when the MCLoader is shutting down so that Magic Link doesn't have to manually scan it.
         */
        PacketIdentification MAGICLINK_HANDSHAKE_KILL = new PacketIdentification("RC-MLHK");

        /**
         * `MCLoader > Proxy` | Request to send a player to a family
         */
        PacketIdentification SEND_PLAYER = new PacketIdentification("RC-SP");

        /**
         * `Proxy > MCLoader` | Queues a tpa on a specific MCLoader. When that player joins the server, they'll be teleported to the other player.
         */
        PacketIdentification QUEUE_TPA = new PacketIdentification("RC-TPAQP");

        /**
         * `Server > MCLoader` | Tells the proxy to open a server.
         */
        PacketIdentification UNLOCK_SERVER = new PacketIdentification("RC-US");

        /**
         * `MCLoader > Proxy` | Tells the proxy to close a server.
         */
        PacketIdentification LOCK_SERVER = new PacketIdentification("RC-LS");

        /**
         * `MCLoader > Proxy` | Tells the proxy to end a game with the currently saved UUID.
         */
        PacketIdentification END_RANKED_GAME = new PacketIdentification("RC-ERG");
        /**
         * `Proxy > MCLoader` | Tells the MCLoader to save the UUID of a game.
         */
        PacketIdentification REQUEST_TO_START_RANKED_GAME = new PacketIdentification("RC-ARG");
    }
}
