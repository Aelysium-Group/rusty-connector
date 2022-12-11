package group.aelysium.rustyconnector.core.lib.data_messaging.firewall;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class MessageTunnel {
    private boolean hasBlacklist = false;
    private boolean hasWhitelist = false;
    private final List<InetSocketAddress> blacklist = new ArrayList<>();
    private final List<InetSocketAddress> whitelist = new ArrayList<>();

    public MessageTunnel(boolean hasBlacklist, boolean hasWhitelist) {
        this.hasBlacklist = hasBlacklist;
        this.hasWhitelist = hasWhitelist;
    }

    public void blacklistAddress(InetSocketAddress address) {
        this.blacklist.add(address);
    }
    public void blacklistAddress(String hostname, int port) {
        InetSocketAddress address = new InetSocketAddress(hostname, port);

        this.blacklist.add(address);
    }

    public void whitelistAddress(InetSocketAddress address) {
        this.whitelist.add(address);
    }
    public void whitelistAddress(String hostname, int port) {
        InetSocketAddress address = new InetSocketAddress(hostname, port);

        this.whitelist.add(address);
    }

    /**
     * Validate an address.
     * This system first checks to see if the address is blacklisted. If so it returns `false`.
     * If the address is not blacklisted, it checks if the address is whitelisted. If not we return `false`.
     *
     * Returns `true` if the address is both whitelisted and not blacklisted.
     * Returns `true` if no whitelist or blacklist is defined.
     * @param address An address to check.
     * @return `true` If the address is valid. `false` if not.
     */
    public boolean validate(InetSocketAddress address) {
        if(hasBlacklist)
            if(this.blacklist.contains(address)) return false;

        if(hasWhitelist)
            return this.whitelist.contains(address);

        return true;
    }
}
