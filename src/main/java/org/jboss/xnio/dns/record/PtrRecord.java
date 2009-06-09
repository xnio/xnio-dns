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
 * A record of type {@link RRType#PTR}.
 */
public class PtrRecord extends Record {

    private static final long serialVersionUID = 2883759267175514233L;

    private final Domain target;

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param rrclass the record class
     * @param ttlSpec the TTL spec
     * @param target the target domain name
     */
    public PtrRecord(final Domain name, final RRClass rrclass, final TTLSpec ttlSpec, final Domain target) {
        super(name, rrclass, RRType.PTR, ttlSpec);
        this.target = target;
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param ttlSpec the TTL spec
     * @param target the target domain name
     */
    public PtrRecord(final Domain name, final TTLSpec ttlSpec, final Domain target) {
        this(name, RRClass.IN, ttlSpec, target);
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param target the target domain name
     */
    public PtrRecord(final Domain name, final Domain target) {
        this(name, TTLSpec.ZERO, target);
    }

    /** {@inheritDoc} */
    protected void appendRData(final StringBuilder builder) {
        builder.append(' ').append(target);
    }

    /**
     * Get the target domain name.
     *
     * @return the target
     */
    public Domain getTarget() {
        return target;
    }
}