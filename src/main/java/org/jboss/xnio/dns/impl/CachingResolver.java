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

package org.jboss.xnio.dns.impl;

import org.jboss.xnio.dns.NetworkResolver;
import org.jboss.xnio.dns.Answer;
import org.jboss.xnio.dns.RRClass;
import org.jboss.xnio.dns.RRType;
import org.jboss.xnio.dns.Domain;
import org.jboss.xnio.dns.AbstractNetworkResolver;
import static org.jboss.xnio.dns.impl.ConcurrentReferenceHashMap.DEFAULT_CONCURRENCY_LEVEL;
import static org.jboss.xnio.dns.impl.ConcurrentReferenceHashMap.DEFAULT_INITIAL_CAPACITY;
import static org.jboss.xnio.dns.impl.ConcurrentReferenceHashMap.DEFAULT_LOAD_FACTOR;
import static org.jboss.xnio.dns.impl.ConcurrentReferenceHashMap.ReferenceType.*;
import org.jboss.xnio.IoFuture;
import java.util.Set;
import java.util.EnumSet;
import java.util.EnumMap;
import java.util.concurrent.ConcurrentMap;
import java.net.SocketAddress;

public final class CachingResolver extends AbstractNetworkResolver implements NetworkResolver {

    private final EnumMap<RRClass, ConcurrentMap<Domain, CacheEntry>> caches;
    private final NetworkResolver realResolver;
    private static final CachingNotifier cachingNotifier = new CachingNotifier();

    public CachingResolver(final NetworkResolver resolver) {
        final EnumMap<RRClass, ConcurrentMap<Domain, CacheEntry>> caches = new EnumMap<RRClass, ConcurrentMap<Domain, CacheEntry>>(RRClass.class);
        for (RRClass rrClass : RRClass.values()) {
            caches.put(rrClass, new ConcurrentReferenceHashMap<Domain, CacheEntry>(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL, STRONG, SOFT, EnumSet.noneOf(ConcurrentReferenceHashMap.Option.class)));
        }
        this.caches = caches;
        realResolver = resolver;
    }

    public SocketAddress getDefaultServerAddress() {
        return realResolver.getServerAddress();
    }

    public IoFuture<Answer> resolve(final SocketAddress server, final Domain name, final RRClass rrClass, final RRType rrType, final Set<Flag> flags) {
        return null;
    }

    public IoFuture<Answer> resolve(Domain name, final RRClass rrClass, final RRType rrType, final Set<Flag> flags) {
        for (;;) {
            final ConcurrentMap<Domain, CacheEntry> cache = caches.get(rrClass);
            final CacheEntry entry = cache.get(name);
            if (entry == null) {
                final IoFuture<Answer> futureResult = realResolver.resolve(name, rrClass, rrType, flags);
                futureResult.addNotifier(cachingNotifier, this);
                return ;
            }

            if (rrType == RRType.ANY) {
            }
        }
    }

    private static class CachingNotifier extends IoFuture.HandlingNotifier<Answer, CachingResolver> {

        public void handleDone(final Answer result, final CachingResolver attachment) {

        }
    }
}
