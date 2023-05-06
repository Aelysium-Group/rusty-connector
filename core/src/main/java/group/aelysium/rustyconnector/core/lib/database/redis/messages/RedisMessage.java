package group.aelysium.rustyconnector.core.lib.database.redis.messages;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.*;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import io.lettuce.core.KeyValue;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class RedisMessage {

    private final boolean sendable;
    private final String rawMessage;

    private char[] privateKey;
    private final RedisMessageType type;
    private final InetSocketAddress address;
    private final MessageOrigin origin;

    public boolean isSendable() { return this.sendable; }
    public String getRawMessage() { return this.rawMessage; }
    public char[] getPrivateKey() { return this.privateKey; }
    public InetSocketAddress getAddress() { return this.address; }
    public RedisMessageType getType() { return this.type; }
    public MessageOrigin getOrigin() { return origin; }

    /*
     * Constructs a sendable RedisMessage.
     */
    protected RedisMessage(RedisMessageType type, InetSocketAddress address, MessageOrigin origin) {
        this.sendable = true;
        this.rawMessage = "";
        this.privateKey = null;
        this.type = type;
        this.address = address;
        this.origin = origin;
    }

    /*
     * Constructs a received RedisMessage.
     */
    protected RedisMessage(String rawMessage, char[] privateKey, RedisMessageType type, InetSocketAddress address, MessageOrigin origin) {
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
        return this.rawMessage;
    }

    /**
     * Checks if the two parameter lists (checking keys) match.
     * @param requiredParameters The parameters that are required.
     * @param parametersToCheck The parameter list to check.
     * @return `true` if all keys are present. `false` otherwise.
     */
    public static boolean validateParameters(List<String> requiredParameters, List<KeyValue<String, JsonElement>> parametersToCheck) {
        List<String> keysToCheck = new ArrayList<>();
        parametersToCheck.forEach(entry -> keysToCheck.add(entry.getKey()));
        List<String> matches = requiredParameters.stream().filter(keysToCheck::contains).toList();
        return requiredParameters.size() == matches.size();
    }

    public static class Builder {
        private String rawMessage;
        private char[] privateKey;
        private RedisMessageType type;
        private InetSocketAddress address;
        private MessageOrigin origin;
        private final List<KeyValue<String, JsonElement>> parameters = new ArrayList<>();

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
        public Builder setPrivateKey(String privateKey) {
            this.privateKey = privateKey.toCharArray();
            return this;
        }
        public Builder setType(RedisMessageType type) {
            this.type = type;
            return this;
        }
        public Builder setAddress(String address) {
            this.address = AddressUtil.stringToAddress(address);
            return this;
        }
        public Builder setOrigin(MessageOrigin origin) {
            this.origin = origin;
            return this;
        }
        public Builder setParameter(String key, String value) {
            this.parameters.add(KeyValue.just(key, JsonParser.parseString(value)));
            return this;
        }
        public Builder setParameter(String key, JsonElement value) {
            this.parameters.add(KeyValue.just(key, value));
            return this;
        }


        /**
         * Build a RedisMessage which was received via the RedisSubscriber.
         * This should be a RedisMessage which was previously built as a sendable RedisMessage, and then was sent via RedisPublisher.
         *
         * ## Required Parameters:
         * - `rawMessage`
         * - `privateKey`
         * - `type`
         * - `address`
         * - `origin`
         * @return A RedisMessage that can be published via the RedisPublisher.
         * @throws IllegalStateException If the required parameters are not provided.
         */
        public RedisMessage buildReceived() {
            if(this.rawMessage == null) throw new IllegalStateException("You must provide `rawMessage` when building a receivable RedisMessage!");
            if(this.privateKey == null) throw new IllegalStateException("You must provide `privateKey` when building a receivable RedisMessage!");
            if(this.type == null) throw new IllegalStateException("You must provide `type` when building a receivable RedisMessage!");
            if(this.address == null) throw new IllegalStateException("You must provide `address` when building a receivable RedisMessage!");
            if(this.origin == null) throw new IllegalStateException("You must provide `origin` when building a receivable RedisMessage!");

            return switch (this.type) {
                case PING, REG_ALL ->           new GenericRedisMessage(this.rawMessage, this.privateKey, this.type, this.address, this.origin);
                case REG ->       new RedisMessageServerRegisterRequest(this.rawMessage, this.privateKey, this.address, this.origin, this.parameters);
                case UNREG ->   new RedisMessageServerUnregisterRequest(this.rawMessage, this.privateKey, this.address, this.origin, this.parameters);
                case SEND ->                 new RedisMessageSendPlayer(this.rawMessage, this.privateKey, this.address, this.origin, this.parameters);
                case PONG ->                 new RedisMessageServerPong(this.rawMessage, this.privateKey, this.address, this.origin, this.parameters);
                case TPA_QUEUE_PLAYER -> new RedisMessageTPAQueuePlayer(this.rawMessage, this.privateKey, this.address, this.origin, this.parameters);
                case REG_FAMILY ->       new RedisMessageFamilyRegister(this.rawMessage, this.privateKey, this.address, this.origin, this.parameters);
                default -> {
                    throw new IllegalStateException("Invalid RedisMessage type encountered!");
                }
            };
        }

        /**
         * Build a RedisMessage which can be sent via the RedisPublisher.
         *
         * ## Required Parameters:
         * - `type`
         * - `origin`
         * @return A RedisMessage that can be published via the RedisPublisher.
         * @throws IllegalStateException If the required parameters are not provided.
         */
        public RedisMessage buildSendable() {
            if(this.type == null) throw new IllegalStateException("You must provide `type` when building a sendable RedisMessage!");
            if(this.origin == null) throw new IllegalStateException("You must provide `origin` when building a sendable RedisMessage!");
            // Specifically allow address to be set as `null`

            return switch (this.type) {
                case PING, REG_ALL ->           new GenericRedisMessage(this.type, this.address, this.origin);
                case REG ->       new RedisMessageServerRegisterRequest(this.address, this.origin, this.parameters);
                case UNREG ->   new RedisMessageServerUnregisterRequest(this.address, this.origin, this.parameters);
                case SEND ->                 new RedisMessageSendPlayer(this.address, this.origin, this.parameters);
                case PONG ->                 new RedisMessageServerPong(this.address, this.origin, this.parameters);
                case TPA_QUEUE_PLAYER -> new RedisMessageTPAQueuePlayer(this.address, this.origin, this.parameters);
                case REG_FAMILY ->       new RedisMessageFamilyRegister(this.address, this.origin, this.parameters);
                default -> {
                    throw new IllegalStateException("Invalid RedisMessage type encountered!");
                }
            };
        }
    }

    public static class Serializer {
        /**
         * Parses a raw string into a received RedisMessage.
         * @param rawMessage The raw message to parse.
         * @return A received RedisMessage.
         */
        public RedisMessage parseReceived(String rawMessage) {
            Gson gson = new Gson();
            JsonObject messageObject = gson.fromJson(rawMessage, JsonObject.class);

            RedisMessage.Builder redisMessageBuilder = new RedisMessage.Builder();
            redisMessageBuilder.setRawMessage(rawMessage);

            messageObject.entrySet().forEach(entry -> {
                String key = entry.getKey();
                JsonElement value = entry.getValue();

                switch (key) {
                    case MasterValidParameters.PRIVATE_KEY -> redisMessageBuilder.setPrivateKey(value.getAsString());
                    case MasterValidParameters.ADDRESS -> redisMessageBuilder.setAddress(value.getAsString());
                    case MasterValidParameters.TYPE -> redisMessageBuilder.setType(RedisMessageType.valueOf(value.getAsString()));
                    case MasterValidParameters.ORIGIN -> redisMessageBuilder.setOrigin(MessageOrigin.valueOf(value.getAsString()));
                    case MasterValidParameters.PARAMETERS -> parseParams(value.getAsJsonObject(), redisMessageBuilder);
                }
            });

            return redisMessageBuilder.buildReceived();
        }

        private void parseParams(JsonObject object, RedisMessage.Builder redisMessageBuilder) {
            object.entrySet().forEach(entry -> {
                String key = entry.getKey();
                JsonElement value = entry.getValue();

                redisMessageBuilder.setParameter(key, value);
            });
        }

    }

    public interface MasterValidParameters {
        String PRIVATE_KEY = "k";
        String TYPE = "t";
        String ADDRESS = "a";
        String ORIGIN = "o";
        String PARAMETERS = "p";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(PRIVATE_KEY);
            list.add(TYPE);
            list.add(ADDRESS);
            list.add(ORIGIN);
            list.add(PARAMETERS);

            return list;
        }
    }

}

