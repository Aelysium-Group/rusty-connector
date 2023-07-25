package group.aelysium.rustyconnector.plugin.velocity;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PluginVersion {

    public static String get() {
        try {
            InputStream stream = Resources.getResource("velocity-plugin.json").openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            JsonObject json = new Gson().fromJson(reader, JsonObject.class);

            stream.close();
            reader.close();
            return json.getAsJsonObject("version").getAsString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}