package group.aelysium.rustyconnector.core.mcloader.lib.server_info;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.toolkit.core.server.ServerAssignment;
import group.aelysium.rustyconnector.toolkit.mc_loader.server_info.IServerInfoService;
import group.aelysium.rustyconnector.toolkit.velocity.util.AddressUtil;
import group.aelysium.rustyconnector.core.TinderAdapterForCore;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.Enumeration;
import java.util.UUID;

public class ServerInfoService implements IServerInfoService {
    private final UUID uuid = UUID.randomUUID();
    private final String displayName;
    private final InetSocketAddress address;
    private final String magicConfigPointer;
    private ServerAssignment assignment = ServerAssignment.GENERIC;

    public ServerInfoService(@NotNull UUID uuid, @NotNull String address, @NotNull String displayName, @NotNull String magicConfigPointer, int port) {
        this.magicConfigPointer = magicConfigPointer;
        this.displayName = displayName;

        if(address.isEmpty())
            this.address = convertPortToAddress(port);
        else
            this.address = AddressUtil.parseAddress(address);
    }
    public ServerInfo serverInfo() {
        return new ServerInfo(this.uuid.toString(), address);
    }

    public UUID uuid() {
        return this.uuid;
    }
    public String displayName() {
        return this.displayName;
    }
    public String address() {
        return AddressUtil.addressToString(this.address);
    }

    public int playerCount() {
        return TinderAdapterForCore.getTinder().onlinePlayerCount();
    }
    public ServerAssignment assignment() {
        return assignment;
    }
    public void assignment(ServerAssignment assignment) {
        this.assignment = assignment;
    }

    public String magicConfig() {
        return magicConfigPointer;
    }

    public void kill() {}

    private static InetSocketAddress convertPortToAddress(int port) {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = netInterface.getInetAddresses();

                while (inetAddresses.hasMoreElements()) {
                    InetAddress address = inetAddresses.nextElement();

                    try (SocketChannel socket = SocketChannel.open()) {
                        socket.bind(new InetSocketAddress(address, 0));
                        socket.connect(new InetSocketAddress("localhost", port));

                        return new InetSocketAddress(address, port);
                    } catch (IOException ignore) {}
                }
            }
        } catch (SocketException ignored) {}
        return null;
    }
}
