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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jboss.xnio.FinishedIoFuture;
import org.jboss.xnio.IoFuture;
import org.jboss.xnio.dns.record.AaaaRecord;
import org.jboss.xnio.dns.record.ARecord;

/**
 * A HOSTS file resolver.
 */
public final class HostsResolver extends AbstractResolver {
    private volatile Map<Domain, InetAddress> hostsMap = Collections.emptyMap();
    private final Resolver next;

    public HostsResolver(final Resolver next) {
        this.next = next;
    }

    private void doInitialize(BufferedReader source) throws IOException {
        Map<Domain, InetAddress> hostsMap = new HashMap<Domain, InetAddress>();
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
            final String[] parts = line.split("\\s++");
            final int len = parts.length;
            if (len >= 1) {
                
            }
        }
        this.hostsMap = hostsMap;
    }

    /**
     * Replace the current mapping with the contents of a new HOSTS file.
     *
     * @param source the hosts file source
     * @throws IOException if an I/O error occurs
     */
    public void initialize(Reader source) throws IOException {
        if (source instanceof BufferedReader) {
            doInitialize((BufferedReader) source);
        } else {
            doInitialize(new BufferedReader(source));
        }
    }

    /**
     * Replace the current mapping with the contents of a new HOSTS file.
     *
     * @param file the file
     * @param encoding the file encoding, or {@code null} to use the platform encoding
     * @throws IOException if an I/O error occurs
     */
    public void initialize(File file, String encoding) throws IOException {
        final FileInputStream is = new FileInputStream(file);
        final InputStreamReader reader = encoding == null ? new InputStreamReader(is) : new InputStreamReader(is, encoding);
        initialize(reader);
    }

    /**
     * Replace the current mapping with the contents of a new HOSTS file.
     *
     * @param fileName the file name
     * @param encoding the file encoding, or {@code null} to use the platform encoding
     * @throws IOException if an I/O error occurs
     */
    public void initialize(String fileName, String encoding) throws IOException {
        initialize(new File(fileName), encoding);
    }

    /**
     * {@inheritDoc}  This instance queries the HOSTS cache, and if no records are found, the request is forwarded to
     * the next resolver in the chain.
     */
    public IoFuture<Answer> resolve(final Domain name, final RRClass rrClass, final RRType rrType, final Set<ResolverFlag> flags) {
        if (rrClass == RRClass.IN) {
            if (rrType == RRType.A) {
                final InetAddress address = hostsMap.get(name);
                if (address instanceof Inet4Address) {
                    final Answer answer = new Answer(name, rrClass, rrType, ResultCode.NOERROR);
                    final List<Record> records = answer.getAnswerRecords();
                    records.add(new ARecord(name, TTLSpec.ZERO, (Inet4Address) address));
                    return new FinishedIoFuture<Answer>(answer);
                }
            } else if (rrType == RRType.AAAA) {
                final InetAddress address = hostsMap.get(name);
                if (address instanceof Inet6Address) {
                    final Answer answer = new Answer(name, rrClass, rrType, ResultCode.NOERROR);
                    final List<Record> records = answer.getAnswerRecords();
                    records.add(new AaaaRecord(name, TTLSpec.ZERO, (Inet6Address) address));
                    return new FinishedIoFuture<Answer>(answer);
                }
            }
        }
        return next.resolve(name, rrClass, rrType, flags);
    }
}
