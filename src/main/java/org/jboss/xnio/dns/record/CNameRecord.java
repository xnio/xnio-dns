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

public class CNameRecord extends Record {

    private final Domain cname;

    public CNameRecord(final Domain name, final RRClass rrclass, final long eol, final Domain cname) {
        super(name, rrclass, RRType.CNAME, eol);
        this.cname = cname;
    }

    public CNameRecord(final Domain name, final long eol, final Domain cname) {
        this(name, RRClass.IN, eol, cname);
    }

    public CNameRecord(final Domain name, final Domain cname) {
        this(name, 0L, cname);
    }

    public Domain getCname() {
        return cname;
    }

    protected void appendRData(final StringBuilder builder) {
        builder.append(' ').append(cname);
    }
}
