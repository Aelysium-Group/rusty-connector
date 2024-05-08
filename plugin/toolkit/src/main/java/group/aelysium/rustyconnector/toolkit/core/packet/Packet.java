package group.aelysium.rustyconnector.toolkit.core.packet;

import com.google.gson.*;
import group.aelysium.rustyconnector.toolkit.core.JSONParseable;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.ICoreServiceHandler;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.IMCLoaderFlame;
import group.aelysium.rustyconnector.toolkit.velocity.central.VelocityFlame;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class Packet implements JSONParseable {
    private static final int protocolVersion = 3;

    public static int protocolVersion() {
        return protocolVersion;
    }

    private final int messageVersion;
    private final PacketIdentification identification;
    private final Packet.Node sender;
    private final Packet.Node target;
    private final Map<String, PacketParameter> parameters;

    public int messageVersion() { return this.messageVersion; }
    public Packet.Node sender() { return this.sender; }
    public Packet.Node target() { return this.target; }
    public PacketIdentification identification() { return this.identification; }
    public Map<String, PacketParameter> parameters() { return parameters; }

    /**
     * A convenience method that lets you build a reply packet that will reply to a packet you've received.
     * The act of "replying" is simply marking the `.sender()` of the previous packet as this packet's `.target()`
     * and marking the `.target()` of the previous packet as this packet's `.sender()`.
     */
    public ReplyPacketBuilder reply() {
        return new ReplyPacketBuilder(this);
    }

    public Packet(@NotNull Integer version, @NotNull PacketIdentification identification, @NotNull Packet.Node sender, @NotNull Packet.Node target, @NotNull Map<String, PacketParameter> parameters) {
        this.messageVersion = version;
        this.identification = identification;
        this.sender = sender;
        this.target = target;
        this.parameters = parameters;
    }

    /**
     * Returns the message as a string.
     * The returned string is actually the raw message that was received or is able to be sent through Redis.
     * @return The message as a string.
     */
    @Override
    public String toString() {
        return this.toJSON().toString();
    }

    public JsonObject toJSON() {
        JsonObject object = new JsonObject();

        object.add(Parameters.PROTOCOL_VERSION, new JsonPrimitive(this.messageVersion));
        object.add(Parameters.IDENTIFICATION, new JsonPrimitive(this.identification.toString()));
        object.add(Parameters.SENDER, this.sender.toJSON());
        object.add(Parameters.TARGET, this.target.toJSON());

        JsonObject parameters = new JsonObject();
        this.parameters.forEach((key, value) -> parameters.add(key, value.toJSON()));
        object.add(Parameters.PARAMETERS, parameters);

        return object;
    }

    protected static class NakedBuilder {
        private Integer protocolVersion = Packet.protocolVersion();
        private PacketIdentification id;
        private Packet.Node sender;
        private Packet.Node target;
        private final Map<String, PacketParameter> parameters = new HashMap<>();

        public NakedBuilder identification(@NotNull PacketIdentification id) {
            this.id = id;
            return this;
        }

        public NakedBuilder sender(@NotNull Packet.Node sender) {
            this.sender = sender;
            return this;
        }

        public NakedBuilder target(@NotNull Packet.Node target) {
            this.target = target;
            return this;
        }

        public NakedBuilder parameter(@NotNull String key, @NotNull String value) {
            this.parameters.put(key, new PacketParameter(value));
            return this;
        }
        public NakedBuilder parameter(@NotNull String key, @NotNull PacketParameter value) {
            this.parameters.put(key, value);
            return this;
        }

        protected NakedBuilder protocolVersion(int protocolVersion) {
            this.protocolVersion = protocolVersion;
            return this;
        }

        public Packet build() {
            return new Packet(this.protocolVersion, this.id, this.sender, this.target, this.parameters);
        }
    }

    // This implementation feels chunky. That's intentional, it's specifically written so that `.build()` isn't available until all the required params are filled in.
    public static class MCLoaderPacketBuilder {
        private final IMCLoaderFlame<? extends ICoreServiceHandler> flame;
        private final NakedBuilder builder;

        public MCLoaderPacketBuilder(IMCLoaderFlame<? extends ICoreServiceHandler> flame) {
            this.flame = flame;
            this.builder = new NakedBuilder();
        }

        public static class ReadyForTargetingAssignment {
            private final IMCLoaderFlame<? extends ICoreServiceHandler> flame;
            private final NakedBuilder builder;

            protected ReadyForTargetingAssignment(IMCLoaderFlame<? extends ICoreServiceHandler> flame, NakedBuilder builder) {
                this.flame = flame;
                this.builder = builder;
            }

            /**
             * Packet is being sent from an MCLoader to the Proxy.
             */
            public ReadyForParameters sendingToProxy() {
                this.builder.sender(Node.mcLoader(flame.services().serverInfo().uuid()));
                this.builder.target(Node.unknownProxy());
                return new ReadyForParameters(builder);
            }

            /**
             * Packet is being sent from one MCLoader to another MCLoader
             * @param targetMCLoader The UUID of the MCLoader that the packet is being sent to.
             */
            public ReadyForParameters sendingToAnotherMCLoader(UUID targetMCLoader) {
                this.builder.sender(Node.mcLoader(flame.services().serverInfo().uuid()));
                this.builder.target(Node.mcLoader(targetMCLoader));
                return new ReadyForParameters(builder);
            }
        }
        public static class ReadyForParameters {
            private final NakedBuilder builder;

            protected ReadyForParameters(NakedBuilder builder) {
                this.builder = builder;
            }

            public ReadyForParameters parameter(String key, String value) {
                this.builder.parameter(key, new PacketParameter(value));
                return this;
            }

            public ReadyForParameters parameter(String key, PacketParameter value) {
                this.builder.parameter(key, value);
                return this;
            }

            public Packet build() {
                return this.builder.build();
            }
        }

        /**
         * The identification of this packet.
         * Identification is what differentiates a "Server ping packet" from a "Teleport player packet"
         */
        public ReadyForTargetingAssignment identification(PacketIdentification id) {
            return new ReadyForTargetingAssignment(flame, builder.identification(id));
        }
    }
    public static class ProxyPacketBuilder {
        private final VelocityFlame<? extends group.aelysium.rustyconnector.toolkit.velocity.central.ICoreServiceHandler> flame;
        private final NakedBuilder builder;

        public ProxyPacketBuilder(VelocityFlame<? extends group.aelysium.rustyconnector.toolkit.velocity.central.ICoreServiceHandler> flame) {
            this.flame = flame;
            this.builder = new NakedBuilder();
        }

        public static class ReadyForTargetingAssignment {
            private final VelocityFlame<? extends group.aelysium.rustyconnector.toolkit.velocity.central.ICoreServiceHandler> flame;
            private final NakedBuilder builder;

            protected ReadyForTargetingAssignment(VelocityFlame<? extends group.aelysium.rustyconnector.toolkit.velocity.central.ICoreServiceHandler> flame, NakedBuilder builder) {
                this.flame = flame;
                this.builder = builder;
            }

            /**
             * Packet is being sent from the Proxy to an MCLoader
             * @param targetMCLoader The UUID of the MCLoader that the packet is being sent to.
             */
            public ReadyForParameters sendingToMCLoader(UUID targetMCLoader) {
                this.builder.sender(Node.proxy(flame.uuid()));
                this.builder.target(Node.mcLoader(targetMCLoader));
                return new ReadyForParameters(builder);
            }
        }

        public static class ReadyForParameters {
            private final NakedBuilder builder;

            protected ReadyForParameters(NakedBuilder builder) {
                this.builder = builder;
            }

            public ReadyForParameters parameter(String key, String value) {
                this.builder.parameter(key, new PacketParameter(value));
                return this;
            }
            public ReadyForParameters parameter(String key, PacketParameter value) {
                this.builder.parameter(key, value);
                return this;
            }

            public Packet build() {
                return this.builder.build();
            }
        }

        /**
         * The identification of this packet.
         * Identification is what differentiates a "Server ping packet" from a "Teleport player packet"
         */
        public ReadyForTargetingAssignment identification(PacketIdentification id) {
            return new ReadyForTargetingAssignment(this.flame, this.builder.identification(id));
        }
    }
    public static class ReplyPacketBuilder {
        private final NakedBuilder builder;

        public ReplyPacketBuilder(Packet packet) {
            this.builder = new NakedBuilder();
            this.builder.sender(packet.target());
            this.builder.target(packet.sender());
        }

        public static class ReadyForParameters {
            private final NakedBuilder builder;

            protected ReadyForParameters(NakedBuilder builder) {
                this.builder = builder;
            }

            public ReadyForParameters parameter(String key, String value) {
                this.builder.parameter(key, new PacketParameter(value));
                return this;
            }
            public ReadyForParameters parameter(String key, PacketParameter value) {
                this.builder.parameter(key, value);
                return this;
            }

            public Packet build() {
                return this.builder.build();
            }
        }

        /**
         * The identification of this packet.
         * Identification is what differentiates a "Server ping packet" from a "Teleport player packet"
         */
        public ReadyForParameters identification(PacketIdentification id) {
            return new ReadyForParameters(this.builder.identification(id));
        }
    }

    public static class Serializer {
        /**
         * Parses a raw string into a received RedisMessage.
         * @param rawMessage The raw message to parse.
         * @return A received RedisMessage.
         */
        public static Packet parseReceived(String rawMessage) {
            Gson gson = new Gson();
            JsonObject messageObject = gson.fromJson(rawMessage, JsonObject.class);

            NakedBuilder builder = new NakedBuilder();

            messageObject.entrySet().forEach(entry -> {
                String key = entry.getKey();
                JsonElement value = entry.getValue();

                switch (key) {
                    case Parameters.PROTOCOL_VERSION -> builder.protocolVersion(value.getAsInt());
                    case Parameters.IDENTIFICATION -> builder.identification(new PacketIdentification(value.getAsString()));
                    case Parameters.SENDER -> builder.sender(Node.fromJSON(value.getAsJsonObject()));
                    case Parameters.TARGET -> builder.target(Node.fromJSON(value.getAsJsonObject()));
                    case Parameters.PARAMETERS -> parseParams(value.getAsJsonObject(), builder);
                }
            });

            return builder.build();
        }

        private static void parseParams(JsonObject object, NakedBuilder builder) {
            object.entrySet().forEach(entry -> {
                String key = entry.getKey();
                PacketParameter value = null;

                if(entry.getValue().isJsonPrimitive())
                    value = new PacketParameter(entry.getValue().getAsJsonPrimitive());
                if(entry.getValue().isJsonArray())
                    value = new PacketParameter(entry.getValue().getAsJsonArray());
                if(entry.getValue().isJsonObject())
                    value = new PacketParameter(entry.getValue().getAsJsonObject());

                if(value == null) value = new PacketParameter("null");

                builder.parameter(key, value);
            });
        }

    }

    public interface Parameters {
        String PROTOCOL_VERSION = "v";
        String IDENTIFICATION = "i";
        String SENDER = "s";
        String TARGET = "t";
        String PARAMETERS = "p";
    }

    public static class Node implements JSONParseable {
        private final UUID uuid;
        private final Origin origin;

        private Node(UUID uuid, Origin origin) {
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

            if(this.uuid == null)
                object.add("u", new JsonPrimitive(""));
            else
                object.add("u", new JsonPrimitive(this.uuid.toString()));
            object.add("n", new JsonPrimitive(Origin.toInteger(this.origin)));

            return object;
        }

        public static Node fromJSON(JsonObject object) {
            UUID uuid = null;
            if(!object.get("u").getAsString().isEmpty())
                uuid = UUID.fromString(object.get("u").getAsString());

            return new Node(
                    uuid,
                    Origin.fromInteger(object.get("n").getAsInt())
            );
        }

        public static Node mcLoader(UUID uuid) {
            return new Node(uuid, Origin.MCLOADER);
        }
        public static Node proxy(UUID uuid) {
            return new Node(uuid, Origin.PROXY);
        }
        public static Node unknownProxy() {
            return new Node(null, Origin.ANY_PROXY);
        }

        /**
         * Checks if the passed node can be considered the same as `this`.
         * For example, if `this` is of type {@link Origin#PROXY} and `node` is of type {@link Origin#ANY_PROXY} this will return `true`
         * because `this` would be considered a part of `ANY_PROXY`.
         * @param node Some other node.
         * @return `true` if the other node is a valid way of identifying `this` node. `false` otherwise.
         */
        public boolean isNodeEquivalentToMe(Node node) {
            // If the two match as defined by default expected behaviour, return true.
            if(Objects.equals(uuid, node.uuid) && origin == node.origin) return true;

            // If one of the two is of type "ANY_PROXY" and the other is of type "PROXY", return true.
            if(
                (this.origin == Origin.ANY_PROXY && node.origin == Origin.PROXY) ||
                (this.origin == Origin.PROXY && node.origin == Origin.ANY_PROXY) ||
                (this.origin == Origin.ANY_PROXY && node.origin == Origin.ANY_PROXY)
            ) return true;

            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;

            // If the two match as defined by default expected behaviour, return true.
            return Objects.equals(uuid, node.uuid) && origin == node.origin;
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid, origin);
        }

        enum Origin {
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

    public static class Wrapper {
        private final Packet packet;

        public int messageVersion() { return this.packet.messageVersion(); }
        public Node sender() { return this.packet.sender(); }
        public Node target() { return this.packet.target(); }
        public PacketIdentification identification() { return this.packet.identification(); }
        public Map<String, PacketParameter> parameters() { return this.packet.parameters(); }
        public PacketParameter parameter(String key) { return this.packet.parameters().get(key); }
        public Packet packet() {
            return this.packet;
        }
        public ReplyPacketBuilder reply() {
            return this.packet.reply();
        }

        protected Wrapper(Packet packet) {
            this.packet = packet;
        }
    }
}

