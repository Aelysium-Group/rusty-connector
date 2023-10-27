package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.rest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class APIResponse {
    private int statusCode = 200;
    private JsonObject data = null;
    private JsonObject error = null;

    public int status() {
        return this.statusCode;
    }

    public void error(int code, String message) {
        this.error = new JsonObject();

        this.statusCode = code;
        this.error.add("code", new JsonPrimitive(code));
        this.error.add("message", new JsonPrimitive(message));
    }

    /**
     * Adds a new entry to the return data.
     * @param key The key to use. Should be defined using `snake_case`.
     * @param value The value to map to this key.
     */
    public void data(String key, JsonElement value) {
        if(this.data == null) this.data = new JsonObject();

        this.statusCode = 200;
        this.data.add(key, value);
    }

    @Override
    public String toString() {
        JsonObject output = new JsonObject();

        if(this.data != null)
            output.add("data", this.data);
        if(this.error != null)
            output.add("error", this.error);

        output.add("status", new JsonPrimitive(this.statusCode));

        return output.toString();
    }
}
