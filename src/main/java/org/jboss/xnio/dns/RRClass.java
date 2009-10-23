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

/**
 *
 */
public enum RRClass {
    UNKNOWN(-1),
    IN(1),
    CH(3),
    HS(4),
    NONE(254),
    ANY(255),
    ;
    private final int id;

    RRClass(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static RRClass fromInt(final int i) {
        switch (i) {
            case 1: return IN;
            case 3: return CH;
            case 4: return HS;
            case 254: return NONE;
            case 255: return ANY;
            default: return UNKNOWN;
        }
    }
}
