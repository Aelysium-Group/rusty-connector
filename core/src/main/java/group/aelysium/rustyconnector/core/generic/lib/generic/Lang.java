package group.aelysium.rustyconnector.core.generic.lib.generic;

import net.kyori.adventure.text.Component;
import group.aelysium.rustyconnector.core.Logger;

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
     * Get a value which has been set in the lang.json file
     * @param key The key of the lang to return
     */
    public static Component getDynamic(String key) {
        return langMap.get(key);
    }

    public static List<String> get(String name, String... params) {
        switch (name) {
            case "wordmark":
                return wordmark();
            case "info":
                return info();
            case "registered-families":
                return registeredFamilies();
            case "registered-servers":
                registeredServers(params);
        }
        return new ArrayList<>();
    }

    public static void print(Logger logger, List<String> message) {
        message.forEach(logger::log);
    }

    public static String border() {
        return "█████████████████████████████████████████████████████████████████████████████████████████████████";
    }
    public static String spacing() {
        return "";
    }

    private static List<String> registeredFamilies() { // font: ANSI Shadow
        List<String> text = new ArrayList<>();
        text.add(border());
        text.add(spacing());
        text.add("██████╗  ███████╗  ██████╗  ██╗ ███████╗ ████████╗ ███████╗ ██████╗  ███████╗ ██████╗ ");
        text.add("██╔══██╗ ██╔════╝ ██╔════╝  ██║ ██╔════╝ ╚══██╔══╝ ██╔════╝ ██╔══██╗ ██╔════╝ ██╔══██╗");
        text.add("██████╔╝ █████╗   ██║  ███╗ ██║ ███████╗    ██║    █████╗   ██████╔╝ █████╗   ██║  ██║");
        text.add("██╔══██╗ ██╔══╝   ██║   ██║ ██║ ╚════██║    ██║    ██╔══╝   ██╔══██╗ ██╔══╝   ██║  ██║");
        text.add("██║  ██║ ███████╗ ╚██████╔╝ ██║ ███████║    ██║    ███████╗ ██║  ██║ ███████╗ ██████╔╝");
        text.add("╚═╝  ╚═╝ ╚══════╝  ╚═════╝  ╚═╝ ╚══════╝    ╚═╝    ╚══════╝ ╚═╝  ╚═╝ ╚══════╝ ╚═════╝ ");
        text.add(spacing());
        text.add("███████╗  █████╗  ███╗   ███╗ ██╗ ██╗      ██╗ ███████╗ ███████╗");
        text.add("██╔════╝ ██╔══██╗ ████╗ ████║ ██║ ██║      ██║ ██╔════╝ ██╔════╝");
        text.add("█████╗   ███████║ ██╔████╔██║ ██║ ██║      ██║ █████╗   ███████╗");
        text.add("██╔══╝   ██╔══██║ ██║╚██╔╝██║ ██║ ██║      ██║ ██╔══╝   ╚════██║");
        text.add("██║      ██║  ██║ ██║ ╚═╝ ██║ ██║ ███████╗ ██║ ███████╗ ███████║");
        text.add("╚═╝      ╚═╝  ╚═╝ ╚═╝     ╚═╝ ╚═╝ ╚══════╝ ╚═╝ ╚══════╝ ╚══════╝");
        text.add(spacing());
        text.add(border());

        return text;
    }

    private static List<String> registeredServers(String[] params) { // font: ANSI Shadow
        List<String> text = new ArrayList<>();
        text.add(border());
        text.add(spacing());
        text.add("██████╗  ███████╗  ██████╗  ██╗ ███████╗ ████████╗ ███████╗ ██████╗  ███████╗ ██████╗ ");
        text.add("██╔══██╗ ██╔════╝ ██╔════╝  ██║ ██╔════╝ ╚══██╔══╝ ██╔════╝ ██╔══██╗ ██╔════╝ ██╔══██╗");
        text.add("██████╔╝ █████╗   ██║  ███╗ ██║ ███████╗    ██║    █████╗   ██████╔╝ █████╗   ██║  ██║");
        text.add("██╔══██╗ ██╔══╝   ██║   ██║ ██║ ╚════██║    ██║    ██╔══╝   ██╔══██╗ ██╔══╝   ██║  ██║");
        text.add("██║  ██║ ███████╗ ╚██████╔╝ ██║ ███████║    ██║    ███████╗ ██║  ██║ ███████╗ ██████╔╝");
        text.add("╚═╝  ╚═╝ ╚══════╝  ╚═════╝  ╚═╝ ╚══════╝    ╚═╝    ╚══════╝ ╚═╝  ╚═╝ ╚══════╝ ╚═════╝ ");
        text.add(spacing());
        text.add("███████╗ ███████╗ ██████╗  ██╗   ██╗ ███████╗ ██████╗  ███████╗");
        text.add("██╔════╝ ██╔════╝ ██╔══██╗ ██║   ██║ ██╔════╝ ██╔══██╗ ██╔════╝");
        text.add("███████╗ █████╗   ██████╔╝ ██║   ██║ █████╗   ██████╔╝ ███████╗");
        text.add("╚════██║ ██╔══╝   ██╔══██╗ ╚██╗ ██╔╝ ██╔══╝   ██╔══██╗ ╚════██║");
        text.add("███████║ ███████╗ ██║  ██║  ╚████╔╝  ███████╗ ██║  ██║ ███████║");
        text.add("╚══════╝ ╚══════╝ ╚═╝  ╚═╝   ╚═══╝   ╚══════╝ ╚═╝  ╚═╝ ╚══════╝");
        text.add(spacing());
        text.add(border());
        text.add(spacing());
        text.add("All servers that have been registered to the family: "+params[0]);
        text.add(params[0]);
        text.add(spacing());
        text.add(border());

        return text;
    }

    private static List<String> info() { // font: ANSI Shadow
        List<String> text = new ArrayList<>();
        text.add(border());
        text.add(spacing());
        text.add("██╗ ███╗   ██╗ ███████╗  ██████╗ ");
        text.add("██║ ████╗  ██║ ██╔════╝ ██╔═══██╗");
        text.add("██║ ██╔██╗ ██║ █████╗   ██║   ██║");
        text.add("██║ ██║╚██╗██║ ██╔══╝   ██║   ██║");
        text.add("██║ ██║ ╚████║ ██║      ╚██████╔╝");
        text.add("╚═╝ ╚═╝  ╚═══╝ ╚═╝       ╚═════╝ ");
        text.add(spacing());
        text.add(border());

        return text;
    }

    public static List<String> commandUsage() { // font: ANSI Shadow
        List<String> text = new ArrayList<>();
        text.add(Lang.border());
        text.add(Lang.spacing());
        text.add("██╗   ██╗ ███████╗  █████╗   ██████╗  ███████╗");
        text.add("██║   ██║ ██╔════╝ ██╔══██╗ ██╔════╝  ██╔════╝");
        text.add("██║   ██║ ███████╗ ███████║ ██║  ███╗ █████╗  ");
        text.add("██║   ██║ ╚════██║ ██╔══██║ ██║   ██║ ██╔══╝  ");
        text.add("╚██████╔╝ ███████║ ██║  ██║ ╚██████╔╝ ███████╗");
        text.add(" ╚═════╝  ╚══════╝ ╚═╝  ╚═╝  ╚═════╝  ╚══════╝");
        text.add(Lang.spacing());
        text.add(Lang.border());
        text.add(Lang.spacing());
        text.add("Try any command that is marked blue to see more details about what it can do!");
        text.add("If a command is yellow. That command will run something if you use it! Make sure you know what it does before you run it!");
        text.add(Lang.spacing());
        text.add(Lang.border());
        text.add(Lang.spacing());

        return text;
    }

    private static List<String> wordmark() {
        List<String> text = new ArrayList<>();
        text.add(border());
        text.add(spacing());
        text.add(" /███████                        /██");
        text.add("| ██__  ██                      | ██");
        text.add("| ██  \\ ██ /██   /██  /███████ /██████   /██   /██");
        text.add("| ███████/| ██  | ██ /██_____/|_  ██_/  | ██  | ██");
        text.add("| ██__  ██| ██  | ██|  ██████   | ██    | ██  | ██");
        text.add("| ██  \\ ██| ██  | ██ \\____  ██  | ██ /██| ██  | ██");
        text.add("| ██  | ██|  ██████/ /███████/  |  ████/|  ███████");
        text.add("|__/  |__/ \\______/ |_______/    \\___/   \\____  ██");
        text.add("                                         /██  | ██");
        text.add("                                        |  ██████/");
        text.add("  /██████                                \\______/             /██");
        text.add(" /██__  ██                                                    | ██");
        text.add("| ██  \\__/  /██████  /███████  /███████   /██████   /███████ /██████    /██████   /██████");
        text.add("| ██       /██__  ██| ██__  ██| ██__  ██ /██__  ██ /██_____/|_  ██_/   /██__  ██ /██__  ██");
        text.add("| ██      | ██  \\ ██| ██  \\ ██| ██  \\ ██| ████████| ██        | ██    | ██  \\ ██| ██  \\__/");
        text.add("| ██    ██| ██  | ██| ██  | ██| ██  | ██| ██_____/| ██        | ██ /██| ██  | ██| ██");
        text.add("|  ██████/|  ██████/| ██  | ██| ██  | ██|  ███████|  ███████  |  ████/|  ██████/| ██");
        text.add("\\______/  \\______/ |__/  |__/|__/  |__/ \\_______/ \\_______/   \\___/   \\______/ |__/");
        text.add(spacing());
        text.add(border());
        text.add(spacing());
        text.add("Developed by Aelysium | Nathan (SIVIN)");
        text.add("Use: `/rc help` to get started");
        text.add(spacing());
        text.add(border());

        return text;
    }
}




