package group.aelysium.rustyconnector.toolkit.common.magic_link.packet;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.toolkit.common.JSONParseable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public interface IPacket extends JSONParseable {
    int protocolVersion = 2;

    /**
     * The protocol version used by this packet.
     */
    int messageVersion();

    /**
     * The node that sent this packet.
     */
    Target sender();

    /**
     * The node that this packet is addressed to.
     * @return
     */
    Target target();

    /**
     * The target that any responses should be made to.
     */
    ResponseTarget responseTarget();

    /**
     * The identification of this packet.
     */
    PacketIdentification identification();

    /**
     * The extra parameters that this packet caries.
     */
    Map<String, PacketParameter> parameters();

    /**
     * Checks whether this packet is a response to a previous packet.
     * @return `true` if this packet is a response to another packet. `false` otherwise.
     */
    boolean replying();

    /**
     * Handles a response(s) that has been made to this packet.
     */
    void handleReply(Consumer<IPacket> handler);

    /**
     * Returns the message as a string.
     * The returned string is actually the raw message that was received or is able to be sent through MagicLink.
     * @return The message as a string.
     */
    @Override
    String toString();

    // This implementation feels chunky. That's intentional, it's specifically written so that `.build()` isn't available until all the required params are filled in.
    interface Builder {
        /**
         * The identification of this packet.
         * Identification is what differentiates a "Server ping packet" from a "Teleport player packet"
         */
        PrepareForSending identification(PacketIdentification id);

        interface PrepareForSending {
            PrepareForSending parameter(String key, String value);
            PrepareForSending parameter(String key, PacketParameter value);

            /**
             * Prepares the packet to the specified {@link Target}.
             * @throws RuntimeException If this packet was already sent or used in a reply, and then you try to send it again.
             */
            ReadyForSending addressedTo(Target target) throws RuntimeException;

            /**
             * Prepares the packet as a reply to the specified {@link IPacket}.
             * @throws RuntimeException If this packet was already sent or used in a reply, and then you try to send it again.
             */
            ReadyForSending addressedTo(IPacket packet) throws RuntimeException;
        }
        interface ReadyForSending {
            /**
             * Sends the packet.
             * @throws RuntimeException If there was an issue sending the packet.
             */
            void send() throws RuntimeException;
        }
    }

    interface Parameters {
        String PROTOCOL_VERSION = "v";
        String IDENTIFICATION = "i";
        String SENDER = "s";
        String TARGET = "t";
        String RESPONSE = "r";
        String PARAMETERS = "p";
    }

    class Target implements JSONParseable {
        private final UUID uuid;
        private final Origin origin;

        private Target(UUID uuid, @NotNull Origin origin) {
            this.uuid = uuid;
            this.origin = origin;
        }

        public UUID uuid() {
            return this.uuid;
        }
        public Origin origin() {
            return this.origin;
        }

        public JsonObject toJSON() {
            JsonObject object = new JsonObject();

            object.add("u", new JsonPrimitive(this.uuid.toString()));
            object.add("n", new JsonPrimitive(Origin.toInteger(this.origin)));

            return object;
        }

        public static Target fromJSON(JsonObject object) {
            return new Target(
                    UUID.fromString(object.get("u").getAsString()),
                    Origin.fromInteger(object.get("n").getAsInt())
            );
        }

        public static Target mcLoader(UUID uuid) {
            return new Target(uuid, Origin.MCLOADER);
        }
        public static Target proxy(UUID uuid) {
            return new Target(uuid, Origin.PROXY);
        }
        public static Target allAvailableProxies() {
            return new Target(null, Origin.ANY_PROXY);
        }

        /**
         * Checks if the passed node can be considered the same as `this`.
         * For example, if `this` is of type {@link Origin#PROXY} and `node` is of type {@link Origin#ANY_PROXY} this will return `true`
         * because `this` would be considered a part of `ANY_PROXY`.
         * @param target Some other node.
         * @return `true` if the other node is a valid way of identifying `this` node. `false` otherwise.
         */
        public boolean isNodeEquivalentToMe(Target target) {
            // If the two match as defined by default expected behaviour, return true.
            if(Objects.equals(uuid, target.uuid) && origin == target.origin) return true;

            // If one of the two is of type "ANY_PROXY" and the other is of type "PROXY", return true.
            if(
                    (this.origin == Origin.ANY_PROXY && target.origin == Origin.PROXY) ||
                            (this.origin == Origin.PROXY && target.origin == Origin.ANY_PROXY) ||
                            (this.origin == Origin.ANY_PROXY && target.origin == Origin.ANY_PROXY)
            ) return true;

            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Target target = (Target) o;

            // If the two match as defined by default expected behaviour, return true.
            return Objects.equals(uuid, target.uuid) && origin == target.origin;
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid, origin);
        }

        public enum Origin {
            PROXY,
            ANY_PROXY,
            MCLOADER,
            ANY_MCLOADER
            ;

            public static Origin fromInteger(int number) {
                return switch (number) {
                    case 0 -> Origin.PROXY;
                    case 1 -> Origin.ANY_PROXY;
                    case 2 -> Origin.MCLOADER;
                    case 3 -> Origin.ANY_MCLOADER;
                    default -> throw new ClassCastException(number+" has no associated value!");
                };
            }
            public static int toInteger(Origin origin) {
                return switch (origin) {
                    case PROXY -> 0;
                    case ANY_PROXY -> 1;
                    case MCLOADER -> 2;
                    case ANY_MCLOADER -> 3;
                };
            }
        }
    }

    class ResponseTarget implements JSONParseable {
        private final UUID ownTarget;
        private final UUID remoteTarget;

        private ResponseTarget() {
            this(null);
        }
        private ResponseTarget(UUID remoteTarget) {
            this(UUID.randomUUID(), remoteTarget);
        }
        protected ResponseTarget(@NotNull UUID ownTarget, UUID remoteTarget) {
            this.ownTarget = ownTarget;
            this.remoteTarget = remoteTarget;
        }

        public UUID ownTarget() {
            return this.ownTarget;
        }
        public Optional<UUID> remoteTarget() {
            if(this.remoteTarget == null) return Optional.empty();
            return Optional.of(this.remoteTarget);
        }

        public static ResponseTarget chainStart() {
            return new ResponseTarget();
        }
        public static ResponseTarget respondTo(UUID remoteTarget) {
            return new ResponseTarget(remoteTarget);
        }
        public static ResponseTarget fromJSON(@NotNull JsonObject object) {
            return new ResponseTarget(
                    UUID.fromString(object.get("o").getAsString()),
                    UUID.fromString(object.get("r").getAsString())
            );
        }

        public JsonObject toJSON() {
            JsonObject object = new JsonObject();

            String remoteTarget = "";
            if(this.remoteTarget != null) remoteTarget = this.remoteTarget.toString();

            object.add("o", new JsonPrimitive(this.ownTarget.toString()));
            object.add("r", new JsonPrimitive(remoteTarget));

            return object;
        }
    }
}