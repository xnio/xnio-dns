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

package org.xnio.dns;

import java.io.Serializable;
import java.nio.ByteBuffer;
import org.xnio.dns.record.AaaaRecord;
import org.xnio.dns.record.ARecord;
import org.xnio.dns.record.CNameRecord;
import org.xnio.dns.record.HInfoRecord;
import org.xnio.dns.record.MxRecord;
import org.xnio.dns.record.NsRecord;
import org.xnio.dns.record.PtrRecord;
import org.xnio.dns.record.SoaRecord;
import org.xnio.dns.record.TxtRecord;
import org.xnio.dns.record.UnknownRecord;
import org.xnio.dns.record.WksRecord;
import org.xnio.Buffers;

/**
 * A resource record.
 */
public abstract class Record implements Serializable {

    private static final long serialVersionUID = 132819048908309214L;

    private final Domain name;
    private final RRClass rrClass;
    private final RRType rrType;
    private final TTLSpec ttlSpec;

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param rrClass the record class
     * @param rrType the record type
     * @param ttlSpec the TTL of this record
     */
    protected Record(final Domain name, final RRClass rrClass, final RRType rrType, final TTLSpec ttlSpec) {
        this.name = name;
        this.rrType = rrType;
        this.rrClass = rrClass;
        this.ttlSpec = ttlSpec;
    }

    /**
     * Get the domain name for this record.
     *
     * @return the domain name
     */
    public Domain getName() {
        return name;
    }

    /**
     * Get the class of this record.
     *
     * @return the resource record class
     */
    public RRClass getRrClass() {
        return rrClass;
    }

    /**
     * Get the type of this record.
     *
     * @return the resource record type
     */
    public RRType getRrType() {
        return rrType;
    }

    /**
     * Get the TTL of this record.
     *
     * @return the TTL
     */
    public TTLSpec getTtlSpec() {
        return ttlSpec;
    }

    /**
     * Append any record-specific RR data to the string builder.
     *
     * @param builder the builder
     */
    protected void appendRData(StringBuilder builder) {}

    /**
     * Get the string representation of this record.
     *
     * @return the string representation
     */
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(name).append(' ').append(getTtlSpec().getTtl()).append(' ').append(rrClass).append(' ').append(rrType);
        appendRData(builder);
        return builder.toString();
    }

    /**
     * Construct an instance from bytes in a byte buffer.
     *
     * @param buffer the source buffer
     * @return the resource record
     */
    public static Record fromBytes(final ByteBuffer buffer) {
        final Domain name = Domain.fromBytes(buffer);
        final RRType rrType = RRType.fromInt(buffer.getShort() & 0xffff);
        final RRClass rrClass = RRClass.fromInt(buffer.getShort() & 0xffff);
        final TTLSpec ttlSpec = TTLSpec.createFixed(buffer.getInt());
        final ByteBuffer recordBuffer = Buffers.slice(buffer, buffer.getShort() & 0xffff);
        switch (rrType) {
            case AAAA:  return new AaaaRecord (name, rrClass, ttlSpec, recordBuffer);
            case A:     return new ARecord    (name, rrClass, ttlSpec, recordBuffer);
            case CNAME: return new CNameRecord(name, rrClass, ttlSpec, recordBuffer);
            case HINFO: return new HInfoRecord(name, rrClass, ttlSpec, recordBuffer);
            case MX:    return new MxRecord   (name, rrClass, ttlSpec, recordBuffer);
            case NS:    return new NsRecord   (name, rrClass, ttlSpec, recordBuffer);
            case PTR:   return new PtrRecord  (name, rrClass, ttlSpec, recordBuffer);
            case SOA:   return new SoaRecord  (name, rrClass, ttlSpec, recordBuffer);
            case TXT:   return new TxtRecord  (name, rrClass, ttlSpec, recordBuffer);
            case WKS:   return new WksRecord  (name, rrClass, ttlSpec, recordBuffer);

            default:    return new UnknownRecord(name, rrClass, rrType, ttlSpec, recordBuffer);
        }
    }
}
