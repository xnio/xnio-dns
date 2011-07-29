/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, JBoss Inc., and individual contributors as indicated
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

package org.xnio.dns.record;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import org.xnio.dns.Record;
import org.xnio.dns.RRClass;
import org.xnio.dns.RRType;
import org.xnio.dns.Domain;
import org.xnio.dns.TTLSpec;
import org.xnio.dns.DNS;
import org.xnio.Buffers;

/**
 * A record of type {@link RRType#AAAA}.
 */
public class AaaaRecord extends Record {
    private static final long serialVersionUID = -8702941004736168982L;

    private final Inet6Address address;

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param rrClass the resource record class
     * @param ttlSpec the TTL spec
     * @param recordBuffer the buffer from which the record data should be built
     */
    public AaaaRecord(final Domain name, final RRClass rrClass, final TTLSpec ttlSpec, final ByteBuffer recordBuffer) {
        super(name, rrClass, RRType.AAAA, ttlSpec);
        byte[] bytes = Buffers.take(recordBuffer, 16);
        try {
            address = (Inet6Address) InetAddress.getByAddress(name.toString(), bytes);
        } catch (UnknownHostException e) {
            // not possible
            throw new IllegalStateException(e);
        }
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param rrClass the resource record class
     * @param ttlSpec the TTL spec
     * @param recordString the string from which the record data should be built
     */
    public AaaaRecord(final Domain name, final RRClass rrClass, final TTLSpec ttlSpec, final String recordString) {
        super(name, rrClass, RRType.AAAA, ttlSpec);
        address = DNS.parseInet6Address(name.getHostName(), recordString);
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param rrClass the resource record class
     * @param ttlSpec the TTL spec
     * @param address the IPv6 address
     */
    public AaaaRecord(final Domain name, final RRClass rrClass, final TTLSpec ttlSpec, final Inet6Address address) {
        super(name, rrClass, RRType.AAAA, ttlSpec);
        this.address = address;
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param ttlSpec the TTL spec
     * @param address the IPv6 address
     */
    public AaaaRecord(final Domain name, final TTLSpec ttlSpec, final Inet6Address address) {
        this(name, RRClass.IN, ttlSpec, address);
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param address the IPv6 address
     */
    public AaaaRecord(final Domain name, final Inet6Address address) {
        this(name, TTLSpec.ZERO, address);
    }

    /**
     * Get the IP address.
     *
     * @return the IP address
     */
    public Inet6Address getAddress() {
        return address;
    }

    /** {@inheritDoc} */
    protected void appendRData(final StringBuilder builder) {
        builder.append(' ').append(address.getHostAddress());
    }
}