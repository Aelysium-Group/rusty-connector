package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.rustyconnector.common.config.Config;
import group.aelysium.rustyconnector.common.config.ConfigLoader;
import group.aelysium.rustyconnector.common.config.Node;
import group.aelysium.rustyconnector.common.config.PathParameter;
import group.aelysium.rustyconnector.proxy.family.scalar_family.ScalarFamily;

import java.io.IOException;
import java.text.ParseException;

@Config("plugins/rustyconnector/scalar_families/{id}.yml")
public class ScalarFamilyConfig {
    @PathParameter("id")
    private String id;

    @Node(order = 0, key = "display-name", defaultValue = "")
    private String displayName;

    @Node(order = 1, key = "parent-family", defaultValue = "")
    private String parentFamily;

    @Node(order = 2, key = "load-balancer", defaultValue = "default")
    private String loadBalancer = "default";

    @Node(order = 3, key = "whitelist", defaultValue = "")
    private String whitelist;

    public ScalarFamily.Tinder tinder() throws IOException, ParseException {
        return new ScalarFamily.Tinder(
                id,
                displayName.isEmpty() ? null : displayName,
                parentFamily.isEmpty() ? null : parentFamily,
                whitelist.isEmpty() ? null : WhitelistConfig.New(whitelist).tinder(),
                LoadBalancerConfig.New(loadBalancer).tinder()
        );
    }

    public static ScalarFamilyConfig New(String familyID) throws IOException {
        return ConfigLoader.load(ScalarFamilyConfig.class, familyID);
    }
}