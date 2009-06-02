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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.jboss.xnio.IoFuture;

public abstract class AbstractNetworkResolver extends AbstractResolver implements NetworkResolver {

    public IoFuture<Answer> resolve(final InetAddress server, final Domain name, final RRClass rrClass, final RRType rrType, final Set<Flag> flags) {
        return resolve(new InetSocketAddress(server, 53), name, rrClass, rrType, flags);
    }

    protected abstract SocketAddress getDefaultServerAddress();

    public IoFuture<Answer> resolve(final Domain server, final Domain name, final RRClass rrClass, final RRType rrType, final Set<Flag> flags) {
        final FutureAnswer futureAnswer = new FutureAnswer();
        final IoFuture<List<InetAddress>> futureServerList = resolveAllInet(server);
        futureAnswer.addCancelHandler(futureServerList);
        futureServerList.addNotifier(new IoFuture.HandlingNotifier<List<InetAddress>, FutureAnswer>() {
            public void handleCancelled(final FutureAnswer attachment) {
                attachment.finishCancel();
            }

            public void handleFailed(final IOException exception, final FutureAnswer attachment) {
                attachment.setException(exception);
            }

            public void handleDone(final List<InetAddress> result, final FutureAnswer attachment) {
                final Iterator<InetAddress> itr = result.iterator();
                if (! itr.hasNext()) {
                    attachment.setException(new DNSException(DNSException.Code.SERVER_FAILURE, "No servers could be found"));
                    return;
                }
                final IoFuture<Answer> futureRealAnswer = resolve(itr.next(), name, rrClass, rrType, flags);
                futureAnswer.addCancelHandler(futureRealAnswer);
                futureRealAnswer.addNotifier(new IoFuture.HandlingNotifier<Answer, FutureAnswer>() {
                    public void handleCancelled(final FutureAnswer attachment) {
                        attachment.finishCancel();
                    }

                    public void handleFailed(final IOException exception, final FutureAnswer attachment) {
                        attachment.setException(exception);
                    }

                    public void handleDone(final Answer result, final FutureAnswer attachment) {
                        attachment.setResult(result);
                    }
                }, attachment);
            }
        }, futureAnswer);
        return futureAnswer;
    }

    public IoFuture<Answer> resolve(final Domain name, final RRClass rrClass, final RRType rrType, final Set<Flag> flags) {
        return resolve(getDefaultServerAddress(), name, rrClass, rrType, flags);
    }
}
