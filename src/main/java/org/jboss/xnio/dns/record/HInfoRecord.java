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
import org.jboss.xnio.Buffers;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * A record of type {@link RRType#HINFO}.
 */
public class HInfoRecord extends Record {

    private static final long serialVersionUID = 8123650143005076515L;

    private final String cpu;
    private final String os;
    private static final Charset LATIN_1 = Charset.forName("ISO-8859-1");

    private static String readCharString(ByteBuffer buffer) {
        return new String(Buffers.take(buffer, buffer.get() & 0xff), LATIN_1);
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param rrClass the resource record class
     * @param ttlSpec the TTL spec
     * @param recordBuffer the buffer from which the record data should be built
     */
    public HInfoRecord(final Domain name, final RRClass rrClass, final TTLSpec ttlSpec, final ByteBuffer recordBuffer) {
        this(name, rrClass, ttlSpec, readCharString(recordBuffer), readCharString(recordBuffer));
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param rrClass the record class
     * @param ttlSpec the TTL spec
     * @param cpu the CPU type
     * @param os the OS type
     */
    public HInfoRecord(final Domain name, final RRClass rrClass, final TTLSpec ttlSpec, final String cpu, final String os) {
        super(name, rrClass, RRType.HINFO, ttlSpec);
        this.cpu = cpu;
        this.os = os;
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param ttlSpec the TTL spec
     * @param cpu the CPU type
     * @param os the OS type
     */
    public HInfoRecord(final Domain name, final TTLSpec ttlSpec, final String cpu, final String os) {
        this(name, RRClass.IN, ttlSpec, cpu, os);
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param cpu the CPU type
     * @param os the OS type
     */
    public HInfoRecord(final Domain name, final String cpu, final String os) {
        this(name, TTLSpec.ZERO, cpu, os);
    }

    /**
     * Get the CPU type.
     *
     * @return the CPU type
     */
    public String getCpu() {
        return cpu;
    }

    /**
     * Get the OS type.
     *
     * @return the OS type
     */
    public String getOs() {
        return os;
    }

    /** {@inheritDoc} */
    protected void appendRData(final StringBuilder builder) {
        builder.append(" \"").append(cpu).append("\" \"").append(os).append('"');
    }
}
