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

import org.jboss.xnio.ChannelSource;
import org.jboss.xnio.IoFuture;
import org.jboss.xnio.ChannelListener;
import org.jboss.xnio.UdpServer;
import org.jboss.xnio.IoUtils;
import org.jboss.xnio.channels.UdpChannel;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.io.Serializable;
import java.util.Random;
import java.util.Arrays;
import java.util.Comparator;
import static java.lang.Integer.signum;

/**
 * A UDP channel source which randomly selects ports.  No checks are made to verify that the port is not already
 * bound; it is the user's responsibility to check for errors and retry.
 */
public final class RandomUdpChannelSource implements ChannelSource<UdpChannel> {
    private final UdpServer udpServer;
    private final InetAddress bindAddress;
    private final PortRange[] portRanges;
    private final int portCount;
    private final Random random;

    /**
     * Construct a new instance.
     *
     * @param udpServer the UDP server to acquire channels from
     * @param portRanges the port ranges to choose from
     */
    public RandomUdpChannelSource(final UdpServer udpServer, final PortRange... portRanges) {
        this(udpServer, null, new Random(), portRanges);
    }

    /**
     * Construct a new instance.
     *
     * @param udpServer the UDP server to acquire channels from
     * @param random the RNG to use to choose ports
     * @param portRanges the port ranges to choose from
     */
    public RandomUdpChannelSource(final UdpServer udpServer, final Random random, final PortRange... portRanges) {
        this(udpServer, null, random, portRanges);
    }

    /**
     * Construct a new instance.
     *
     * @param udpServer the UDP server to acquire channels from
     * @param bindAddress the IP address to bind to
     * @param portRanges the port ranges to choose from
     */
    public RandomUdpChannelSource(final UdpServer udpServer, final InetAddress bindAddress, final PortRange... portRanges) {
        this(udpServer, bindAddress, new Random(), portRanges);
    }

    /**
     * Construct a new instance.
     *
     * @param udpServer the UDP server to acquire channels from
     * @param bindAddress the IP address to bind to
     * @param random the RNG to use to choose ports
     * @param portRanges the port ranges to choose from
     */
    public RandomUdpChannelSource(final UdpServer udpServer, final InetAddress bindAddress, final Random random, final PortRange... portRanges) {
        this.bindAddress = bindAddress;
        this.udpServer = udpServer;
        this.portRanges = portRanges.clone();
        Arrays.sort(this.portRanges, BY_DESCENDING_SIZE);
        this.random = random;
        int cnt = 0;
        for (PortRange portRange : portRanges) {
            cnt += portRange.getPortCount();
            if (cnt < 0) {
                throw new IllegalArgumentException("Too many ports");
            }
        }
        portCount = cnt;
    }

    private int getPort(int index) {
        for (PortRange portRange : portRanges) {
            if (index < portRange.getPortCount()) {
                return portRange.getStartPort() + index;
            }
        }
        throw new IllegalStateException();
    }

    private InetSocketAddress getAddress() {
        final InetAddress bindAddress = this.bindAddress;
        final int port = getPort(random.nextInt(portCount));
        if (bindAddress == null) {
            return new InetSocketAddress(port);
        } else {
            return new InetSocketAddress(bindAddress, port);
        }
    }

    /** {@inheritDoc}  The source port of the channel is randomly selected according to the given port ranges. */
    public IoFuture<? extends UdpChannel> open(final ChannelListener<? super UdpChannel> openListener) {
        return udpServer.bind(getAddress()).addNotifier(IoUtils.<UdpChannel>channelListenerNotifier(), openListener);
    }

    /**
     * Create a port range instance.  Suitable for static imports.
     *
     * @param start the start port
     * @param count the number of ports in the range
     * @return the port range
     */
    public static PortRange portRange(int start, int count) {
        return new PortRange(start, count);
    }

    /**
     * A port range to bind to.  Use the {@link org.jboss.xnio.dns.RandomUdpChannelSource#portRange(int, int)} method
     * to construct instances.
     */
    public static final class PortRange implements Serializable, Comparable<PortRange> {

        private static final long serialVersionUID = -8095304791835413542L;

        private final int startPort;
        private final int portCount;

        private PortRange(final int startPort, final int portCount) {
            if (startPort < 0 || startPort > 65535) {
                throw new IllegalArgumentException("Invalid start port number");
            }
            this.startPort = startPort;
            if (portCount < 1 || portCount > 65536-startPort) {
                throw new IllegalArgumentException("Invalid port count");
            }
            this.portCount = portCount;
        }

        /** {@inheritDoc} */
        public int compareTo(final PortRange o) {
            return signum(startPort - o.startPort);
        }

        /**
         * Get the start port number.
         *
         * @return the start port number
         */
        public int getStartPort() {
            return startPort;
        }

        /**
         * Get the number of ports in this range.
         *
         * @return the number of ports in this range
         */
        public int getPortCount() {
            return portCount;
        }
    }

    private static final Comparator<PortRange> BY_DESCENDING_SIZE = new Comparator<PortRange>() {
        public int compare(final PortRange o1, final PortRange o2) {
            return signum(o2.getPortCount() - o1.getPortCount());
        }
    };
}
