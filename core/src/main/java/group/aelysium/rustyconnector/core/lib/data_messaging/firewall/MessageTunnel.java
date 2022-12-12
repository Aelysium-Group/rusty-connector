package group.aelysium.rustyconnector.core.lib.data_messaging.firewall;

import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessage;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class MessageTunnel {
    private boolean hasBlacklist = false;
    private boolean hasWhitelist = false;
    private int maxLength = 512;
    private final List<InetSocketAddress> blacklist = new ArrayList<>();
    private final List<InetSocketAddress> whitelist = new ArrayList<>();

    public MessageTunnel(boolean hasBlacklist, boolean hasWhitelist, int maxLength) {
        this.hasBlacklist = hasBlacklist;
        this.hasWhitelist = hasWhitelist;
        this.maxLength = maxLength;
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
     * Validate a message.
     * This system first checks to see if the address is blacklisted. If so it returns `false`.
     * If the address is not blacklisted, it checks if the address is whitelisted. If not we return `false`.
     *
     * Returns `true` if the address is both whitelisted and not blacklisted.
     * Returns `true` if no whitelist or blacklist is defined.
     * @param message The message to check.
     * @return `true` If the message is valid. `false` if not.
     */
    public boolean validate(RedisMessage message) {
        if(message.toString().length() > this.maxLength) return false;

        if(hasBlacklist)
            if(this.blacklist.contains(message.getAddress())) return false;

        if(hasWhitelist)
            return this.whitelist.contains(message.getAddress());

        return true;
    }
}
