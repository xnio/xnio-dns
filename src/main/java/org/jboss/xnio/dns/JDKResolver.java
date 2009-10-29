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
import org.jboss.xnio.FinishedIoFuture;
import org.jboss.xnio.FutureResult;
import org.jboss.xnio.dns.record.PtrRecord;
import org.jboss.xnio.dns.record.ARecord;
import org.jboss.xnio.dns.record.AaaaRecord;
import java.util.Set;
import java.util.EnumSet;
import java.util.concurrent.Executor;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.Inet4Address;
import java.net.Inet6Address;

/**
 * A resolver which uses the JDK facility to answer queries.  The JDK facility can only do simple forward and reverse
 * host/IP address lookups.
 */
public final class JDKResolver extends AbstractResolver {

    private static final Set<RRClass> RRCLASSES = EnumSet.of(RRClass.ANY, RRClass.IN);
    private static final Set<RRType> RRTYPES = EnumSet.of(RRType.ANY, RRType.A, RRType.AAAA);

    private final Executor queryExecutor;

    /**
     * Construct a new instance.
     *
     * @param queryExecutor the executor to use to execute asynchronous queries
     */
    public JDKResolver(final Executor queryExecutor) {
        this.queryExecutor = queryExecutor;
    }

    /** {@inheritDoc} */
    public IoFuture<Answer> resolve(final Domain name, final RRClass rrClass, final RRType rrType, final Set<ResolverFlag> flags) {
        if (! RRCLASSES.contains(rrClass) || ! RRTYPES.contains(rrType)) {
            return new FinishedIoFuture<Answer>(Answer.builder().setHeaderInfo(name, rrClass, rrType, ResultCode.NOERROR).create());
        }
        final Answer.Builder builder = Answer.builder();
        builder.setHeaderInfo(name, rrClass, rrType, ResultCode.UNKNOWN);
        final FutureResult<Answer> answerManager = new FutureResult<Answer>();
        queryExecutor.execute(new QueryTask(answerManager, builder, name));
        return answerManager.getIoFuture();
    }

    private static class QueryTask implements Runnable {

        private final FutureResult<Answer> answerManager;
        private final Answer.Builder builder;
        private final Domain name;

        public QueryTask(final FutureResult<Answer> answerManager, final Answer.Builder builder, final Domain name) {
            this.answerManager = answerManager;
            this.builder = builder;
            this.name = name;
        }

        public void run() {
            if (name.isReverseArpa()) {
                try {
                    builder.addAnswerRecord(new PtrRecord(name, Domain.fromString(InetAddress.getByAddress(name.getReverseArpaBytes()).getHostAddress())));
                } catch (UnknownHostException e) {
                } catch (IllegalArgumentException e) {
                }
            } else {
                try {
                    for (InetAddress address : InetAddress.getAllByName(name.getHostName())) {
                        if (address instanceof Inet4Address) {
                            builder.addAnswerRecord(new ARecord(name, (Inet4Address) address));
                        } else if (address instanceof Inet6Address) {
                            builder.addAnswerRecord(new AaaaRecord(name, (Inet6Address) address));
                        }
                        // else ignore
                    }
                } catch (UnknownHostException e) {
                }
            }
            answerManager.setResult(builder.create());
        }
    }
}
