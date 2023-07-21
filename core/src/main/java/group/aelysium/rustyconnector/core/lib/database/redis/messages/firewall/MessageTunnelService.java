package group.aelysium.rustyconnector.core.lib.database.redis.messages.firewall;

import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.exception.BlockedMessageException;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class MessageTunnelService extends Service {
    private final boolean hasBlacklist;
    private final boolean hasWhitelist;
    private final int maxLength;
    private final List<InetSocketAddress> blacklist = new ArrayList<>();
    private final List<InetSocketAddress> whitelist = new ArrayList<>();

    public MessageTunnelService(boolean hasBlacklist, boolean hasWhitelist, int maxLength) {
        this.hasBlacklist = hasBlacklist;
        this.hasWhitelist = hasWhitelist;
        this.maxLength = maxLength;
    }

    public void blacklistAddress(InetSocketAddress address) {
        this.blacklist.add(address);
    }

    public void whitelistAddress(InetSocketAddress address) {
        this.whitelist.add(address);
    }

    /**
     * Validate a message.
     * This system first checks to see if the address is blacklisted. If so it returns `false`.
     * If the address is not blacklisted, it checks if the address is whitelisted. If not we return `false`.
     *
     * Succeeds if the address is both whitelisted and not blacklisted.
     * Succeeds if no whitelist or blacklist is defined.
     * @param message The message to check.
     * @throws BlockedMessageException If the message should be blocked.
     */
    public void validate(GenericRedisMessage message) throws BlockedMessageException {
        if(message.getMessageVersion() > GenericRedisMessage.getProtocolVersion())
            throw new BlockedMessageException("The incoming message contained a protocol version greater than expected! " + message.getMessageVersion() + " > " + GenericRedisMessage.getProtocolVersion() + ". Make sure you are using the same version of RustyConnector on your proxy and sub-servers!");
        if(message.getMessageVersion() < GenericRedisMessage.getProtocolVersion())
            throw new BlockedMessageException("The incoming message contained a protocol version that was less than expected! " + message.getMessageVersion() + " < " + GenericRedisMessage.getProtocolVersion() + ". Make sure you are using the same version of RustyConnector on your proxy and sub-servers!");

        if(message.toString().length() > this.maxLength)
            throw new BlockedMessageException("The message is to long!");

        if(hasBlacklist)
            if(this.blacklist.contains(message.getAddress()))
                throw new BlockedMessageException("The message was sent from a blacklisted IP Address!");

        if(hasWhitelist)
            if(!this.whitelist.contains(message.getAddress()))
                throw new BlockedMessageException("The message was sent from an IP Address that isn't whitelisted!");
    }

    @Override
    public void kill() {
        this.blacklist.clear();
        this.whitelist.clear();
    }
}
