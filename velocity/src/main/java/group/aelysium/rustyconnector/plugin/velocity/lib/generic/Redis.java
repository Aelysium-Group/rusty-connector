package group.aelysium.rustyconnector.plugin.velocity.lib.generic;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Redis extends rustyconnector.generic.database.Redis {
    @Override
    public void onMessage(String message) {
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(message, JsonObject.class);

    }
}
