package group.aelysium.rustyconnector.toolkit.common;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Arrays;
import java.util.Objects;

public record UserPass(String user, char[] password) {
    public JsonObject toJSON() {
        JsonObject object = new JsonObject();
        object.add("username", new JsonPrimitive(this.user));

        return object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPass userPass = (UserPass) o;
        return Objects.equals(user, userPass.user) && Arrays.equals(password, userPass.password);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(user);
        result = 31 * result + Arrays.hashCode(password);
        return result;
    }
}
