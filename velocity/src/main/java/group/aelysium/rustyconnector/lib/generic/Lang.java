package group.aelysium.rustyconnector.lib.generic;

import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.Map;

public class Lang {
    private static Map<String, Component> langMap = new HashMap<>();

    /**
     * Add a value to the lang.json.
     * @param key The key of the value to set
     * @param value The value of the lang entry to set.
     * @return void
     */
    public static void add(String key, String value) {
        Component component = Component.text(value);
        langMap.put(key, component);
    }

    /**
     * Return a value from lang.json.
     * @param key The key of the value to return.
     * @return A Component containing the value.
     */
    public static Component get(String key) {
        return langMap.get(key);
    }
}
