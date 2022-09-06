package group.aelysium.rustyconnector.core.lib.generic.database;

import com.google.gson.JsonObject;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class RedisMessage {
    private final String key;
    private final RedisMessageType type;
    private final InetSocketAddress address;
    private final Map<String, String> parameters = new HashMap<>();
    private final boolean didReceive;

    public String getKey() { return this.key; }
    public InetSocketAddress getAddress() { return this.address; }
    public RedisMessageType getType() { return this.type; }

    public RedisMessage(String key, RedisMessageType type, String address, boolean didReceive) {
        this.key = key;
        this.type = type;

        String[] addressSplit = address.split(":");

        this.address = new InetSocketAddress(addressSplit[0], Integer.parseInt(addressSplit[1]));

        this.didReceive = didReceive;
    }
    public RedisMessage(String key, RedisMessageType type, InetSocketAddress address, boolean didReceive) {
        this.key = key;
        this.type = type;
        this.address = address;
        this.didReceive = didReceive;
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
    public void dispatchMessage(Redis redis) throws IllegalCallerException {
        if(this.didReceive) throw new IllegalCallerException("You can't send a message if it's already been received over the datachannel!");

        redis.sendMessage(this.key, this.type, this.address, this.parameters);
    }
}