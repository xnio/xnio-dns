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

import org.xnio.IoFuture;
import org.xnio.FinishedIoFuture;
import org.xnio.dns.record.SoaRecord;
import java.util.Set;
import java.util.Map;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

public final class LocalZoneResolver extends AbstractResolver {

    private final Resolver nextResolver;
    private volatile Map<Domain, Zone> allZones = Collections.emptyMap();

    public LocalZoneResolver(final Resolver nextResolver) {
        this.nextResolver = nextResolver;
    }

    public IoFuture<Answer> resolve(final Domain name, final RRClass rrClass, final RRType rrType, final Set<ResolverFlag> flags) {
        // search the local zones first
        final Map<Domain, Zone> allZones = this.allZones;
        Zone zone = null;
        for (Domain search = name; search != Domain.ROOT; search = search.getParent()) {
            zone = allZones.get(name);
            if (zone != null) {
                break;
            }
        }
        if (zone == null) {
            return nextResolver.resolve(name, rrClass, rrType, flags);
        }
        final Map<RRType, List<Record>> entries = zone.getInfo().get(name);
        if (entries != null) {
            // zone contains data for this question
            // next, if the question is answered, return it; else return the authority (the NS records or our SOA) in the authority section
            final Answer.Builder builder = Answer.builder().setHeaderInfo(name, rrClass, rrType, ResultCode.NOERROR);
            boolean answered = false;
            if (rrType == RRType.ANY) {
                for (List<Record> records : entries.values()) {
                    for (Record record : records) {
                        if (rrClass == RRClass.ANY || rrClass == record.getRrClass()) {
                            builder.addAnswerRecord(record);
                            answered = true;
                        }
                    }
                }
            } else {
                final List<Record> records = entries.get(rrType);
                if (records != null) for (Record record : records) {
                    if (rrClass == RRClass.ANY || rrClass == record.getRrClass()) {
                        builder.addAnswerRecord(record);
                        answered = true;
                    }
                }
            }
            if (! answered) {
                final List<Record> records = entries.get(RRType.NS);
                if (records != null) {
                    
                } else {
                    builder.addAuthorityRecord(zone.getSoa());
                }
            }
            return new FinishedIoFuture<Answer>(builder.create());
        } else {
            // return our authority + NXDOMAIN
            return new FinishedIoFuture<Answer>(Answer.builder().setHeaderInfo(name, rrClass, rrType, ResultCode.NXDOMAIN).addAuthorityRecord(zone.getSoa()).create());
        }
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

    private static final class Zone {
        private final SoaRecord zoneSoa;
        private final Map<Domain, Map<RRType, List<Record>>> info;

        private Zone(final SoaRecord zoneSoa, final Map<Domain, Map<RRType, List<Record>>> info) {
            this.zoneSoa = zoneSoa;
            this.info = info;
        }

        public SoaRecord getSoa() {
            return zoneSoa;
        }

        public Map<Domain, Map<RRType, List<Record>>> getInfo() {
            return info;
        }
    }
}
