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

/**
 * A record of type {@link RRType#MX}.
 */
public class MxRecord extends Record {

    private static final long serialVersionUID = -8404288502462059611L;

    private final int preference;
    private final Domain exchanger;

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param rrclass the record class
     * @param ttlSpec the TTL spec
     * @param preference the exchanger preference value
     * @param exchanger the exchanger domain
     */
    public MxRecord(final Domain name, final RRClass rrclass, final TTLSpec ttlSpec, final int preference, final Domain exchanger) {
        super(name, rrclass, RRType.MX, ttlSpec);
        this.preference = preference;
        this.exchanger = exchanger;
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param ttlSpec the TTL spec
     * @param preference the exchanger preference value
     * @param exchanger the exchanger domain
     */
    public MxRecord(final Domain name, final TTLSpec ttlSpec, final int preference, final Domain exchanger) {
        this(name, RRClass.IN, ttlSpec, preference, exchanger);
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param preference the exchanger preference value
     * @param exchanger the exchanger domain
     */
    public MxRecord(final Domain name, final int preference, final Domain exchanger) {
        this(name, TTLSpec.ZERO, preference, exchanger);
    }

    /** {@inheritDoc} */
    protected void appendRData(final StringBuilder builder) {
        builder.append(' ').append(preference).append(' ').append(exchanger);
    }

    /**
     * Get the preference value.
     *
     * @return the preference value
     */
    public int getPreference() {
        return preference;
    }

    /**
     * Get the exchanger name.
     *
     * @return the exchanger name
     */
    public Domain getExchanger() {
        return exchanger;
    }
}
