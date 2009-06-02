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

import org.jboss.xnio.IoFuture;
import java.util.Set;
import java.util.List;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.SocketAddress;

/**
 *
 */
public interface Resolver {
    IoFuture<Answer> resolve(InetAddress server, Domain name, RRClass rrClass, RRType rrType, Set<Flag> flags);

    IoFuture<Answer> resolve(SocketAddress server, Domain name, RRClass rrClass, RRType rrType, Set<Flag> flags);

    IoFuture<Answer> resolve(Domain name, RRClass rrClass, RRType rrType, Set<Flag> flags);

    IoFuture<Answer> resolve(Domain name, RRClass rrClass, RRType rrType);

    IoFuture<Answer> resolve(Domain name, RRType rrType);

    IoFuture<List<InetAddress>> resolveAllInet(Domain name);

    IoFuture<InetAddress> resolveInet(Domain name);

    IoFuture<List<Inet4Address>> resolveAllInet4(Domain name);

    IoFuture<Inet4Address> resolveInet4(Domain name);

    IoFuture<List<Inet6Address>> resolveAllInet6(Domain name);

    IoFuture<Inet6Address> resolveInet6(Domain name);

    IoFuture<Domain> resolveReverse(InetAddress address);

    IoFuture<List<String>> resolveText(Domain name);

    interface Flag {}

    enum StandardFlag implements Flag {

        /**
         * Bypass the cache (if any).
         */
        NO_CACHE,
        /**
         * Bypass recursion.
         */
        NO_RECURSION,
        /**
         * Use TCP.
         */
        USE_TCP,
    }
}
