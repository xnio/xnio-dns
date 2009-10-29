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
import org.jboss.xnio.FutureResult;
import java.util.Set;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.concurrent.Executor;
import java.io.IOException;

public final class CachingResolver extends AbstractResolver implements Resolver {

    private final Map<QueryKey, FutureResult<Answer>> cache;
    private final Resolver realResolver;
    private final Executor executor;

    public CachingResolver(final Resolver resolver, final Executor executor, final int cacheSize) {
        cache = new CacheMap<QueryKey, FutureResult<Answer>>(cacheSize);
        realResolver = resolver;
        this.executor = executor;
    }

    /** {@inheritDoc} */
    public IoFuture<Answer> resolve(final Domain name, final RRClass rrClass, final RRType rrType, final Set<ResolverFlag> flags) {
        if (flags.contains(ResolverFlag.BYPASS_CACHE)) {
            // skip the cache, do not record results
            return realResolver.resolve(name, rrClass, rrType, flags);
        } else {
            final QueryKey key = new QueryKey(name, rrClass, rrType);
            final FutureResult<Answer> newAnswer;
            synchronized (cache) {
                final FutureResult<Answer> future = cache.get(key);
                if (future != null) {
                    final IoFuture.Status status = future.getIoFuture().getStatus();
                    if (status == IoFuture.Status.WAITING) {
                        // still waiting for result
                        return future.getIoFuture();
                    } else if (status == IoFuture.Status.DONE) {
                        try {
                            boolean expired = false;
                            for (Record record : future.getIoFuture().get().getAnswerRecords()) {
                                if (record.getTtlSpec().isExpired()) {
                                    expired = true;
                                    break;
                                }
                            }
                            if (! expired) {
                                return future.getIoFuture();
                            }
                        } catch (IOException e) {
                            // fall out and re-query, the future has gone bad
                            // technically shouldn't be possible because status was "done"
                        }
                    }
                }
                newAnswer = new FutureResult<Answer>(executor);
                cache.put(key, newAnswer);
            }
            final IoFuture<Answer> realFuture = realResolver.resolve(name, rrClass, rrType, flags);
            realFuture.addNotifier(new IoFuture.HandlingNotifier<Answer, FutureResult<Answer>>() {
                public void handleCancelled(final FutureResult<Answer> attachment) {
                    synchronized (cache) {
                        if (cache.get(key).equals(attachment)) {
                            cache.remove(key);
                        }
                    }
                    attachment.setCancelled();
                }

                public void handleFailed(final IOException exception, final FutureResult<Answer> attachment) {
                    synchronized (cache) {
                        if (cache.get(key).equals(attachment)) {
                            cache.remove(key);
                        }
                    }
                    attachment.setException(exception);
                }

                public void handleDone(final Answer result, final FutureResult<Answer> attachment) {
                    attachment.setResult(result);
                }
            }, newAnswer);
            return newAnswer.getIoFuture();
        }
    }

    private static final class CacheMap<K, V> extends LinkedHashMap<K, V> {

        private static final long serialVersionUID = 733255475501069288L;

        private final int max;

        CacheMap(final int max) {
            super(64, 0.6f, true);
            this.max = max;
        }

        protected boolean removeEldestEntry(final Map.Entry<K, V> eldest) {
            return size() > max;
        }
    }
}
