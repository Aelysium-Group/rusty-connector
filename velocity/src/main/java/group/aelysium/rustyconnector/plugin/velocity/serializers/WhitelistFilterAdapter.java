package group.aelysium.rustyconnector.plugin.velocity.serializers;

import group.aelysium.rustyconnector.proxy.family.whitelist.Whitelist;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.UUID;

@ConfigSerializable
public class WhitelistFilterAdapter {
    @Setting("username")
    private String username;
    @Setting("uuid")
    private String uuid;
    @Setting("ip")
    private String ip;

    public Whitelist.Filter resolveFilter() {
        UUID uuid1 = null;
        try {
            uuid1 = UUID.fromString(this.uuid);
        } catch (Exception ignore) {}

        return new Whitelist.Filter(this.username, uuid1, this.ip);
    }
}
