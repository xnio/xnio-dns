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

import java.net.SocketAddress;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.jboss.xnio.IoFuture;

/**
 * A resolver which delegates to one or more remote servers.
 */
public final class DelegatingResolver extends AbstractResolver {
    private final NetworkResolver networkResolver;
    private final SocketAddress[] servers;
    private final AtomicInteger cnt = new AtomicInteger();

    /**
     * Construct a new instance.
     *
     * @param networkResolver the network resolver
     * @param servers the array of servers to delegate to
     */
    public DelegatingResolver(final NetworkResolver networkResolver, final SocketAddress[] servers) {
        this.networkResolver = networkResolver;
        this.servers = servers;
    }

    /** {@inheritDoc} */
    public IoFuture<Answer> resolve(final Domain name, final RRClass rrClass, final RRType rrType, final Set<ResolverFlag> flags) {
        final SocketAddress[] servers = this.servers;
        final SocketAddress serverAddress = servers[(cnt.getAndIncrement() & 0x7fffffff) % servers.length];
        return networkResolver.resolve(serverAddress, name, rrClass, rrType, flags);
    }
}
