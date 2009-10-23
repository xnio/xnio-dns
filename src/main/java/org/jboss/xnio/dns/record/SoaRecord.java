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
import org.jboss.xnio.dns.Domain;
import org.jboss.xnio.dns.RRClass;
import org.jboss.xnio.dns.RRType;
import org.jboss.xnio.dns.TTLSpec;
import java.nio.ByteBuffer;

/**
 * A record of type {@link RRType#SOA}.
 */
public class SoaRecord extends Record {

    private static final long serialVersionUID = 4582740248500266324L;

    private final Domain mName;
    private final Domain rName;
    private final int serial;
    private final int refresh;
    private final int retry;
    private final int expire;
    private final TTLSpec minimum;

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param rrClass the record class
     * @param ttlSpec the TTL of the record
     * @param mName the domain name of the name server that was the primary source of data for this domain
     * @param rName the mailbox of the person responsible for this domain
     * @param serial the serial number (always treat as unsigned)
     * @param refresh the zone refresh time
     * @param retry the zone refresh failure retry time
     * @param expire the zone expiration time
     * @param minimum the minimum TTL
     */
    public SoaRecord(final Domain name, final RRClass rrClass, final TTLSpec ttlSpec, final Domain mName, final Domain rName, final int serial, final int refresh, final int retry, final int expire, final TTLSpec minimum) {
        super(name, rrClass, RRType.SOA, ttlSpec);
        this.mName = mName;
        this.rName = rName;
        this.serial = serial;
        this.refresh = refresh;
        this.retry = retry;
        this.expire = expire;
        this.minimum = minimum;
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param ttlSpec the TTL of the record
     * @param mName the domain name of the name server that was the primary source of data for this domain
     * @param rName the mailbox of the person responsible for this domain
     * @param serial the serial number (always treat as unsigned)
     * @param refresh the zone refresh time
     * @param retry the zone refresh failure retry time
     * @param expire the zone expiration time
     * @param minimum the minimum TTL
     */
    public SoaRecord(final Domain name, final TTLSpec ttlSpec, final Domain mName, final Domain rName, final int serial, final int refresh, final int retry, final int expire, final TTLSpec minimum) {
        this(name, RRClass.IN, ttlSpec, mName, rName, serial, refresh, retry, expire, minimum);
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param mName the domain name of the name server that was the primary source of data for this domain
     * @param rName the mailbox of the person responsible for this domain
     * @param serial the serial number (always treat as unsigned)
     * @param refresh the zone refresh time
     * @param retry the zone refresh failure retry time
     * @param expire the zone expiration time
     * @param minimum the minimum TTL
     */
    public SoaRecord(final Domain name, final Domain mName, final Domain rName, final int serial, final int refresh, final int retry, final int expire, final TTLSpec minimum) {
        this(name, RRClass.IN, TTLSpec.ZERO, mName, rName, serial, refresh, retry, expire, minimum);
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param rrClass the resource record class
     * @param ttlSpec the TTL spec
     * @param recordBuffer the buffer from which the record data should be built
     */
    public SoaRecord(final Domain name, final RRClass rrClass, final TTLSpec ttlSpec, final ByteBuffer recordBuffer) {
        super(name, rrClass, RRType.SOA, ttlSpec);
        mName = Domain.fromBytes(recordBuffer);
        rName = Domain.fromBytes(recordBuffer);
        serial = recordBuffer.getInt();
        refresh = recordBuffer.getInt();
        retry = recordBuffer.getInt();
        expire = recordBuffer.getInt();
        minimum = TTLSpec.createFixed(recordBuffer.getInt());
    }

    /**
     * Get the MNAME, which is the domain name of the name server that was the primary source of data for this domain.
     *
     * @return the MNAME
     */
    public Domain getMName() {
        return mName;
    }

    /**
     * Get the RNAME, which is the mailbox of the person responsible for this domain (but in domain format).
     *
     * @return the RNAME
     */
    public Domain getRName() {
        return rName;
    }

    /**
     * Get the serial number of this domain.  This value is an unsigned 32-bit integer.
     *
     * @return the serial number
     */
    public long getSerial() {
        return serial & 0xffffffffL;
    }

    /**
     * Get the zone refresh time (in seconds).
     *
     * @return the zone refresh time
     */
    public int getRefresh() {
        return refresh;
    }

    /**
     * Get the zone refresh time failure retry time (in seconds).
     *
     * @return the zone refresh time
     */
    public int getRetry() {
        return retry;
    }

    /**
     * Get the zone expiration time (in seconds).
     *
     * @return the zone expiration time
     */
    public int getExpire() {
        return expire;
    }

    /**
     * Get the zone minimum TTL.
     *
     * @return the zone minimum TTL
     */
    public TTLSpec getMinimum() {
        return minimum;
    }

    /** {@inheritDoc} */
    protected void appendRData(final StringBuilder builder) {
        builder.append(' ').append(mName).append(' ').append(rName).append(" ( ").append(serial & 0xffffffffL);
        builder.append(' ').append(refresh).append(' ').append(retry).append(' ').append(expire).append(' ').append(minimum);
        builder.append(" )");
    }
}
