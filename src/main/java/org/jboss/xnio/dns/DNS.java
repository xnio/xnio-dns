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

import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.io.IOException;
import org.jboss.xnio.IoFuture;
import org.jboss.xnio.AbstractIoFuture;

public final class DNS {
    private DNS() {}

    public static IoFuture<InetAddress[]> resolveInet(String name) {
        return null;
    }

    public static IoFuture<Inet4Address[]> resolveInet4(String name) {
        return null;
    }

    public static IoFuture<Inet6Address[]> resolveInet6(String name) {
        return null;
    }

    public static IoFuture<String> resolveReverse(InetAddress address) {
        return null;
    }

    public static IoFuture<String> resolveText(String name) {
        return null;
    }

    public static IoFuture<Record> getAnswer(IoFuture<Answer> message, final String name, final RRClass rrClass, final RRType rrType) {
        final FutureRecord futureRecord = new FutureRecord();
        message.addNotifier(new IoFuture.HandlingNotifier<Answer, Object>() {
            public void handleCancelled(final Object attachment) {
                futureRecord.finishCancel();
            }

            public void handleFailed(final IOException exception, final Object attachment) {
                futureRecord.setException(exception);
            }

            public void handleDone(final Answer result, final Object attachment) {
                for (Record record : result.getAnswerRecords()) {
                    if (record.getRrClass().equals(rrClass) && record.getRrType().equals(rrType) && record.getName().equals(name)) {
                        futureRecord.setResult(record);
                        return;
                    }
                }
                // not found
                futureRecord.setResult(null);
            }
        }, null);
        return futureRecord;
    }

    private static final class FutureRecord extends AbstractIoFuture<Record> {
        protected boolean finishCancel() {
            return super.finishCancel();
        }

        protected boolean setResult(final Record result) {
            return super.setResult(result);
        }

        protected boolean setException(final IOException exception) {
            return super.setException(exception);
        }
    }
}
