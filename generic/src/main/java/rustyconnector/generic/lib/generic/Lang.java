package rustyconnector.generic.lib.generic;

import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public static List<String> getWordmark() {
        List<String> wordmark = new ArrayList<>();
        wordmark.add("#################################################################################################");
        wordmark.add("");
        wordmark.add(" /$$$$$$$                        /$$");
        wordmark.add("| $$__  $$                      | $$");
        wordmark.add("| $$  \\ $$ /$$   /$$  /$$$$$$$ /$$$$$$   /$$   /$$");
        wordmark.add("| $$$$$$$/| $$  | $$ /$$_____/|_  $$_/  | $$  | $$");
        wordmark.add("| $$__  $$| $$  | $$|  $$$$$$   | $$    | $$  | $$");
        wordmark.add("| $$  \\ $$| $$  | $$ \\____  $$  | $$ /$$| $$  | $$");
        wordmark.add("| $$  | $$|  $$$$$$/ /$$$$$$$/  |  $$$$/|  $$$$$$$");
        wordmark.add("|__/  |__/ \\______/ |_______/    \\___/   \\____  $$");
        wordmark.add("                                         /$$  | $$");
        wordmark.add("                                        |  $$$$$$/");
        wordmark.add("                                         \\______/");
        wordmark.add("  /$$$$$$                                                      /$$");
        wordmark.add(" /$$__  $$                                                    | $$");
        wordmark.add("| $$  \\__/  /$$$$$$  /$$$$$$$  /$$$$$$$   /$$$$$$   /$$$$$$$ /$$$$$$    /$$$$$$   /$$$$$$");
        wordmark.add("| $$       /$$__  $$| $$__  $$| $$__  $$ /$$__  $$ /$$_____/|_  $$_/   /$$__  $$ /$$__  $$");
        wordmark.add("| $$      | $$  \\ $$| $$  \\ $$| $$  \\ $$| $$$$$$$$| $$        | $$    | $$  \\ $$| $$  \\__/");
        wordmark.add("| $$    $$| $$  | $$| $$  | $$| $$  | $$| $$_____/| $$        | $$ /$$| $$  | $$| $$");
        wordmark.add("|  $$$$$$/|  $$$$$$/| $$  | $$| $$  | $$|  $$$$$$$|  $$$$$$$  |  $$$$/|  $$$$$$/| $$");
        wordmark.add("\\______/  \\______/ |__/  |__/|__/  |__/ \\_______/ \\_______/   \\___/   \\______/ |__/");
        wordmark.add("");
        wordmark.add("#################################################################################################");
        wordmark.add("");
        wordmark.add("Developed by Aelysium | Nathan (SIVIN)");
        wordmark.add("");
        wordmark.add("#################################################################################################");

        return wordmark;
    }
}




