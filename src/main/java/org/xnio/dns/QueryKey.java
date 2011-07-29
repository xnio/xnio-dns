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

final class QueryKey {
    private final RRClass rrClass;
    private final RRType rrType;
    private final Domain domain;
    private final int hashCode;

    QueryKey(final Domain domain, final RRClass rrClass, final RRType rrType) {
        this.rrClass = rrClass;
        this.rrType = rrType;
        this.domain = domain;
        int result = rrClass.hashCode();
        result = 31 * result + rrType.hashCode();
        result = 31 * result + domain.hashCode();
        hashCode = result;
    }

    public boolean equals(final Object o) {
        if (this == o) return true;
        if (! (o instanceof QueryKey)) return false;
        final QueryKey cacheKey = (QueryKey) o;
        if (hashCode != cacheKey.hashCode) return false;
        if (!domain.equals(cacheKey.domain)) return false;
        if (rrClass != cacheKey.rrClass) return false;
        if (rrType != cacheKey.rrType) return false;
        return true;
    }

    public int hashCode() {
        return hashCode;
    }
}
