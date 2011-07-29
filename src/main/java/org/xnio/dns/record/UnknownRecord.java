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

import org.xnio.dns.Record;
import org.xnio.dns.Domain;
import org.xnio.dns.RRClass;
import org.xnio.dns.RRType;
import org.xnio.dns.TTLSpec;
import org.xnio.Buffers;
import java.nio.ByteBuffer;

/**
 * A record of unknown type.
 */
public class UnknownRecord extends Record {

    private static final long serialVersionUID = -1357005801530421627L;

    private final byte[] data;

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param rrClass the record class
     * @param rrType the record type
     * @param ttlSpec the TTL spec
     * @param data the raw data
     */
    public UnknownRecord(final Domain name, final RRClass rrClass, final RRType rrType, final TTLSpec ttlSpec, final byte[] data) {
        super(name, rrClass, rrType, ttlSpec);
        this.data = data;
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param rrType the record type
     * @param ttlSpec the TTL spec
     * @param data the raw data
     */
    public UnknownRecord(final Domain name, final RRType rrType, final TTLSpec ttlSpec, final byte[] data) {
        this(name, RRClass.IN, rrType, ttlSpec, data);
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param rrType the record type
     * @param data the raw data
     */
    public UnknownRecord(final Domain name, final RRType rrType, final byte[] data) {
        this(name, rrType, TTLSpec.ZERO, data);
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param rrClass the record class
     * @param rrType the record type
     * @param ttlSpec the TTL spec
     * @param recordBuffer the buffer from which this record's RDATA should be built
     */
    public UnknownRecord(final Domain name, final RRClass rrClass, final RRType rrType, final TTLSpec ttlSpec, final ByteBuffer recordBuffer) {
        super(name, rrClass, rrType, ttlSpec);
        data = Buffers.take(recordBuffer, recordBuffer.remaining());
    }

    /** {@inheritDoc} */
    protected void appendRData(final StringBuilder builder) {
        for (byte b : data) {
            builder.append(' ').append(Integer.toHexString(b & 0xff));
        }
    }
}