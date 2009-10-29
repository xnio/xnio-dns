/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.xnio.dns;

import java.net.InetAddress;
import java.net.Inet6Address;
import java.net.Inet4Address;

/**
 * DNS general utility methods.
 */
public final class DNS {

    private DNS() {}

    /**
     * Parse an IP address string.
     *
     * @param hostName the host name to use for the resultant {@code InetAddress} object
     * @param addressString the address string to parse
     * @return the address
     * @throws AddressParseException if the address string is not a valid IP address
     */
    public static InetAddress parseInetAddress(String hostName, String addressString) throws AddressParseException {
        return IPParserImpl.parseAddress(IPParserImpl.Kind.IP, hostName, addressString);
    }

    /**
     * Parse an IPv4 address string.
     *
     * @param hostName the host name to use for the resultant {@code Inet4Address} object
     * @param addressString the address string to parse
     * @return the address
     * @throws AddressParseException if the address string is not a valid IPv4 address
     */
    public static Inet4Address parseInet4Address(String hostName, String addressString) throws AddressParseException {
        return (Inet4Address) IPParserImpl.parseAddress(IPParserImpl.Kind.IPv4, hostName, addressString);
    }

    /**
     * Parse an IPv6 address string.
     *
     * @param hostName the host name to use for the resultant {@code Inet6Address} object
     * @param addressString the address string to parse
     * @return the address
     * @throws AddressParseException if the address string is not a valid IPv6 address
     */
    public static Inet6Address parseInet6Address(String hostName, String addressString) throws AddressParseException {
        return (Inet6Address) IPParserImpl.parseAddress(IPParserImpl.Kind.IPv6, hostName, addressString);
    }
}
