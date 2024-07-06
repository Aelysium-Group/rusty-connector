/*
 * Copyright (C) 2018 Velocity Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/*
 * Code courtesy of the Velocity Contributors.
 * https://github.com/PaperMC/Velocity/edit/dev/3.0.0/proxy/src/main/java/com/velocitypowered/proxy/util/AddressUtil.java
 */

package group.aelysium.rustyconnector.toolkit.proxy.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;

public final class AddressUtil {
    private static final int DEFAULT_MINECRAFT_PORT = 25565;

    private AddressUtil() {
        throw new AssertionError();
    }

    /**
     * Attempts to parse an IP address of the form {@code 127.0.0.1:25565}. The returned
     * {@link InetSocketAddress} is not resolved.
     *
     * @param ip the IP to parse
     * @return the parsed address
     */
    public static InetSocketAddress parseAddress(String ip) {
        Preconditions.checkNotNull(ip, "ip");
        URI uri = URI.create("tcp://" + ip);
        if (uri.getHost() == null) {
            throw new IllegalStateException("Invalid hostname/IP " + ip);
        }

        int port = uri.getPort() == -1 ? DEFAULT_MINECRAFT_PORT : uri.getPort();
        try {
            InetAddress ia = InetAddresses.forUriString(uri.getHost());
            return new InetSocketAddress(ia, port);
        } catch (IllegalArgumentException e) {
            return InetSocketAddress.createUnresolved(uri.getHost(), port);
        }
    }

    /**
     * Attempts to parse an IP address of the form {@code 127.0.0.1:25565}. The returned
     * {@link InetSocketAddress} is resolved.
     *
     * @param ip the IP to parse
     * @return the parsed address
     */
    public static InetSocketAddress parseAndResolveAddress(String ip) {
        Preconditions.checkNotNull(ip, "ip");
        URI uri = URI.create("tcp://" + ip);
        if (uri.getHost() == null) {
            throw new IllegalStateException("Invalid hostname/IP " + ip);
        }

        int port = uri.getPort() == -1 ? DEFAULT_MINECRAFT_PORT : uri.getPort();
        return new InetSocketAddress(uri.getHost(), port);
    }

    /**
     * Converts an address into a string of format {@code 127.0.0.1:25565}.
     * @param address The address to convert.
     * @return A string of format {@code 127.0.0.1:25565}.
     */
    public static String addressToString(InetSocketAddress address) {
        return address.getHostName() +":"+ address.getPort();
    }
}
