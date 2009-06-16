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
import java.util.Set;
import java.util.concurrent.Executor;
import java.io.IOException;

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
            return new FinishedIoFuture<Answer>(new Answer(name, rrClass, rrType, ResultCode.NOERROR));
        }
        final FutureAnswer futureAnswer = new FutureAnswer(executor);
        final IoFuture<Answer> futureParentNs = localResolver.resolve(name.getParent(), RRClass.IN, RRType.NS);
        futureParentNs.addNotifier(new IoFuture.HandlingNotifier<Answer, FutureAnswer>() {
            public void handleDone(final Answer result, final FutureAnswer futureAnswer) {
                
            }

            public void handleFailed(final IOException exception, final FutureAnswer futureAnswer) {
                futureAnswer.setException(exception);
            }
        }, futureAnswer);
        return futureAnswer;
    }
}
