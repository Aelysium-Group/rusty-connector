package group.aelysium.rustyconnector.plugin.velocity.lib.module;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;

import java.util.Arrays;
import java.util.Locale;

public class Permission {
    /**
     * Checks of the player has the defined permissions.
     * Will return `true` even if only one of these permissions is valid.
     * @param player The player to validate
     * @param nodes The permissions to check for
     * @return `true` If the player has any one of the defined permissions. `false` If the player has none of them.
     */
    public static boolean validate(Player player, String... nodes) {
        for (String node : nodes) {
            String nodeToLower = node.toLowerCase(Locale.ROOT);

            if(player.hasPermission(nodeToLower)) return true;

            /*
             * Check for wildcard variants of permissions like: rustyconnector.* or rustyconnector.admin.*
             */
            String adjustedNode = nodeToLower.replaceFirst("[A-z\\_\\-]*$","*");
            if(player.hasPermission(adjustedNode)) return true;
        }
        return false;
    }

    /**
     * Construct a permission node using insertion nodes. Define your string to be inserted into.
     * By surrounding a string with `<>` brackets you can mark to be replaced.
     * <h3>Example:</h3>
     * `rustyconnector.<server name>.access`
     *
     * Insertion points are replaced with the defined values in the order that they are defined.
     * @param pattern The pattern to change
     * @param insertions The insertions to add
     * @return
     */
    public static String constructNode(String pattern, String... insertions) {
        for (String node : insertions)
            pattern = pattern.replaceFirst("\\<[A-z\\s]*\\>",node);
        return pattern;
    }
}
