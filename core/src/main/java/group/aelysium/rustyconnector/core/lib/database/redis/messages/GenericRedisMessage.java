package group.aelysium.rustyconnector.core.lib.database.redis.messages;

import com.google.gson.*;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.*;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import io.lettuce.core.KeyValue;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType.*;

public class GenericRedisMessage {
    private static final int protocolVersion = 1;

    public static int getProtocolVersion() {
        return protocolVersion;
    }

    private final boolean sendable;
    private String rawMessage;
    private final int messageVersion;

    private char[] privateKey;
    private final RedisMessageType.Mapping type;
    private final InetSocketAddress address;
    private final MessageOrigin origin;

    public int getMessageVersion() { return this.messageVersion; }

    public boolean isSendable() { return this.sendable; }
    public String getRawMessage() { return this.rawMessage; }
    public char[] getPrivateKey() { return this.privateKey; }
    public InetSocketAddress getAddress() { return this.address; }
    public RedisMessageType.Mapping getType() { return this.type; }
    public MessageOrigin getOrigin() { return origin; }

    /*
     * Constructs a sendable RedisMessage.
     */
    protected GenericRedisMessage(RedisMessageType.Mapping type, InetSocketAddress address, MessageOrigin origin) {
        this.messageVersion = protocolVersion;
        this.sendable = true;
        this.rawMessage = null;
        this.privateKey = null;
        this.type = type;
        this.address = address;
        this.origin = origin;
    }

    /*
     * Constructs a received RedisMessage.
     */
    protected GenericRedisMessage(int messageVersion, String rawMessage, char[] privateKey, RedisMessageType.Mapping type, InetSocketAddress address, MessageOrigin origin) {
        this.messageVersion = messageVersion;
        this.sendable = false;
        this.rawMessage = rawMessage;
        this.privateKey = privateKey;
        this.type = type;
        this.address = address;
        this.origin = origin;
    }

    /**
     * Sign a sendable message with a private key.
     * @param privateKey The private key to sign with.
     * @throws IllegalStateException If you attempt to sign a received message. Or if the message is already signed.
     */
    public void signMessage(char[] privateKey) {
        if(!this.isSendable()) throw new IllegalStateException("Attempted to sign a received message! You can only sign sendable messages!");
        if(this.privateKey != null) throw new IllegalStateException("Attempted to sign a message that was already signed!");
        this.privateKey = privateKey;
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

        object.add(MasterValidParameters.PRIVATE_KEY, new JsonPrimitive(String.valueOf(this.privateKey)));
        object.add(MasterValidParameters.PROTOCOL_VERSION, new JsonPrimitive(this.messageVersion));
        object.add(MasterValidParameters.TYPE, new JsonPrimitive(String.valueOf(this.type)));
        object.add(MasterValidParameters.ORIGIN, new JsonPrimitive(String.valueOf(this.origin)));
        if(this.origin == MessageOrigin.PROXY || this.address == null)
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
        private char[] privateKey;
        private RedisMessageType.Mapping type;
        private InetSocketAddress address;
        private MessageOrigin origin;
        private final List<KeyValue<String, JsonPrimitive>> parameters = new ArrayList<>();

        public Builder() {}

        public Builder setRawMessage(String rawMessage) {
            this.rawMessage = rawMessage;
            return this;
        }

        /**
         * Sets the private key for this RedisMessage.
         * If you're building this RedisMessage as a sendable message.
         * You shouldn't have to set this because RedisPublisher will sign the message,
         * when you attempt to publish it.
         * @param privateKey The private key to set.
         * @return Builder
         */
        public Builder setPrivateKey(char[] privateKey) {
            this.privateKey = privateKey;
            return this;
        }
        public Builder setType(RedisMessageType.Mapping type) {
            this.type = type;
            return this;
        }

        /**
         * Address has two contexts:
         * If {@link MessageOrigin} is {@link MessageOrigin#PROXY}: Address is the recipient of the message.
         * If {@link MessageOrigin} is a {@link MessageOrigin#SERVER}: Address is referring to the sender of the message.
         * @param address The address of this message.
         * @return The Builder.
         */
        public Builder setAddress(String address) {
            this.address = AddressUtil.stringToAddress(address);
            return this;
        }
        /**
         * Address has two contexts:
         * If {@link MessageOrigin} is {@link MessageOrigin#PROXY}: Address is the recipient of the message.
         * If {@link MessageOrigin} is a {@link MessageOrigin#SERVER}: Address is referring to the sender of the message.
         * @param address The address of this message.
         * @return The Builder.
         */
        public Builder setAddress(InetSocketAddress address) {
            this.address = address;
            return this;
        }
        public Builder setOrigin(MessageOrigin origin) {
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
         *
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
        public GenericRedisMessage buildReceived() {
            if(this.protocolVersion == null) throw new IllegalStateException("You must provide `protocolVersion` when building a receivable RedisMessage!");
            if(this.rawMessage == null) throw new IllegalStateException("You must provide `rawMessage` when building a receivable RedisMessage!");
            if(this.privateKey == null) throw new IllegalStateException("You must provide `privateKey` when building a receivable RedisMessage!");
            if(this.type == null) throw new IllegalStateException("You must provide `type` when building a receivable RedisMessage!");
            if(this.address == null) throw new IllegalStateException("You must provide `address` when building a receivable RedisMessage!");
            if(this.origin == null) throw new IllegalStateException("You must provide `origin` when building a receivable RedisMessage!");

            if(this.type == REGISTER_ALL_SERVERS_TO_PROXY)  return new GenericRedisMessage(this.protocolVersion, this.rawMessage, this.privateKey, this.type, this.address, this.origin);
            if(this.type == PING)                           return new GenericRedisMessage(this.protocolVersion, this.rawMessage, this.privateKey, this.type, this.address, this.origin);
            if(this.type == REGISTER_SERVER)                return new RedisMessageServerRegisterRequest(this.protocolVersion, this.rawMessage, this.privateKey, this.address, this.origin, this.parameters);
            if(this.type == UNREGISTER_SERVER)              return new RedisMessageServerUnregisterRequest(this.protocolVersion, this.rawMessage, this.privateKey, this.address, this.origin, this.parameters);
            if(this.type == SEND_PLAYER)                    return new RedisMessageSendPlayer(this.protocolVersion, this.rawMessage, this.privateKey, this.address, this.origin, this.parameters);
            if(this.type == PONG)                           return new RedisMessageServerPong(this.protocolVersion, this.rawMessage, this.privateKey, this.address, this.origin, this.parameters);
            if(this.type == TPA_QUEUE_PLAYER)               return new RedisMessageTPAQueuePlayer(this.protocolVersion, this.rawMessage, this.privateKey, this.address, this.origin, this.parameters);
            if(this.type == REGISTER_ALL_SERVERS_TO_FAMILY) return new RedisMessageFamilyRegister(this.protocolVersion, this.rawMessage, this.privateKey, this.address, this.origin, this.parameters);
            if(this.type == ROUNDED_SESSION_START_REQUEST)  return new GenericRedisMessage(this.protocolVersion, this.rawMessage, this.privateKey, this.type, this.address, this.origin);
            if(this.type == ROUNDED_SESSION_START_REQUEST_RESPONSE)  return new RedisMessageRoundedSessionStartRequestResponse(this.protocolVersion, this.rawMessage, this.privateKey, this.address, this.origin, this.parameters);
            if(this.type == ROUNDED_SESSION_START_EVENT)    return new RedisMessageRoundedSessionStartEvent(this.protocolVersion, this.rawMessage, this.privateKey, this.address, this.origin, this.parameters);
            if(this.type == ROUNDED_SESSION_CLOSE_REQUEST)  return new RedisMessageRoundedSessionCloseRequest(this.protocolVersion, this.rawMessage, this.privateKey, this.address, this.origin, this.parameters);

            throw new IllegalStateException("Invalid RedisMessage type encountered!");
        }

        /**
         * Build a RedisMessage which can be sent via the RedisPublisher.
         *
         * ## Required Parameters:
         * - `type`
         * - `origin`
         *
         * ## Not Allowed Parameters:
         * - `protocolVersion`
         * @return A RedisMessage that can be published via the RedisPublisher.
         * @throws IllegalStateException If the required parameters are not provided. Or if protocolVersion is attempted to be set.
         */
        public GenericRedisMessage buildSendable() {
            if(this.protocolVersion != null) throw new IllegalStateException("You're not allowed to set `protocolVersion` when building a sendable RedisMessage!");
            if(this.type == null) throw new IllegalStateException("You must provide `type` when building a sendable RedisMessage!");
            if(this.origin == null) throw new IllegalStateException("You must provide `origin` when building a sendable RedisMessage!");
            // Specifically allow address to be set as `null`

            if(this.type == PING)                           return new GenericRedisMessage(this.type, this.address, this.origin);
            if(this.type == REGISTER_ALL_SERVERS_TO_PROXY)  return new GenericRedisMessage(this.type, this.address, this.origin);
            if(this.type == REGISTER_SERVER)                return new RedisMessageServerRegisterRequest(this.address, this.origin, this.parameters);
            if(this.type == UNREGISTER_SERVER)              return new RedisMessageServerUnregisterRequest(this.address, this.origin, this.parameters);
            if(this.type == SEND_PLAYER)                    return new RedisMessageSendPlayer(this.address, this.origin, this.parameters);
            if(this.type == PONG)                           return new RedisMessageServerPong(this.address, this.origin, this.parameters);
            if(this.type == TPA_QUEUE_PLAYER)               return new RedisMessageTPAQueuePlayer(this.address, this.origin, this.parameters);
            if(this.type == ROUNDED_SESSION_START_REQUEST)  return new GenericRedisMessage(this.type, this.address, this.origin);
            if(this.type == REGISTER_ALL_SERVERS_TO_FAMILY) return new RedisMessageFamilyRegister(this.address, this.origin, this.parameters);
            if(this.type == ROUNDED_SESSION_START_REQUEST_RESPONSE)  return new RedisMessageRoundedSessionStartRequestResponse(this.address, this.origin, this.parameters);
            if(this.type == ROUNDED_SESSION_START_EVENT)    return new RedisMessageRoundedSessionStartEvent(this.address, this.origin, this.parameters);
            if(this.type == ROUNDED_SESSION_CLOSE_REQUEST)  return new RedisMessageRoundedSessionCloseRequest(this.address, this.origin, this.parameters);

            throw new IllegalStateException("Invalid RedisMessage type encountered!");
        }
    }

    public static class Serializer {
        /**
         * Parses a raw string into a received RedisMessage.
         * @param rawMessage The raw message to parse.
         * @return A received RedisMessage.
         */
        public GenericRedisMessage parseReceived(String rawMessage) {
            Gson gson = new Gson();
            JsonObject messageObject = gson.fromJson(rawMessage, JsonObject.class);

            GenericRedisMessage.Builder redisMessageBuilder = new GenericRedisMessage.Builder();
            redisMessageBuilder.setRawMessage(rawMessage);

            messageObject.entrySet().forEach(entry -> {
                String key = entry.getKey();
                JsonElement value = entry.getValue();

                switch (key) {
                    case MasterValidParameters.PROTOCOL_VERSION -> redisMessageBuilder.setProtocolVersion(value.getAsInt());
                    case MasterValidParameters.PRIVATE_KEY -> redisMessageBuilder.setPrivateKey(value.getAsString().toCharArray());
                    case MasterValidParameters.ADDRESS -> redisMessageBuilder.setAddress(value.getAsString());
                    case MasterValidParameters.TYPE -> redisMessageBuilder.setType(RedisMessageType.getMapping(value.getAsInt()));
                    case MasterValidParameters.ORIGIN -> redisMessageBuilder.setOrigin(MessageOrigin.valueOf(value.getAsString()));
                    case MasterValidParameters.PARAMETERS -> parseParams(value.getAsJsonObject(), redisMessageBuilder);
                }
            });

            return redisMessageBuilder.buildReceived();
        }

        private void parseParams(JsonObject object, GenericRedisMessage.Builder redisMessageBuilder) {
            object.entrySet().forEach(entry -> {
                String key = entry.getKey();
                JsonPrimitive value = entry.getValue().getAsJsonPrimitive();

                redisMessageBuilder.setParameter(key, value);
            });
        }

    }

    public interface MasterValidParameters {
        String PROTOCOL_VERSION = "v";
        String PRIVATE_KEY = "k";
        String TYPE = "t";
        String ADDRESS = "a";
        String ORIGIN = "o";
        String PARAMETERS = "p";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(PROTOCOL_VERSION);
            list.add(PRIVATE_KEY);
            list.add(TYPE);
            list.add(ADDRESS);
            list.add(ORIGIN);
            list.add(PARAMETERS);

            return list;
        }
    }

}

