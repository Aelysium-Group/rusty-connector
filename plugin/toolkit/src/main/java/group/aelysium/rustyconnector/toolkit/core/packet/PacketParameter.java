package group.aelysium.rustyconnector.toolkit.core.packet;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

/**
 * A parameter used by a packet.
 */
public class PacketParameter {
    protected char type;
    protected Object object;

    private PacketParameter() {}
    public PacketParameter(@NotNull Number object) {
        this.object = object;
        this.type = 'n';
    }
    public PacketParameter(@NotNull Boolean object) {
        this.object = object;
        this.type = 'b';
    }
    public PacketParameter(@NotNull String object) {
        this.object = object;
        this.type = 's';
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

    protected JsonPrimitive toJSON() {
        return switch (type) {
            case 'n' -> new JsonPrimitive((Number) this.object);
            case 'b' -> new JsonPrimitive((Boolean) this.object);
            case 's' -> new JsonPrimitive((String) this.object);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }
}
