package rustyconnector.generic.lib.database;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class RedisMessage {
    private final String key;
    private final RedisMessageType type;
    private final String address;
    private final Map<String, String> parameters = new HashMap<>();

    public String getKey() { return this.key; }
    public String getAddress() { return this.address; }
    public RedisMessageType getType() { return this.type; }

    public RedisMessage(String key, RedisMessageType type, String address) {
        this.key = key;
        this.type = type;
        this.address = address;
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
}