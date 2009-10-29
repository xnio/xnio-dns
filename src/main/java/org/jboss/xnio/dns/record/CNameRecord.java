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

import org.jboss.xnio.dns.Record;
import org.jboss.xnio.dns.RRClass;
import org.jboss.xnio.dns.RRType;
import org.jboss.xnio.dns.Domain;
import org.jboss.xnio.dns.TTLSpec;
import java.nio.ByteBuffer;

/**
 * A record of type {@link RRType#CNAME}.
 */
public class CNameRecord extends Record {

    private static final long serialVersionUID = 6806325778477267585L;

    private final Domain cname;

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param rrClass the resource record class
     * @param ttlSpec the TTL spec
     * @param recordBuffer the buffer from which the record data should be built
     */
    public CNameRecord(final Domain name, final RRClass rrClass, final TTLSpec ttlSpec, final ByteBuffer recordBuffer) {
        this(name, rrClass, ttlSpec, Domain.fromBytes(recordBuffer));
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param rrClass the resource record class
     * @param ttlSpec the TTL spec
     * @param recordString the string from which the record data should be built
     */
    public CNameRecord(final Domain name, final RRClass rrClass, final TTLSpec ttlSpec, final String recordString) {
        this(name, rrClass, ttlSpec, Domain.fromString(recordString));
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param rrclass the record class
     * @param ttlSpec the TTL spec
     * @param cname the destination domain name
     */
    public CNameRecord(final Domain name, final RRClass rrclass, final TTLSpec ttlSpec, final Domain cname) {
        super(name, rrclass, RRType.CNAME, ttlSpec);
        this.cname = cname;
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param ttlSpec the TTL spec
     * @param cname the destination domain name
     */
    public CNameRecord(final Domain name, final TTLSpec ttlSpec, final Domain cname) {
        this(name, RRClass.IN, ttlSpec, cname);
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param cname the destination domain name
     */
    public CNameRecord(final Domain name, final Domain cname) {
        this(name, TTLSpec.ZERO, cname);
    }

    /**
     * Get the target of this CNAME.
     *
     * @return the target domain
     */
    public Domain getCname() {
        return cname;
    }

    /** {@inheritDoc} */
    protected void appendRData(final StringBuilder builder) {
        builder.append(' ').append(cname);
    }
}
