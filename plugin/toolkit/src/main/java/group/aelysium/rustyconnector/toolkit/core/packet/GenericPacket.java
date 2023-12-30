package group.aelysium.rustyconnector.toolkit.core.packet;

import com.google.gson.*;

import java.util.*;

public class GenericPacket implements IPacket {
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

    // Only exists so that inheriting packets don't have to define constructors.
    protected GenericPacket() {
        messageVersion = 0;
        identification = null;
        sender = null;
        target = null;
        parameters = new HashMap<>();
    }

    protected GenericPacket(int messageVersion, PacketIdentification identification, UUID sender, UUID target, Map<String, PacketParameter> parameters) {
        this.messageVersion = messageVersion;
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

    private static class NakedBuilder {
        private Integer protocolVersion = GenericPacket.protocolVersion();
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

        public <TPacket extends GenericPacket> TPacket build() {
            return (TPacket) new GenericPacket(this.protocolVersion, this.id, this.sender, this.target, this.parameters);
        }
    }

    // This implementation feels chunky. That's intentional, it's specifically written so that `.build()` isn't available until all the required params are filled in.
    public static class Builder {
        public static class ReadyForTargetingAssignment {
            private final NakedBuilder builder;

            protected ReadyForTargetingAssignment(NakedBuilder builder) {
                this.builder = builder;
            }

            /**
             * Packet is being sent from an MCLoader to the Proxy.
             * @param sendingMCLoader The UUID of the MCLoader that is sending the packet.
             */
            public ReadyForParameters toProxy(UUID sendingMCLoader) {
                this.builder.sender(sendingMCLoader);
                this.builder.target(null);
                return new ReadyForParameters(builder);
            }

            /**
             * Packet is being sent from the Proxy to an MCLoader
             * @param targetMCLoader The UUID of the MCLoader that the packet is being sent to.
             */
            public ReadyForParameters toMCLoader(UUID targetMCLoader) {
                this.builder.sender(null);
                this.builder.target(targetMCLoader);
                return new ReadyForParameters(builder);
            }

            /**
             * Packet is being sent from one MCLoader to another MCLoader
             * @param targetMCLoader The UUID of the MCLoader that the packet is being sent to.
             */
            public ReadyForParameters toMCLoader(UUID sendingMCLoader, UUID targetMCLoader) {
                this.builder.sender(sendingMCLoader);
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

            public <TPacket extends GenericPacket> TPacket build() {
                return this.builder.build();
            }
        }

        /**
         * The identification of this packet.
         * Identification is what differentiates a "Server ping packet" from a "Teleport player packet"
         */
        public ReadyForTargetingAssignment identification(PacketIdentification id) {
            return new ReadyForTargetingAssignment(new NakedBuilder().identification(id));
        }
    }

    public static class Serializer {
        /**
         * Parses a raw string into a received RedisMessage.
         * @param rawMessage The raw message to parse.
         * @return A received RedisMessage.
         */
        public GenericPacket parseReceived(String rawMessage) {
            Gson gson = new Gson();
            JsonObject messageObject = gson.fromJson(rawMessage, JsonObject.class);

            NakedBuilder builder = new NakedBuilder();

            messageObject.entrySet().forEach(entry -> {
                String key = entry.getKey();
                JsonElement value = entry.getValue();

                switch (key) {
                    case MasterValidParameters.PROTOCOL_VERSION -> builder.protocolVersion(value.getAsInt());
                    case MasterValidParameters.IDENTIFICATION -> builder.identification(PacketIdentification.mapping(value.getAsString()));
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

        private void parseParams(JsonObject object, NakedBuilder builder) {
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
}

