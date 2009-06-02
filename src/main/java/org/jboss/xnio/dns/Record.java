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

package org.jboss.xnio.dns;

import static java.lang.Math.max;
import static java.lang.Math.min;

public abstract class Record {

    private final Domain name;
    private final RRClass rrClass;
    private final RRType rrType;
    private final long eol;

    protected Record(final Domain name, final RRClass rrClass, final RRType rrType, final long eol) {
        this.name = name;
        this.rrType = rrType;
        this.rrClass = rrClass;
        this.eol = eol;
    }

    public Domain getName() {
        return name;
    }

    public RRClass getRrClass() {
        return rrClass;
    }

    public RRType getRrType() {
        return rrType;
    }

    /**
     * Get the EOL as a timestamp with millisecond resolution.  This is equal to the TTL plus the time at which the
     * request was received.
     *
     * @return the EOL
     */
    public long getEol() {
        return eol;
    }

    /**
     * Get the remaining TTL of this record in seconds.  The result will be between zero and {@code Integer.MAX_VALUE},
     * inclusive.
     *
     * @return the TTL
     */
    public int getTtl() {
        return (int) min((long) Integer.MAX_VALUE, max(0L, (eol - System.currentTimeMillis()) / 1000L));
    }

    protected abstract void appendRData(StringBuilder builder);

    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(name).append(' ').append(getTtl()).append(' ').append(rrClass).append(' ').append(rrType);
        appendRData(builder);
        return builder.toString();
    }
}
