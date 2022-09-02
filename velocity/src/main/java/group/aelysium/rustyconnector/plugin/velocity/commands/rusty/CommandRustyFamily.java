package group.aelysium.rustyconnector.plugin.velocity.commands.rusty;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerFamily;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import rustyconnector.generic.lib.generic.Lang;
import rustyconnector.generic.lib.generic.server.Family;

public class CommandRustyFamily {
    public static void execute(final CommandSource source, final VelocityRustyConnector plugin, final String[] args) {

        if(args.length > 1) // /rc family [[[xxx]]] xxx xxx
            switch (args[1].toLowerCase()) {
                case "list": // /rc family list
                    CommandRustyFamily.listFamilies(source, plugin);
                    return;
                case "reload":
                    return;
                case "reloadall":
                    return;
                default:
                    if(args.length > 2) {
                        plugin.getProxy().getRegisteredFamilies().forEach(family -> {
                            if(args[2].equals(family.getName())) familyDetails(plugin, family);
                            return;
                        });
                    }
                    return;
            }

        // /rc family list
        source.sendMessage(Component.text("Usage:").color(NamedTextColor.RED));
        source.sendMessage(Component.text("/rc family list").color(NamedTextColor.AQUA));
        source.sendMessage(Component.text("/rc family reload").color(NamedTextColor.AQUA));
        source.sendMessage(Component.text("/rc family reloadAll").color(NamedTextColor.AQUA));
        source.sendMessage(Component.text("/rc family <family name>").color(NamedTextColor.AQUA));
    }

    public static void listFamilies(CommandSource source, VelocityRustyConnector plugin) {
        Lang.print(plugin.logger(), Lang.get("registered-families"));
        plugin.logger().log(Lang.spacing());
        plugin.getProxy().getRegisteredFamilies().forEach(family -> {
            plugin.logger().log("   ---| "+family.getName());
        });
        plugin.logger().log(Lang.spacing());
        plugin.logger().log("To see more details about a particular family use:");
        plugin.logger().log("/rc family <family name> info");
        plugin.logger().log(Lang.spacing());
        plugin.logger().log(Lang.border());
    }
    public static void familyDetails(VelocityRustyConnector plugin, ServerFamily family) {
        Lang.print(plugin.logger(), Lang.get("info"));
        plugin.logger().log(Lang.spacing());
        plugin.logger().log("   ---| Name: "+family.getName());
        plugin.logger().log("   ---| Online Players: "+family.playerCount());
        plugin.logger().log("   ---| Registered Servers: "+family.serverCount());
        plugin.logger().log("   ---| Load Balancing Algorithm: "+family.algorithm());
        plugin.logger().log(Lang.spacing());
        plugin.logger().log(Lang.border());
    }
}
