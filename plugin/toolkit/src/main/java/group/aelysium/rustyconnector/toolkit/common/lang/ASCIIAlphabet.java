package group.aelysium.rustyconnector.toolkit.common.lang;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * The ascii alphabet class is used to generate large multi-line
 * representations of alphanumeric text via ASCII Characters.
 */
public interface ASCIIAlphabet {
    /**
     * Generate a component containing the ASCII representation of the provided string.
     */
    Component generate(String string);

    /**
     * Generates a component containing the ASCII representation of the provided string, also colorized.
     */
    Component generate(String string, NamedTextColor color);
}
