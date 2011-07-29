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

import static java.lang.Math.min;
import static java.lang.Math.max;
import java.io.Serializable;

/**
 * A resource record time-to-live specification.  Such a specification may be relative or absolute.
 */
public abstract class TTLSpec implements Serializable {

    private static final long serialVersionUID = 9017672320381434104L;

    private TTLSpec() {}

    /**
     * Get the timestamp (in milliseconds, ala {@link System#currentTimeMillis()}) at which this record will be expired.
     *
     * @return the timestamp
     */
    public abstract long getEol();

    /**
     * Get the remaining time-to-live in seconds.  A fixed TTL spec will always return the same value, whereas a variable TTL
     * spec will count down towards an absolute time.
     *
     * @return the remaining time-to-live in seconds
     */
    public abstract int getTtl();

    /**
     * Determine whether this specification has a fixed or variable (decreasing) TTL.
     *
     * @return {@code true} if this instance has a fixed TTL, {@code false} if it has a variable TTL
     */
    public abstract boolean isFixed();

    /**
     * Determine whether this TTL specification is expired.  A fixed TTL specification never expires.
     *
     * @return {@code true} if this TTL is expired
     */
    public abstract boolean isExpired();

    /**
     * Create a variable (decreasing) TTL with the given EOL.
     *
     * @param eol the EOL timestamp
     * @return the specification
     */
    public static TTLSpec createVariable(long eol) {
        return new VariableTtlSpec(eol);
    }

    /**
     * Create a fixed TTL with the given lifetime.
     *
     * @param ttl the TTL value, in seconds
     * @return the specification
     */
    public static TTLSpec createFixed(int ttl) {
        return new FixedTtlSpec(ttl);
    }

    /**
     * A fixed TTL with a time of zero.
     */
    public static TTLSpec ZERO = createFixed(0);

    static final class FixedTtlSpec extends TTLSpec {

        private static final long serialVersionUID = -2365149842752581181L;
        private final int ttl;

        private FixedTtlSpec(final int ttl) {
            this.ttl = ttl;
        }

        public long getEol() {
            return System.currentTimeMillis() + ((long)ttl * 1000L);
        }

        public int getTtl() {
            return ttl;
        }

        public boolean isFixed() {
            return true;
        }

        public boolean isExpired() {
            return false;
        }
    }

    static final class VariableTtlSpec extends TTLSpec {

        private static final long serialVersionUID = -3843994141080180888L;
        private final long eol;

        private VariableTtlSpec(final long eol) {
            this.eol = eol;
        }

        public long getEol() {
            return eol;
        }

        public int getTtl() {
            return (int) min((long) Integer.MAX_VALUE, max(0L, (eol - System.currentTimeMillis()) / 1000L));
        }

        public boolean isFixed() {
            return false;
        }

        public boolean isExpired() {
            return eol <= System.currentTimeMillis();
        }
    }
}
