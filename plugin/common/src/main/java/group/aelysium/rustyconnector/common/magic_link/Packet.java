package group.aelysium.rustyconnector.common.magic_link;

import com.google.gson.*;
import group.aelysium.rustyconnector.toolkit.RC;
import group.aelysium.rustyconnector.toolkit.RustyConnector;
import group.aelysium.rustyconnector.toolkit.common.magic_link.IMagicLink;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.PacketParameter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class Packet implements IPacket {
    private static final int protocolVersion = 2;

    public static int protocolVersion() {
        return protocolVersion;
    }

    private final int messageVersion;
    private final PacketIdentification identification;
    private final Target sender;
    private final Target target;
    private final ResponseTarget responseTarget;
    private final Map<String, PacketParameter> parameters;
    private List<Consumer<IPacket>> replyListeners = null; // Intentionally left null, if no responses are saved here, we don't want to bother instantiating a list here.

    public int messageVersion() { return this.messageVersion; }
    public Target sender() { return this.sender; }
    public Target target() { return this.target; }
    public ResponseTarget responseTarget() { return this.responseTarget; }
    public PacketIdentification identification() { return this.identification; }
    public Map<String, PacketParameter> parameters() { return parameters; }
    public List<Consumer<IPacket>> replyListeners() {
        return this.replyListeners;
    }

    /**
     * Checks whether this packet is a response to a previous packet.
     * @return `true` if this packet is a response to another packet. `false` otherwise.
     */
    public boolean replying() {
        return this.responseTarget.remoteTarget().isPresent();
    }

    /**
     * Returns the packet which was sent as a reply to this one.
     */
    public void handleReply(Consumer<IPacket> response) {
        if(this.replyListeners == null) this.replyListeners = new ArrayList<>();
        this.replyListeners.add(response);
    }

    protected Packet(@NotNull Integer version, @NotNull PacketIdentification identification, @NotNull Packet.Target sender, @NotNull Packet.Target target, @NotNull Packet.ResponseTarget responseTarget, @NotNull Map<String, PacketParameter> parameters) {
        this.messageVersion = version;
        this.identification = identification;
        this.sender = sender;
        this.target = target;
        this.responseTarget = responseTarget;
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
        object.add(Parameters.RESPONSE, this.responseTarget.toJSON());

        JsonObject parameters = new JsonObject();
        this.parameters.forEach((key, value) -> parameters.add(key, value.toJSON()));
        object.add(Parameters.PARAMETERS, parameters);

        return object;
    }

    public static Builder New() {
        return new Builder();
    }

    protected static class NakedBuilder {
        private Integer protocolVersion = Packet.protocolVersion();
        private PacketIdentification id;
        private Target sender;
        private Target target;
        private ResponseTarget responseTarget = ResponseTarget.chainStart();
        private final Map<String, PacketParameter> parameters = new HashMap<>();

        public NakedBuilder identification(@NotNull PacketIdentification id) {
            this.id = id;
            return this;
        }

        public NakedBuilder sender(@NotNull Packet.Target sender) {
            this.sender = sender;
            return this;
        }

        public NakedBuilder target(@NotNull Packet.Target target) {
            this.target = target;
            return this;
        }

        public NakedBuilder response(@NotNull Packet.ResponseTarget responseTarget) {
            this.responseTarget = responseTarget;
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
            return new Packet(this.protocolVersion, this.id, this.sender, this.target, this.responseTarget, this.parameters);
        }
    }

    // This implementation feels chunky. That's intentional, it's specifically written so that `.build()` isn't available until all the required params are filled in.
    public static class Builder implements IPacket.Builder {
        private final NakedBuilder builder = new NakedBuilder();

        protected Builder() {}

        public static class PrepareForSending implements IPacket.Builder.PrepareForSending {
            private final NakedBuilder builder;

            protected PrepareForSending(NakedBuilder builder) {
                this.builder = builder;
            }

            public PrepareForSending parameter(String key, String value) {
                this.builder.parameter(key, new PacketParameter(value));
                return this;
            }

            public PrepareForSending parameter(String key, PacketParameter value) {
                this.builder.parameter(key, value);
                return this;
            }

            private void assignTargetAndSender(@NotNull Packet.Target target, Target sender) {
                this.builder.target(target);

                if(sender != null) this.builder.sender(sender);

                try {
                    this.builder.sender(Target.mcLoader(RustyConnector.Toolkit.Proxy().orElseThrow().orElseThrow().uuid()));
                    return;
                } catch (Exception ignore) {}
                try {
                    this.builder.sender(Target.mcLoader(RustyConnector.Toolkit.MCLoader().orElseThrow().orElseThrow().uuid()));
                    return;
                } catch (Exception ignore) {}
                throw new RuntimeException("No available flames existed in order to send the packet!");
            }
            private void assignTargetAndSender(@NotNull Packet.Target target) {
                assignTargetAndSender(target, null);
            }

            public ReadyForSending addressedTo(Target target) {
                assignTargetAndSender(target);

                return new ReadyForSending(this.builder);
            }

            public ReadyForSending addressedTo(IPacket targetPacket) {
                assignTargetAndSender(targetPacket.target(), targetPacket.sender());
                this.builder.response(ResponseTarget.respondTo(targetPacket.responseTarget().ownTarget()));

                return new ReadyForSending(this.builder);
            }
        }
        public static class ReadyForSending implements IPacket.Builder.ReadyForSending {
            private final NakedBuilder builder;

            protected ReadyForSending(NakedBuilder builder) {
                this.builder = builder;
            }

            private IMagicLink fetchMagicLink() {
                try {
                    return RC.P.MagicLink();
                } catch (Exception ignore) {}
                try {
                    return RC.M.MagicLink();
                } catch (Exception ignore) {}
                throw new RuntimeException("No available flames existed in order to send the packet!");
            }

            @Override
            public void send() throws RuntimeException {
                Packet packet = this.builder.build();

                IMagicLink magicLinkService = fetchMagicLink();
                ((MagicLinkCore) magicLinkService).publish(packet);
            }
        }

        /**
         * The identification of this packet.
         * Identification is what differentiates a "Server ping packet" from a "Teleport player packet"
         */
        public PrepareForSending identification(PacketIdentification id) {
            return new PrepareForSending(builder.identification(id));
        }
    }

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
                case Parameters.SENDER -> builder.sender(Target.fromJSON(value.getAsJsonObject()));
                case Parameters.TARGET -> builder.target(Target.fromJSON(value.getAsJsonObject()));
                case Parameters.RESPONSE -> builder.response(ResponseTarget.fromJSON(value.getAsJsonObject()));
                case Parameters.PARAMETERS -> {
                    value.getAsJsonObject().entrySet().forEach(entry2 -> {
                        String key2 = entry.getKey();
                        PacketParameter value2 = new PacketParameter(entry2.getValue().getAsJsonPrimitive());

                        builder.parameter(key2, value2);
                    });
                }
            }
        });

        return builder.build();
    }

    public static class Wrapper extends Packet {
        public Wrapper(IPacket packet) {
            super(
                    packet.messageVersion(),
                    packet.identification(),
                    packet.sender(),
                    packet.target(),
                    packet.responseTarget(),
                    packet.parameters()
            );
        }
    }
}