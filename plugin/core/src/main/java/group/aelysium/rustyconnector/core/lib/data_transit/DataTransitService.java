package group.aelysium.rustyconnector.core.lib.data_transit;

import group.aelysium.rustyconnector.toolkit.core.packet.Packet;
import group.aelysium.rustyconnector.core.lib.exception.BlockedMessageException;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class DataTransitService implements Service {
    private final boolean hasBlacklist;
    private final boolean hasWhitelist;
    private final List<InetSocketAddress> blacklist = new ArrayList<>();
    private final List<InetSocketAddress> whitelist = new ArrayList<>();

    public DataTransitService(boolean hasBlacklist, boolean hasWhitelist) {
        this.hasBlacklist = hasBlacklist;
        this.hasWhitelist = hasWhitelist;
    }

    public void blacklistAddress(InetSocketAddress address) {
        this.blacklist.add(address);
    }

    public void whitelistAddress(InetSocketAddress address) {
        this.whitelist.add(address);
    }

    /**
     * Validate a message.
     * @param message The message to check.
     * @throws BlockedMessageException If the message should be blocked.
     */
    public void validate(Packet message) throws BlockedMessageException, NoOutputException {
        if(message.messageVersion() > Packet.protocolVersion())
            throw new BlockedMessageException("The incoming message contained a protocol version greater than expected! " + message.messageVersion() + " > " + Packet.protocolVersion() + ". Make sure you are using the same version of RustyConnector on your proxy and sub-servers!");
        if(message.messageVersion() < Packet.protocolVersion())
            throw new BlockedMessageException("The incoming message contained a protocol version that was less than expected! " + message.messageVersion() + " < " + Packet.protocolVersion() + ". Make sure you are using the same version of RustyConnector on your proxy and sub-servers!");

        if(hasBlacklist)
            if(this.blacklist.contains(message.sender()))
                throw new BlockedMessageException("The message was sent from a blacklisted IP Address!");

        if(hasWhitelist)
            if(!this.whitelist.contains(message.sender()))
                throw new BlockedMessageException("The message was sent from an IP Address that isn't whitelisted!");
    }

    @Override
    public void kill() {
        this.blacklist.clear();
        this.whitelist.clear();
    }
}
