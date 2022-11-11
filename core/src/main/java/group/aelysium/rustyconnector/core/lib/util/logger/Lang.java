package group.aelysium.rustyconnector.core.lib.util.logger;

import java.util.*;

public class Lang {
    private static Map<LangKey, LangEntry> langMappings = new HashMap<>();

    public static LangEntry get(LangKey key) {
        return langMappings.get(key);
    }
    public static void add(LangKey key, LangEntry value) {
        langMappings.put(key, value);
    }

    public void getDynamic() {}

    public static LangEntry boxedMessage(String... lines) {
        List<String> text = new ArrayList<>();
        text.add(border());
        text.add(spacing());
        text.addAll(Arrays.asList(lines));
        text.add(spacing());
        text.add(border());
        return new LangEntry(text);
    }

    public static String border() {
        return  "█████████████████████████████████████████████████████████████████████████████████████████████████";
    }
    public static String spacing() {
        return "";
    }

    public static LangEntry registeredFamilies() { // font: ANSI Shadow
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

        return new LangEntry(text);
    }

    public static LangEntry registeredServers(String[] params) { // font: ANSI Shadow
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

        return new LangEntry(text);
    }

    public static LangEntry info() { // font: ANSI Shadow
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

        return new LangEntry(text);
    }

    public static LangEntry commandUsage() { // font: ANSI Shadow
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

        return new LangEntry(text);
    }

    public static LangEntry wordmark() {
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
        text.add("Use: `/rc` to get started");
        text.add(spacing());
        text.add(border());

        return new LangEntry(text);
    }
}




