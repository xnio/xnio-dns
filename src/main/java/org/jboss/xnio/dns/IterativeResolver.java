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
import org.jboss.xnio.FailedIoFuture;
import org.jboss.xnio.FinishedIoFuture;
import org.jboss.xnio.FutureResult;
import org.jboss.xnio.dns.record.NsRecord;
import org.jboss.xnio.dns.record.ARecord;
import org.jboss.xnio.dns.record.AaaaRecord;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.IOException;
import java.net.InetAddress;

/**
 * A resolver which queries servers iteratively until the complete answer is acquired.
 */
public final class IterativeResolver extends AbstractResolver {

    private final NetworkResolver networkResolver;
    private final Resolver localResolver;
    private final Executor executor;

    public IterativeResolver(final NetworkResolver networkResolver, final Resolver localResolver, final Executor executor) {
        this.networkResolver = networkResolver;
        this.localResolver = localResolver;
        this.executor = executor;
    }

    public IoFuture<Answer> resolve(final Domain name, final RRClass rrClass, final RRType rrType, final Set<ResolverFlag> flags) {
        if (name.equals(Domain.ROOT)) {
            // no root hints loaded...
            return new FailedIoFuture<Answer>(new DNSException(ResultCode.SERVER_FAILURE, "No root hints loaded"));
        }
        if (flags.contains(ResolverFlag.NO_RECURSION)) {
            return new FinishedIoFuture<Answer>(
                    Answer.builder()
                            .setQueryDomain(name)
                            .setQueryRRClass(rrClass)
                            .setQueryRRType(rrType)
                            .setResultCode(ResultCode.NOERROR)
                            .create()
            );
        }
        final FutureResult<Answer> futureResult = new FutureResult<Answer>(executor);
        final IoFuture<Answer> futureParentNs = localResolver.resolve(name.getParent(), RRClass.IN, RRType.NS);
        futureParentNs.addNotifier(new IoFuture.HandlingNotifier<Answer, FutureResult<Answer>>() {
            // todo configurable?
            private final AtomicInteger ttl = new AtomicInteger(16);

            public void handleCancelled(final FutureResult<Answer> result) {
                result.setCancelled();
            }

            public void handleFailed(final IOException exception, final FutureResult<Answer> result) {
                result.setException(exception);
            }

            public void handleDone(final Answer answer, final FutureResult<Answer> result) {
                if (answer.getResultCode() != ResultCode.NOERROR) {
                    // pass on the love
                    result.setResult(answer);
                    return;
                }
                if (ttl.decrementAndGet() == 0) {
                    result.setResult(Answer.builder().setHeaderInfo(name, rrClass, rrType, ResultCode.SERVER_FAILURE).create());
                }
                final List<Record> answerRecords = answer.getAnswerRecords();
                if (answerRecords.isEmpty()) {
                    // iteration needed...
                    final List<Record> additionalRecords = answer.getAdditionalRecords();
                    final Map<Domain, InetAddress> possibleServers = new HashMap<Domain, InetAddress>();
                    for (Record record : additionalRecords) {
                        if (record instanceof ARecord) {
                            possibleServers.put(record.getName(), ((ARecord)record).getAddress());
                        } else if (record instanceof AaaaRecord) {
                            possibleServers.put(record.getName(), ((AaaaRecord)record).getAddress());
                        }
                    }
                    final List<InetAddress> serversToTry = new ArrayList<InetAddress>();
                    for (Record record : answer.getAuthorityRecords()) {
                        if (record instanceof NsRecord) {
                            final NsRecord nsRecord = (NsRecord) record;
                            if (name.isSubdomainOf(record.getName())) {
                                // try that server next!
                                final Domain server = nsRecord.getServer();
                                // but first, get the IP...
                                final InetAddress address = possibleServers.get(server);
                                if (address != null) {
                                    serversToTry.add(address);
                                }
                            }
                        }
                    }
                    if (serversToTry.isEmpty()) {
                        result.setResult(Answer.builder().setHeaderInfo(name, rrClass, rrType, ResultCode.SERVER_FAILURE).create());
                    }
                    // todo - use first server, but we should have a better algo
                    final Resolver resolver = networkResolver.resolverFor(serversToTry.get(0));
                    final IoFuture<Answer> recursion = resolver.resolve(name, rrClass, rrType);
                    recursion.addNotifier(this, result);
                    result.addCancelHandler(recursion);
                } else {
                    // got an answer!
                    result.setResult(answer);
                }
            }
        }, futureResult);
        return futureResult.getIoFuture();
    }
}
