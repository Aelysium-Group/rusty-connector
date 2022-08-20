package group.aelysium.rustyconnector.plugin.velocity.lib.generic;

public class Proxy extends rustyconnector.generic.lib.generic.server.Proxy {
    public Proxy(String privateKey) {
        super(privateKey);
    }

    @Override
    public void init() {
        this.requestGlobalRegistration();
    }
}
