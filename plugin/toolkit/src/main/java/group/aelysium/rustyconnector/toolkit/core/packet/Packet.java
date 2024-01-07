package group.aelysium.rustyconnector.toolkit.core.packet;

import com.google.gson.*;
import group.aelysium.rustyconnector.toolkit.core.JSONParseable;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.ICoreServiceHandler;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.IMCLoaderFlame;
import group.aelysium.rustyconnector.toolkit.velocity.central.VelocityFlame;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public final class Packet implements JSONParseable {
    private static final int protocolVersion = 2;

    public static int protocolVersion() {
        return protocolVersion;
    }

    private final int messageVersion;
    private final PacketIdentification identification;
    private final UUID sender;
    private final UUID target;
    protected final Map<String, PacketParameter> parameters;

    public int messageVersion() { return this.messageVersion; }
    public UUID sender() { return this.sender; }
    public UUID target() { return this.target; }
    public PacketIdentification identification() { return this.identification; }
    public Map<String, PacketParameter> parameters() { return parameters; }

    public Packet(Integer version, PacketIdentification identification, UUID sender, UUID target, Map<String, PacketParameter> parameters) {
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

        object.add(MasterValidParameters.PROTOCOL_VERSION, new JsonPrimitive(this.messageVersion));
        object.add(MasterValidParameters.IDENTIFICATION, new JsonPrimitive(this.identification.toString()));
        if(this.sender == null)
            object.add(MasterValidParameters.SENDER_UUID, new JsonPrimitive("PROXY"));
        else
            object.add(MasterValidParameters.SENDER_UUID, new JsonPrimitive(this.sender.toString()));
        if(this.target == null)
            object.add(MasterValidParameters.TARGET_UUID, new JsonPrimitive("PROXY"));
        else
            object.add(MasterValidParameters.TARGET_UUID, new JsonPrimitive(this.target.toString()));

        JsonObject parameters = new JsonObject();
        this.parameters.forEach((key, value) -> parameters.add(key, value.toJSON()));
        object.add(MasterValidParameters.PARAMETERS, parameters);

        return object;
    }

    protected static class NakedBuilder {
        private Integer protocolVersion = Packet.protocolVersion();
        private PacketIdentification id;
        private UUID sender;
        private UUID target;
        private final Map<String, PacketParameter> parameters = new HashMap<>();

        public NakedBuilder identification(PacketIdentification id) {
            this.id = id;
            return this;
        }

        /**
         * The sender of the packet.
         * @param uuid The UUID of the sending MCLoader for the packet. Or `null` if the sender is the Proxy.
         */
        public NakedBuilder sender(UUID uuid) {
            this.sender = uuid;
            return this;
        }

        /**
         * The target of the packet.
         * @param uuid The UUID of the target MCLoader for the packet. Or `null` if the target is the Proxy.
         */
        public NakedBuilder target(UUID uuid) {
            this.target = uuid;
            return this;
        }

        public NakedBuilder parameter(String key, String value) {
            this.parameters.put(key, new PacketParameter(value));
            return this;
        }
        public NakedBuilder parameter(String key, PacketParameter value) {
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
                this.builder.sender(flame.services().serverInfo().uuid());
                this.builder.target(null);
                return new ReadyForParameters(builder);
            }

            /**
             * Packet is being sent from one MCLoader to another MCLoader
             * @param targetMCLoader The UUID of the MCLoader that the packet is being sent to.
             */
            public ReadyForParameters sendingToAnotherMCLoader(UUID targetMCLoader) {
                this.builder.sender(flame.services().serverInfo().uuid());
                this.builder.target(targetMCLoader);
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
        private final NakedBuilder builder;

        public ProxyPacketBuilder(VelocityFlame<? extends group.aelysium.rustyconnector.toolkit.velocity.central.ICoreServiceHandler> flame) {
            this.builder = new NakedBuilder();
        }

        public static class ReadyForTargetingAssignment {
            private final NakedBuilder builder;

            protected ReadyForTargetingAssignment(NakedBuilder builder) {
                this.builder = builder;
            }

            /**
             * Packet is being sent from the Proxy to an MCLoader
             * @param targetMCLoader The UUID of the MCLoader that the packet is being sent to.
             */
            public ReadyForParameters sendingToMCLoader(UUID targetMCLoader) {
                this.builder.sender(null);
                this.builder.target(targetMCLoader);
                return new ReadyForParameters<>(builder);
            }
        }

        public static class ReadyForParameters<Packet extends group.aelysium.rustyconnector.toolkit.core.packet.Packet> {
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

            public group.aelysium.rustyconnector.toolkit.core.packet.Packet build() {
                return this.builder.build();
            }
        }

        /**
         * The identification of this packet.
         * Identification is what differentiates a "Server ping packet" from a "Teleport player packet"
         */
        public ReadyForTargetingAssignment identification(PacketIdentification id) {
            return new ReadyForTargetingAssignment(this.builder.identification(id));
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
                    case MasterValidParameters.PROTOCOL_VERSION -> builder.protocolVersion(value.getAsInt());
                    case MasterValidParameters.IDENTIFICATION -> builder.identification(new PacketIdentification(value.getAsString()));
                    case MasterValidParameters.SENDER_UUID -> {
                        UUID uuid = null;
                        try {
                            uuid = UUID.fromString(value.getAsString());
                        } catch (Exception ignore) {}

                        builder.sender(uuid);
                    }
                    case MasterValidParameters.TARGET_UUID -> {
                        UUID uuid = null;
                        try {
                            uuid = UUID.fromString(value.getAsString());
                        } catch (Exception ignore) {}

                        builder.target(uuid);
                    }
                    case MasterValidParameters.PARAMETERS -> parseParams(value.getAsJsonObject(), builder);
                }
            });

            return builder.build();
        }

        private static void parseParams(JsonObject object, NakedBuilder builder) {
            object.entrySet().forEach(entry -> {
                String key = entry.getKey();
                PacketParameter value = new PacketParameter(entry.getValue().getAsJsonPrimitive());

                builder.parameter(key, value);
            });
        }

    }

    public interface MasterValidParameters {
        String PROTOCOL_VERSION = "v";
        String IDENTIFICATION = "i";
        String SENDER_UUID = "su";
        String TARGET_UUID = "tu";
        String PARAMETERS = "p";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(PROTOCOL_VERSION);
            list.add(IDENTIFICATION);
            list.add(SENDER_UUID);
            list.add(TARGET_UUID);
            list.add(PARAMETERS);

            return list;
        }
    }

    public static class Wrapper {
        private final Packet packet;

        public int messageVersion() { return this.packet.messageVersion(); }
        public UUID sender() { return this.packet.sender(); }
        public UUID target() { return this.packet.target(); }
        public PacketIdentification identification() { return this.packet.identification(); }
        public Map<String, PacketParameter> parameters() { return this.packet.parameters(); }
        public PacketParameter parameter(String key) { return this.packet.parameters().get(key); }
        public Packet packet() {
            return this.packet;
        }

        protected Wrapper(Packet packet) {
            this.packet = packet;
        }
    }
}

