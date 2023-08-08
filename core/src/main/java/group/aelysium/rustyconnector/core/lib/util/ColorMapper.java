package group.aelysium.rustyconnector.core.lib.util;

import net.kyori.adventure.text.format.NamedTextColor;

public class ColorMapper {
    /**
     * Generate a NamedTextColor from the string representation of the color.
     * @param name The neame to search for.
     * @return A NamedTextColor or `null` if one couldn't be found.
     */
    public static NamedTextColor map(String name) {
        switch (name.toUpperCase()) {
            case "BLACK" -> { return NamedTextColor.BLACK; }
            case "DARK_BLUE" -> { return NamedTextColor.DARK_BLUE; }
            case "DARK_GREEN" -> { return NamedTextColor.DARK_GREEN; }
            case "DARK_AQUA" -> { return NamedTextColor.DARK_AQUA; }
            case "DARK_RED" -> { return NamedTextColor.DARK_RED; }
            case "DARK_PURPLE" -> { return NamedTextColor.DARK_PURPLE; }
            case "GOLD" -> { return NamedTextColor.GOLD; }
            case "GRAY" -> { return NamedTextColor.GRAY; }
            case "DARK_GRAY" -> { return NamedTextColor.DARK_GRAY; }
            case "BLUE" -> { return NamedTextColor.BLUE; }
            case "GREEN" -> { return NamedTextColor.GREEN; }
            case "AQUA" -> { return NamedTextColor.AQUA; }
            case "RED" -> { return NamedTextColor.RED; }
            case "LIGHT_PURPLE" -> { return NamedTextColor.LIGHT_PURPLE; }
            case "YELLOW" -> { return NamedTextColor.YELLOW; }
            case "WHITE" -> { return NamedTextColor.WHITE; }
        }
        return null;
    }
}
