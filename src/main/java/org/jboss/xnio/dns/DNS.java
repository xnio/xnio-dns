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
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public final class DNS {
    private DNS() {}

    private static final Pattern IP_ADDRESS_PATTERN = Pattern.compile(
            // IPv4 dotted-decimal
            "((\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.\\(d{1,3}))|" +
            // IPv4 hex string
            "([0-9a-fA-F]{1,8})|" +
            // IPv6 full form
            "((?:[0-9a-fA-F]{1,4}:){7}["
    );

    public static InetAddress parse(String addressString) {
        final Matcher matcher = IP_ADDRESS_PATTERN.matcher(addressString);
        if (matcher.matches()) {
            String group;
            if ((group = matcher.group(1)) != null) {
                int a = Integer.parseInt(matcher.group(2));
                int b = Integer.parseInt(matcher.group(3));
                int c = Integer.parseInt(matcher.group(4));
                int d = Integer.parseInt(matcher.group(5));

            } else if ((group = matcher.group(6)) != null) {

            }
        }
        throw new AddressParseException("Not a valid IP address");
    }
}
