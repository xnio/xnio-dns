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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jboss.xnio.FinishedIoFuture;
import org.jboss.xnio.IoFuture;
import org.jboss.xnio.dns.Answer;
import org.jboss.xnio.dns.Domain;
import org.jboss.xnio.dns.RRClass;
import org.jboss.xnio.dns.RRType;
import org.jboss.xnio.dns.Record;
import org.jboss.xnio.dns.AbstractNetworkResolver;
import org.jboss.xnio.dns.record.InetAAAARecord;
import org.jboss.xnio.dns.record.InetARecord;

public final class HostsResolver extends AbstractNetworkResolver {
    private volatile Map<String, InetAddress> hostsMap = Collections.emptyMap();

    private void initialize(BufferedReader source) throws IOException {
        Map<String, InetAddress> hostsMap = new HashMap<String, InetAddress>();
        String line;
        while ((line = source.readLine()) != null) {
            int hi = line.indexOf('#');
            if (hi != -1) {
                line = line.substring(0, hi);
            }
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            final String[] parts = line.split("\\s+");
        }
    }

    public void initialize(Reader source) throws IOException {
        if (source instanceof BufferedReader) {
            initialize((BufferedReader) source);
        } else {
            initialize(new BufferedReader(source));
        }
    }

    protected SocketAddress getDefaultServerAddress() {
        return null;
    }

    public IoFuture<Answer> resolve(final SocketAddress addr, final Domain name, final RRClass rrClass, final RRType rrType, final Set<Flag> flags) {
        if (rrClass == RRClass.IN) {
            if (rrType == RRType.A) {
                final InetAddress address = hostsMap.get(name);
                if (address instanceof Inet4Address) {
                    final Answer answer = new Answer(name, rrClass, rrType);
                    final List<Record> records = answer.getAnswerRecords();
                    records.add(new InetARecord(name, 0L, (Inet4Address) address));
                    return new FinishedIoFuture<Answer>(answer);
                }
            } else if (rrType == RRType.AAAA) {
                final InetAddress address = hostsMap.get(name);
                if (address instanceof Inet6Address) {
                    final Answer answer = new Answer(name, rrClass, rrType);
                    final List<Record> records = answer.getAnswerRecords();
                    records.add(new InetAAAARecord(name, 0L, (Inet6Address) address));
                    return new FinishedIoFuture<Answer>(answer);
                }
            }
        }
        return new FinishedIoFuture<Answer>(new Answer(name, rrClass, rrType));
    }
}
