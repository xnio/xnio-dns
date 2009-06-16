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

package org.jboss.xnio.dns.record;

import java.net.Inet4Address;
import org.jboss.xnio.dns.Record;
import org.jboss.xnio.dns.RRClass;
import org.jboss.xnio.dns.RRType;
import org.jboss.xnio.dns.Domain;
import org.jboss.xnio.dns.TTLSpec;

/**
 * A record of type {@link RRType#A}.
 */
public class ARecord extends Record {

    private static final long serialVersionUID = -2685055677791879066L;

    private final Inet4Address address;

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param rrClass the resource record class
     * @param ttlSpec the TTL spec
     * @param address the IP address
     */
    public ARecord(final Domain name, final RRClass rrClass, final TTLSpec ttlSpec, final Inet4Address address) {
        super(name, rrClass, RRType.A, ttlSpec);
        this.address = address;
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param ttlSpec the TTL spec
     * @param address the IP address
     */
    public ARecord(final Domain name, final TTLSpec ttlSpec, final Inet4Address address) {
        this(name, RRClass.IN, ttlSpec, address);
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param address the IP address
     */
    public ARecord(final Domain name, final Inet4Address address) {
        this(name, TTLSpec.ZERO, address);
    }

    /**
     * Get the IP address.
     *
     * @return the IP address
     */
    public Inet4Address getAddress() {
        return address;
    }

    /** {@inheritDoc} */
    protected void appendRData(final StringBuilder builder) {
        builder.append(' ').append(address.getHostAddress());
    }
}
