package group.aelysium.rustyconnector.core.lib.data_messaging;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.core.lib.Callable;
import group.aelysium.rustyconnector.core.lib.database.RedisIO;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class RedisMessage {
    private final String rawMessage;
    private final String key;
    private final RedisMessageType type;
    private final InetSocketAddress address;
    private final Map<String, String> parameters = new HashMap<>();
    private final boolean didReceive;

    public String getKey() { return this.key; }
    public InetSocketAddress getAddress() { return this.address; }
    public RedisMessageType getType() { return this.type; }

    public RedisMessage(String key, RedisMessageType type, String address) {
        this.key = key;
        this.type = type;

        String[] addressSplit = address.split(":");

        this.address = new InetSocketAddress(addressSplit[0], Integer.parseInt(addressSplit[1]));

        this.didReceive = false;
        this.rawMessage = null;
    }
    private RedisMessage(String key, RedisMessageType type, InetSocketAddress address, String rawMessage) {
        this.key = key;
        this.type = type;
        this.address = address;
        this.didReceive = true;
        this.rawMessage = rawMessage;
    }

    public void addParameter(String key, String value) {
        this.parameters.put(key, value);
    }
    public String getParameter(String key) {
        return this.parameters.get(key);
    }


    /**
     * Get parameter from JSON Object, parse it as String, and set it as message parameter.
     * @param object The JSON Object to parse from.
     * @param key The key of the parameter we want.
     * @param parameterKey The key of the parameter that will be in the message object.
     */
    public void setToParameter(JsonObject object, String key, String parameterKey) {
        String value = object.get(key).getAsString();
        this.addParameter(parameterKey, value);
    }

    /**
     * Get parameter from JSON Object, parse it as String, and set it as message parameter.
     * @param object The JSON Object to parse from.
     * @param key The key of the parameter we want.
     */
    public void setToParameter(JsonObject object, String key) throws NullPointerException {
        String value = object.get(key).getAsString();
        if(value == null) throw new NullPointerException("The requested value doesn't exist!");
        this.addParameter(key, value);
    }

    /**
     * Sends the current message over the datachannel.
     * @throws IllegalCallerException If you try to send a message that was already received over the data channel.
     */
    public void dispatchMessage(RedisIO redis) throws IllegalCallerException {
        if(this.didReceive) throw new IllegalCallerException("You can't send a message if it's already been received over the datachannel!");

        redis.sendPluginMessage(this.key, this.type, this.address, this.parameters);
    }

    /**
     * Returns the message as a string.
     * The returned string is actually the raw message that was received and parsed into this RedisMessage.
     * @return The message as a string.
     */
    @Override
    public String toString() {
        return this.rawMessage;
    }

    /**
     * Create new Redis message from a JSON.
     *
     * @param rawMessage The raw message, to be parsed.
     * @param origin  Where should the message be originating from?
     * @param addressForCompare The address to use to check if a message is directed to the current server (optional for proxies)
     * @return A Redis Message.
     * @throws NullPointerException If the message is missing a necessary parameter.
     * @throws IllegalArgumentException If the message is malformed or not meant for the server reading it.
     */
    public static RedisMessage create(String rawMessage, MessageOrigin origin, InetSocketAddress addressForCompare) throws NullPointerException, IllegalArgumentException {
        Gson gson = new Gson();
        JsonObject messageObject = gson.fromJson(rawMessage, JsonObject.class);

        Callable<RedisMessage> processProxyMessage = () -> { // Messages coming from the proxy
            if(addressForCompare == null) throw new NullPointerException("In order to process messages from the proxy, the sub-server must provide an address!");

            JsonElement assumedPrivateKey = messageObject.get("pk");
            JsonElement assumedType = messageObject.get("type");
            JsonElement assumedFromAddress = messageObject.get("from");
            JsonElement assumedToAddress = messageObject.get("to");

            // If `from` is NOT null. We have a problem.
            if(!(assumedFromAddress == null)) throw new IllegalArgumentException("Message is from another sub-server! Ignoring...");

            if(assumedPrivateKey == null) throw new NullPointerException("`private-key` is required in transit messages!");
            if(assumedType == null) throw new NullPointerException("`type` is required in transit messages!");
            if(assumedToAddress == null) throw new NullPointerException("`to` is required for in transit messages sent from the proxy!");

            RedisMessageType type = RedisMessageType.valueOf(assumedType.getAsString());
            String privateKey = assumedPrivateKey.getAsString();
            InetSocketAddress address = AddressUtil.stringToAddress(assumedToAddress.getAsString());

            // Unless type is a REG_ALL or REG_FAMILY. Check and make sure that the message is actually addressed to this server
            if(!(type == RedisMessageType.REG_ALL) && !(type == RedisMessageType.REG_FAMILY))
                if(!AddressUtil.addressToString(address).equals(AddressUtil.addressToString(addressForCompare)))
                    throw new IllegalArgumentException("This message isn't directed at us!");

            return new RedisMessage(
                    privateKey,
                    type,
                    address,
                    rawMessage
            );
        };
        Callable<RedisMessage> processServerMessage = () -> { // Messages coming from a sub-server
            JsonElement assumedPrivateKey = messageObject.get("pk");
            JsonElement assumedType = messageObject.get("type");
            JsonElement assumedFromAddress = messageObject.get("from");
            JsonElement assumedToAddress = messageObject.get("to");

            // If `to` is NOT null. We have a problem.
            if(!(assumedToAddress == null)) throw new IllegalArgumentException("Message is from proxy! Ignoring...");

            if(assumedPrivateKey == null) throw new NullPointerException("`private-key` is required in transit messages!");
            if(assumedType == null) throw new NullPointerException("`type` is required in transit messages!");
            if(assumedFromAddress == null) throw new NullPointerException("`from` is required for in transit messages sent from sub-servers!");

            RedisMessageType type = RedisMessageType.valueOf(assumedType.getAsString());
            String privateKey = assumedPrivateKey.getAsString();
            InetSocketAddress address = AddressUtil.stringToAddress(assumedFromAddress.getAsString());

            return new RedisMessage(
                    privateKey,
                    type,
                    address,
                    rawMessage
            );
        };


        if(origin == MessageOrigin.SERVER) return processProxyMessage.execute();
        if(origin == MessageOrigin.PROXY) return processServerMessage.execute();

        throw new NullPointerException("The origin that was provided for this message is invalid and doesn't exist!");
    }
}