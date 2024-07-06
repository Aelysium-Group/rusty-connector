package group.aelysium.rustyconnector.toolkit.common.magic_link.packet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A parameter used by a packet.
 */
public class PacketParameter {
    protected char type;
    protected Object object;

    private PacketParameter(@NotNull Object object, char type) {
        this.object = object;
        this.type = type;
    }
    public PacketParameter(@NotNull Number object) {
        this(object, 'n');
    }
    public PacketParameter(@NotNull Boolean object) {
        this(object, 'b');
    }
    public PacketParameter(@NotNull String object) {
        this(object, 's');
    }
    public PacketParameter(@NotNull JsonArray object) {
        this(object, 'a');
    }
    public PacketParameter(@NotNull JsonObject object) {
        this(object, 'j');
    }
    public PacketParameter(JsonPrimitive object) {
        if(object.isNumber()) {
            this.object = object.getAsNumber();
            this.type = 'n';
            return;
        }
        if(object.isBoolean()) {
            this.object = object.getAsBoolean();
            this.type = 'b';
            return;
        }
        if(object.isString()) {
            this.object = object.getAsString();
            this.type = 's';
            return;
        }
        if(object.isJsonArray()) {
            this.object = object.getAsJsonArray();
            this.type = 'a';
            return;
        }
        if(object.isJsonObject()) {
            this.object = object.getAsJsonObject();
            this.type = 'j';
            return;
        }
        throw new IllegalStateException("Unexpected value: " + type);
    }

    public char type() {
        return this.type;
    }

    public int getAsInt() {
        return ((Number) this.object).intValue();
    }
    public long getAsLong() {
        return ((Number) this.object).longValue();
    }
    public double getAsDouble() {
        return ((Number) this.object).doubleValue();
    }
    public boolean getAsBoolean() {
        return (boolean) this.object;
    }
    public String getAsString() {
        return (String) this.object;
    }
    public UUID getStringAsUUID() {
        return UUID.fromString(this.getAsString());
    }
    public JsonArray getAsJsonArray() {
        return (JsonArray) this.object;
    }
    public JsonObject getAsJsonObject() {
        return (JsonObject) this.object;
    }

    public JsonElement toJSON() {
        return switch (type) {
            case 'n' -> new JsonPrimitive((Number) this.object);
            case 'b' -> new JsonPrimitive((Boolean) this.object);
            case 's' -> new JsonPrimitive((String) this.object);
            case 'a' -> (JsonArray) this.object;
            case 'j' -> (JsonObject) this.object;
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }
}
