package group.aelysium.rustyconnector.core.lib.packets;

import com.google.gson.*;
import group.aelysium.rustyconnector.api.core.packet.IPacket;
import group.aelysium.rustyconnector.api.core.packet.PacketOrigin;
import group.aelysium.rustyconnector.api.core.packet.PacketType;
import group.aelysium.rustyconnector.core.lib.packets.variants.LockServerPacket;
import group.aelysium.rustyconnector.core.lib.packets.variants.CoordinateRequestQueuePacket;
import group.aelysium.rustyconnector.core.lib.packets.variants.UnlockServerPacket;
import group.aelysium.rustyconnector.core.lib.packets.variants.SendPlayerPacket;
import group.aelysium.rustyconnector.core.lib.packets.variants.ServerPingPacket;
import group.aelysium.rustyconnector.core.lib.packets.variants.ServerPingResponsePacket;
import group.aelysium.rustyconnector.api.velocity.util.AddressUtil;
import io.lettuce.core.KeyValue;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class GenericPacket implements IPacket {
    private static final int protocolVersion = 2;

    public static int protocolVersion() {
        return protocolVersion;
    }

    private final boolean sendable;
    private String rawMessage;
    private final int messageVersion;
    private final PacketType.Mapping type;
    private final InetSocketAddress address;
    private final PacketOrigin origin;

    public int messageVersion() { return this.messageVersion; }

    public boolean sendable() { return this.sendable; }
    public String rawMessage() { return this.rawMessage; }
    public InetSocketAddress address() { return this.address; }
    public PacketType.Mapping type() { return this.type; }
    public PacketOrigin origin() { return origin; }

    /*
     * Constructs a sendable RedisMessage.
     */
    protected GenericPacket(PacketType.Mapping type, InetSocketAddress address, PacketOrigin origin) {
        this.messageVersion = protocolVersion;
        this.sendable = true;
        this.rawMessage = null;
        this.type = type;
        this.address = address;
        this.origin = origin;
    }

    /*
     * Constructs a received RedisMessage.
     */
    protected GenericPacket(int messageVersion, String rawMessage, PacketType.Mapping type, InetSocketAddress address, PacketOrigin origin) {
        this.messageVersion = messageVersion;
        this.sendable = false;
        this.rawMessage = rawMessage;
        this.type = type;
        this.address = address;
        this.origin = origin;
    }

    /**
     * Returns the message as a string.
     * The returned string is actually the raw message that was received or is able to be sent through Redis.
     * @return The message as a string.
     */
    @Override
    public String toString() {
        if(this.rawMessage == null) this.rawMessage = this.toJSON().toString();
        return this.rawMessage;
    }

    public JsonObject toJSON() {
        JsonObject object = new JsonObject();

        object.add(MasterValidParameters.PROTOCOL_VERSION, new JsonPrimitive(this.messageVersion));
        object.add(MasterValidParameters.TYPE, new JsonPrimitive(String.valueOf(this.type)));
        object.add(MasterValidParameters.ORIGIN, new JsonPrimitive(String.valueOf(this.origin)));
        if(this.origin == PacketOrigin.PROXY && this.address == null)
            object.add(MasterValidParameters.ADDRESS, new JsonPrimitive("null"));
        else
            object.add(MasterValidParameters.ADDRESS, new JsonPrimitive(this.address.getHostString() + ":" + this.address.getPort()));

        return object;
    }

    /**
     * Checks if the two parameter lists (checking keys) match.
     * @param requiredParameters The parameters that are required.
     * @param parametersToCheck The parameter list to check.
     * @return `true` if all keys are present. `false` otherwise.
     */
    public static boolean validateParameters(List<String> requiredParameters, List<KeyValue<String, JsonPrimitive>> parametersToCheck) {
        List<String> keysToCheck = new ArrayList<>();
        parametersToCheck.forEach(entry -> keysToCheck.add(entry.getKey()));
        List<String> matches = requiredParameters.stream().filter(keysToCheck::contains).toList();
        return requiredParameters.size() == matches.size();
    }

    public static class Builder {
        private Integer protocolVersion;
        private String rawMessage;
        private PacketType.Mapping type;
        private InetSocketAddress address;
        private PacketOrigin origin;
        private final List<KeyValue<String, JsonPrimitive>> parameters = new ArrayList<>();

        public Builder() {}

        public Builder setRawMessage(String rawMessage) {
            this.rawMessage = rawMessage;
            return this;
        }

        public Builder setType(PacketType.Mapping type) {
            this.type = type;
            return this;
        }

        /**
         * Address has two contexts:
         * If {@link PacketOrigin} is {@link PacketOrigin#PROXY}: Address is the recipient of the message.
         * If {@link PacketOrigin} is a {@link PacketOrigin#SERVER}: Address is referring to the sender of the message.
         * @param address The address of this message.
         * @return The Builder.
         */
        public Builder setAddress(String address) {
            this.address = AddressUtil.stringToAddress(address);
            return this;
        }
        /**
         * Address has two contexts:
         * If {@link PacketOrigin} is {@link PacketOrigin#PROXY}: Address is the recipient of the message.
         * If {@link PacketOrigin} is a {@link PacketOrigin#SERVER}: Address is referring to the sender of the message.
         * @param address The address of this message.
         * @return The Builder.
         */
        public Builder setAddress(InetSocketAddress address) {
            this.address = address;
            return this;
        }
        public Builder setOrigin(PacketOrigin origin) {
            this.origin = origin;
            return this;
        }
        public Builder setProtocolVersion(int protocolVersion) {
            this.protocolVersion = protocolVersion;
            return this;
        }

        public Builder setParameter(String key, String value) {
            this.parameters.add(KeyValue.just(key, new JsonPrimitive(value)));
            return this;
        }
        public Builder setParameter(String key, JsonPrimitive value) {
            this.parameters.add(KeyValue.just(key, value));
            return this;
        }


        /**
         * Build a RedisMessage which was received via the RedisSubscriber.
         * This should be a RedisMessage which was previously built as a sendable RedisMessage, and then was sent via RedisPublisher.
         * <p>
         * ## Required Parameters:
         * - `protocolVersion`
         * - `rawMessage`
         * - `privateKey`
         * - `type`
         * - `address`
         * - `origin`
         * @return A RedisMessage that can be published via the RedisPublisher.
         * @throws IllegalStateException If the required parameters are not provided.
         */
        public GenericPacket buildReceived() {
            if (this.protocolVersion == null)
                throw new IllegalStateException("You must provide `protocolVersion` when building a receivable RedisMessage!");
            if (this.rawMessage == null)
                throw new IllegalStateException("You must provide `rawMessage` when building a receivable RedisMessage!");
            if (this.type == null)
                throw new IllegalStateException("You must provide `type` when building a receivable RedisMessage!");
            if (this.address == null)
                throw new IllegalStateException("You must provide `address` when building a receivable RedisMessage!");
            if (this.origin == null)
                throw new IllegalStateException("You must provide `origin` when building a receivable RedisMessage!");

            if (this.type == PacketType.PING)              return new ServerPingPacket(this.protocolVersion, this.rawMessage, this.address, this.origin, this.parameters);
            if (this.type == PacketType.PING_RESPONSE)     return new ServerPingResponsePacket(this.protocolVersion, this.rawMessage, this.address, this.origin, this.parameters);
            if (this.type == PacketType.SEND_PLAYER)       return new SendPlayerPacket(this.protocolVersion, this.rawMessage, this.address, this.origin, this.parameters);
            if (this.type == PacketType.COORDINATE_REQUEST_QUEUE)  return new CoordinateRequestQueuePacket(this.protocolVersion, this.rawMessage, this.address, this.origin, this.parameters);
            if (this.type == PacketType.UNLOCK_SERVER)   return new UnlockServerPacket(this.address, this.origin, this.parameters);
            if (this.type == PacketType.LOCK_SERVER)   return new LockServerPacket(this.address, this.origin, this.parameters);

            throw new IllegalStateException("Invalid RedisMessage type encountered!");
        }

        /**
         * Build a RedisMessage which can be sent via the RedisPublisher.
         * <p>
         * ## Required Parameters:
         * - `type`
         * - `origin`
         * <p>
         * ## Not Allowed Parameters:
         * - `protocolVersion`
         * @return A RedisMessage that can be published via the RedisPublisher.
         * @throws IllegalStateException If the required parameters are not provided. Or if protocolVersion is attempted to be set.
         */
        public GenericPacket buildSendable() {
            if(this.protocolVersion != null) throw new IllegalStateException("You're not allowed to set `protocolVersion` when building a sendable RedisMessage!");
            if(this.type == null) throw new IllegalStateException("You must provide `type` when building a sendable RedisMessage!");
            if(this.origin == null) throw new IllegalStateException("You must provide `origin` when building a sendable RedisMessage!");
            if(this.address == null) throw new IllegalStateException("You must provide `address` when building a sendable RedisMessage!");

            if(this.type == PacketType.PING)               return new ServerPingPacket(this.address, this.origin, this.parameters);
            if(this.type == PacketType.PING_RESPONSE)      return new ServerPingResponsePacket(this.address, this.origin, this.parameters);
            if(this.type == PacketType.SEND_PLAYER)        return new SendPlayerPacket(this.address, this.origin, this.parameters);
            if(this.type == PacketType.COORDINATE_REQUEST_QUEUE)   return new CoordinateRequestQueuePacket(this.address, this.origin, this.parameters);
            if(this.type == PacketType.UNLOCK_SERVER)   return new UnlockServerPacket(this.address, this.origin, this.parameters);
            if(this.type == PacketType.LOCK_SERVER)   return new LockServerPacket(this.address, this.origin, this.parameters);

            throw new IllegalStateException("Invalid RedisMessage type encountered!");
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

            GenericPacket.Builder redisMessageBuilder = new GenericPacket.Builder();
            redisMessageBuilder.setRawMessage(rawMessage);

            messageObject.entrySet().forEach(entry -> {
                String key = entry.getKey();
                JsonElement value = entry.getValue();

                switch (key) {
                    case MasterValidParameters.PROTOCOL_VERSION -> redisMessageBuilder.setProtocolVersion(value.getAsInt());
                    case MasterValidParameters.ADDRESS -> redisMessageBuilder.setAddress(value.getAsString());
                    case MasterValidParameters.TYPE -> redisMessageBuilder.setType(PacketType.mapping(value.getAsInt()));
                    case MasterValidParameters.ORIGIN -> redisMessageBuilder.setOrigin(PacketOrigin.valueOf(value.getAsString()));
                    case MasterValidParameters.PARAMETERS -> parseParams(value.getAsJsonObject(), redisMessageBuilder);
                }
            });

            return redisMessageBuilder.buildReceived();
        }

        private void parseParams(JsonObject object, GenericPacket.Builder redisMessageBuilder) {
            object.entrySet().forEach(entry -> {
                String key = entry.getKey();
                JsonPrimitive value = entry.getValue().getAsJsonPrimitive();

                redisMessageBuilder.setParameter(key, value);
            });
        }

    }

    public interface MasterValidParameters {
        String PROTOCOL_VERSION = "v";
        String TYPE = "t";
        String ADDRESS = "a";
        String ORIGIN = "o";
        String PARAMETERS = "p";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(PROTOCOL_VERSION);
            list.add(TYPE);
            list.add(ADDRESS);
            list.add(ORIGIN);
            list.add(PARAMETERS);

            return list;
        }
    }
}

