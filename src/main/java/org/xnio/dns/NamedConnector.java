/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, JBoss Inc., and individual contributors as indicated
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

package org.xnio.dns;

import java.nio.channels.Channel;
import java.net.InetSocketAddress;
import org.xnio.Connector;
import org.xnio.IoFuture;
import org.xnio.ChannelListener;
import org.xnio.ChannelSource;
import org.xnio.channels.BoundChannel;

/**
 * A connector which can connect by name.
 */
public interface NamedConnector<T extends Channel> extends Connector<InetSocketAddress, T> {

    /** {@inheritDoc} */
    IoFuture<T> connectTo(InetSocketAddress dest, ChannelListener<? super T> openListener, ChannelListener<? super BoundChannel<InetSocketAddress>> bindListener);

    /**
     * Establish a connection to a destination, by name.
     *
     * @param hostName the destination host name or IP address
     * @param port the destination port
     * @param openListener the handler which will be notified when the channel is open, or {@code null} for none
     * @param bindListener the handler which will be notified when the channel is bound, or {@code null} for none
     * @return the future result of this operation
     */
    IoFuture<T> connectTo(String hostName, int port, ChannelListener<? super T> openListener, ChannelListener<? super BoundChannel<InetSocketAddress>> bindListener);

    /** {@inheritDoc} */
    ChannelSource<T> createChannelSource(InetSocketAddress dest);

    /**
     * Create a client that always connects to the given destination, by name.
     *
     * @param hostName the destination host name or IP address
     * @param port the destination port
     * @return the client
     */
    ChannelSource<T> createChannelSource(String hostName, int port);
}
