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

import org.jboss.xnio.IoFuture;
import java.util.Set;
import java.util.Map;
import java.util.Collections;
import java.util.EnumMap;

public final class LocalZoneResolver extends AbstractResolver {

    private volatile Map<Domain, Map<Key, Set<Record>>> zoneInfo = Collections.emptyMap();

    public IoFuture<Answer> resolve(final Domain name, final RRClass rrClass, final RRType rrType, final Set<ResolverFlag> flags) {
        final Map<Domain, Map<Key, Set<Record>>> zoneInfo = this.zoneInfo;
        Map<Key, Set<Record>> zoneMap;
        if ((zoneMap = zoneInfo.get(name)) != null) {
            final Set<Record> recordSet = zoneMap.get(keyFor(rrType, rrClass));
            
        }
        final Domain.Label[] parts = name.getParts();
        
        return null;
    }

    private static final class Key {
        private final RRClass rrClass;
        private final RRType rrType;
        private final int hashCode;

        private static final int numValues = RRClass.values().length;

        private Key(final RRClass rrClass, final RRType rrType) {
            this.rrClass = rrClass;
            this.rrType = rrType;
            hashCode = rrClass.ordinal() + rrType.ordinal() * numValues;
        }
    }

    private static Key keyFor(RRType rrType, RRClass rrClass) {
        return keys.get(rrType).get(rrClass);
    }

    private static final EnumMap<RRType, EnumMap<RRClass, Key>> keys;

    static {
        final EnumMap<RRType, EnumMap<RRClass, Key>> map = new EnumMap<RRType, EnumMap<RRClass, Key>>(RRType.class);
        for (RRType rrType : RRType.values()) {
            final EnumMap<RRClass, Key> innerMap = new EnumMap<RRClass, Key>(RRClass.class);
            map.put(rrType, innerMap);
            for (RRClass rrClass : RRClass.values()) {
                innerMap.put(rrClass, new Key(rrClass, rrType));
            }
        }
        keys = map;
    }
}
