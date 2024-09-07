package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.rustyconnector.common.config.Comment;
import group.aelysium.rustyconnector.common.config.Config;
import group.aelysium.rustyconnector.common.config.ConfigLoader;
import group.aelysium.rustyconnector.common.config.Node;
import group.aelysium.rustyconnector.proxy.family.whitelist.Whitelist;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

@Config("proxy_whitelist.yml")
@Comment({
        "############################################################",
        "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
        "#                     Proxy Whitelist                      #",
        "#                                                          #",
        "#               ---------------------------                #",
        "# | The Proxy whitelist will affect all player connections #",
        "# | across the entire proxy.                               #",
        "#               ---------------------------                #",
        "#                                                          #",
        "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
        "############################################################"
})
public class ProxyWhitelistConfig {
    @Comment({
        "#",
        "# If you set this to be enabled. You will be turning on a whitelist which will affect your entire proxy!",
        "# If you only want to affect certain families or individual servers. Look into the appropriate configuration locations!",
        "#"
    })
    @Node(order = 0, key = "enabled", defaultValue = "false")
    private boolean enabled;

    @Comment({
        "#",
        "# The name of the whitelist to load from the `whitelists` folder.",
        "#"
    })
    @Node(order = 1, key = "name", defaultValue = "default")
    private String name;

    public @Nullable Whitelist.Tinder tinder() throws IOException {
        if(!this.enabled) return null;

        return WhitelistConfig.New(name).tinder();
    }

    public static ProxyWhitelistConfig New() throws IOException {
        return ConfigLoader.load(ProxyWhitelistConfig.class);
    }
}
