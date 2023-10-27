package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.config;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.model.UserPass;
import group.aelysium.rustyconnector.core.lib.model.LiquidTimestamp;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.net.InetSocketAddress;
import java.text.ParseException;
import java.util.concurrent.TimeUnit;

public class ViewportConfig extends YAML {

    private boolean enabled;
    private boolean sendURI;
    private LiquidTimestamp afkExpiration;
    private InetSocketAddress api_address;
    private boolean api_ssl;
    private UserPass credentials;

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isSendURI() {
        return sendURI;
    }

    public LiquidTimestamp getAfkExpiration() {
        return afkExpiration;
    }

    public InetSocketAddress getApi_address() {
        return api_address;
    }

    public boolean isApi_ssl() {
        return api_ssl;
    }

    public UserPass getCredentials() {
        return credentials;
    }

    public ViewportConfig(File configPointer) {
        super(configPointer);
    }

    @SuppressWarnings("unchecked")
    public void register() throws IllegalStateException {
        this.enabled = this.getNode(this.data, "enabled", Boolean.class);
        this.sendURI = this.getNode(this.data, "send-uri", Boolean.class);

        try {
            String expiration = this.getNode(this.data, "afk-expiration", String.class);
            if (expiration.equals("NEVER")) {
                this.afkExpiration = LiquidTimestamp.from(1, TimeUnit.HOURS);
                Tinder.get().logger().send(VelocityLang.BOXED_MESSAGE_COLORED.build("\"NEVER\" as a Liquid Timestamp for [afk-expiration] is not allowed! Set to default of 1 HOUR.", NamedTextColor.YELLOW));
            } else this.afkExpiration = LiquidTimestamp.from(expiration);
        } catch (ParseException e) {
            throw new IllegalStateException("You must provide a valid time value for [afk-expiration] in viewport.yml!");
        }

        this.api_address = AddressUtil.parseAddress(
                this.getNode(this.data, "api.hostname", String.class) + ":" +
                        this.getNode(this.data, "api.port", Integer.class)
        );

        this.api_ssl = this.getNode(this.data, "api.ssl", Boolean.class);

        String username = this.getNode(this.data, "credentials.username", String.class);
        String password = this.getNode(this.data, "credentials.password", String.class);
        if(username.equals("")) throw new IllegalStateException("`username` in viewport.yml can't be empty!");
        if(password.length() < 32) throw new IllegalStateException("`password` in viewport.yml can't be less than 32 character! (Remember! Viewport grants access to admin privileges so make it strong!!!)");
        this.credentials = new UserPass(username, password.toCharArray());
    }
}
