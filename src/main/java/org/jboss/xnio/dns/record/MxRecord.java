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

public class MxRecord extends Record {

    private final int preference;
    private final Domain exchange;

    public MxRecord(final Domain name, final RRClass rrclass, final long eol, final int preference, final Domain exchange) {
        super(name, rrclass, RRType.MX, eol);
        this.preference = preference;
        this.exchange = exchange;
    }

    public MxRecord(final Domain name, final long eol, final int preference, final Domain exchange) {
        this(name, RRClass.IN, eol, preference, exchange);
    }

    public MxRecord(final Domain name, final int preference, final Domain exchange) {
        this(name, 0L, preference, exchange);
    }

    protected void appendRData(final StringBuilder builder) {
        builder.append(' ').append(preference).append(' ').append(exchange);
    }

    public int getPreference() {
        return preference;
    }

    public Domain getExchange() {
        return exchange;
    }
}
