package group.aelysium.rustyconnector.toolkit.common.magic_link.packet;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PacketIdentification {
    protected String id;

    public PacketIdentification(String id) {
        this.id = id;
    }

    public String get() {
        return this.id;
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
        return Objects.equals(this.get(), mapping.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(get());
    }

    /**
     * Create a new Packet Mapping from a pluginID and a packetID.
     * @param namespace
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
    public static PacketIdentification from(@NotNull String namespace, @NotNull String packetID) throws IllegalArgumentException {
        String idToCheck = namespace.toUpperCase();
        if(idToCheck.isEmpty()) throw new IllegalArgumentException("pluginID can't be empty!");
        if(packetID.isEmpty()) throw new IllegalArgumentException("packetID can't be empty!");

        return new PacketIdentification(namespace + "-" + packetID);
    }
}
