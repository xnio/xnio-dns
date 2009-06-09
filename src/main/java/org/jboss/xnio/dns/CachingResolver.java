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

import static org.jboss.xnio.dns.ConcurrentReferenceHashMap.DEFAULT_INITIAL_CAPACITY;
import static org.jboss.xnio.dns.ConcurrentReferenceHashMap.DEFAULT_LOAD_FACTOR;
import static org.jboss.xnio.dns.ConcurrentReferenceHashMap.DEFAULT_CONCURRENCY_LEVEL;
import static org.jboss.xnio.dns.ConcurrentReferenceHashMap.ReferenceType.STRONG;
import static org.jboss.xnio.dns.ConcurrentReferenceHashMap.ReferenceType.SOFT;
import static org.jboss.xnio.dns.ConcurrentReferenceHashMap.Option;
import org.jboss.xnio.IoFuture;
import org.jboss.xnio.AbstractIoFuture;
import java.util.Set;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.io.IOException;
import static java.lang.Math.min;
import static java.lang.Math.max;

public final class CachingResolver extends AbstractResolver implements Resolver {

    private final ConcurrentMap<CacheKey, FutureCacheValue> cache;
    private final Resolver realResolver;

    private static <K, V> ConcurrentMap<K, V> cacheMap() {
        return new ConcurrentReferenceHashMap<K, V>(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL, STRONG, SOFT, EnumSet.noneOf(Option.class));
    }

    public CachingResolver(final Resolver resolver) {
        cache = cacheMap();
        realResolver = resolver;
    }

    private FutureCacheValue getOrCreate(final CacheKey key, final Domain name, final RRClass rrClass, final RRType rrType, final Set<ResolverFlag> flags) {
        final FutureCacheValue result = cache.get(key);
        if (result != null) {
            return result;
        }
        final FutureCacheValue newResult = new FutureCacheValue();
        final FutureCacheValue oldResult = cache.putIfAbsent(key, newResult);
        if (oldResult == null) {
            final FutureAnswer ourFutureAnswer = new FutureAnswer();
            final IoFuture<Answer> futureAnswer = realResolver.resolve(name, rrClass, rrType, flags);
            futureAnswer.addNotifier(new IoFuture.HandlingNotifier<Answer, FutureAnswer>() {
                public void handleDone(final Answer result, final FutureAnswer futureAnswer) {
                    final List<Record> answerList = result.getAnswerRecords();
                    int matchCnt = 0;
                    int ttl = 0;
                    final RRClass queryRrClass = rrClass;
                    final RRType queryRrType = rrType;
                    for (Record record : answerList) {
                        if (record.getRrClass() == queryRrClass && record.getRrType() == queryRrType) {
                            matchCnt++;
                        }
                        
                    }
                    Record[] records = answerList.toArray(new Record[answerList.size()]);
                    newResult.setResult(new CacheValue(records));
                    futureAnswer.setResult(result);
                }

                public void handleFailed(final IOException exception, final FutureAnswer futureAnswer) {
                    futureAnswer.setException(exception);
                }
            }, ourFutureAnswer);
            return newResult;
        } else {
            return oldResult;
        }
    }

    public IoFuture<Answer> resolve(final Domain name, final RRClass rrClass, final RRType rrType, final Set<ResolverFlag> flags) {
        if (flags.contains(ResolverFlag.BYPASS_CACHE)) {
            return realResolver.resolve(name, rrClass, rrType, flags);
        } else {
            final CacheKey key = new CacheKey(name, rrClass, rrType);
            final FutureAnswer futureAnswer = new FutureAnswer();
            final FutureCacheValue futureValue;

            futureValue = cache.get(key);
            if (futureValue == null) {

            } else {
                futureValue.addNotifier(new IoFuture.HandlingNotifier<CacheValue, FutureAnswer>() {
                    public void handleFailed(final IOException exception, final FutureAnswer attachment) {
                        attachment.setException(exception);
                    }

                    public void handleDone(final CacheValue result, final FutureAnswer attachment) {

                    }
                }, futureAnswer);
            }
        }
        return null;
    }

    private static final class CacheKey {
        private final RRClass rrClass;
        private final RRType rrType;
        private final Domain domain;
        private final int hashCode;

        private CacheKey(final Domain domain, final RRClass rrClass, final RRType rrType) {
            this.rrClass = rrClass;
            this.rrType = rrType;
            this.domain = domain;
            int result = rrClass.hashCode();
            result = 31 * result + rrType.hashCode();
            result = 31 * result + domain.hashCode();
            hashCode = result;
        }

        public boolean equals(final Object o) {
            if (this == o) return true;
            if (! (o instanceof CacheKey)) return false;
            final CacheKey cacheKey = (CacheKey) o;
            if (!domain.equals(cacheKey.domain)) return false;
            if (rrClass != cacheKey.rrClass) return false;
            if (rrType != cacheKey.rrType) return false;
            return true;
        }

        public int hashCode() {
            return hashCode;
        }
    }

    private static final class CacheValue {
        private final Record[] records;
        private final TTLSpec ttl;

        private CacheValue(final Record[] records) {
            this.records = records;
            int ttl = Integer.MAX_VALUE;
            for (Record record : records) {
                ttl = max(0, min(ttl, record.getTtlSpec().getTtl()));
            }
            this.ttl = TTLSpec.createVariable(((long)ttl * 1000) + System.currentTimeMillis());
        }
    }

    private static final class FutureCacheValue extends AbstractIoFuture<CacheValue> {

        protected boolean setException(final IOException exception) {
            return super.setException(exception);
        }

        protected boolean setResult(final CacheValue result) {
            return super.setResult(result);
        }
    }
}
