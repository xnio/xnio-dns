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

/**
 * A record of type {@link RRType#ANY}.
 */
public class AnyRecord extends Record {

    private static final long serialVersionUID = -1357005801530421627L;

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param rrClass the record class
     * @param ttlSpec the TTL spec
     */
    public AnyRecord(final Domain name, final RRClass rrClass, final TTLSpec ttlSpec) {
        super(name, rrClass, RRType.ANY, ttlSpec);
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param ttlSpec the TTL spec
     */
    public AnyRecord(final Domain name, final TTLSpec ttlSpec) {
        this(name, RRClass.IN, ttlSpec);
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     */
    public AnyRecord(final Domain name) {
        this(name, TTLSpec.ZERO);
    }
}
