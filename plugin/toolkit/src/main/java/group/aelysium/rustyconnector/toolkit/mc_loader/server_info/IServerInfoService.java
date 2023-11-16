package group.aelysium.rustyconnector.toolkit.mc_loader.server_info;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

import java.util.UUID;

public interface IServerInfoService extends Service {
    /**
     * Gets the info of this server.
     * @return {@link ServerInfo}
     */
    ServerInfo serverInfo();

    /**
     * Gets the address of this server.
     * The address, assuming the user entered it properly, should be formatted in the same format as you format a joinable address in Velocity's velocity.toml.
     * @return {@link String}
     */
    String address();

    /**
     * Gets the name of the magic config this server is going to use.
     * @return {@link String}
     */
    String magicConfig();

    /**
     * The number of players on this server.
     * @return {@link Integer}
     */
    int playerCount();

    /**
     * Gets the session uuid of this server.
     * The server's uuid won't change while it's alive, but once it's restarted or reloaded, the session uuid will change.
     * @return {@link UUID}
     */
    UUID sessionUUID();
}
