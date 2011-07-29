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

/**
 * The possible result code values.
 */
public enum ResultCode {
    NOERROR,
    FORMAT_ERROR,
    SERVER_FAILURE,
    NXDOMAIN,
    NOT_IMPLEMENTED,
    REFUSED,
    UNKNOWN;

    /**
     * Get the result code from its integer representation.  If the integer does not represent a known
     * result code, then {@link #UNKNOWN} is returned.
     *
     * @param rcode the integer code
     * @return the code
     */
    public static ResultCode fromInt(final int rcode) {
        switch (rcode) {
            case 0: return NOERROR;
            case 1: return FORMAT_ERROR;
            case 2: return SERVER_FAILURE;
            case 3: return NXDOMAIN;
            case 4: return NOT_IMPLEMENTED;
            case 5: return REFUSED;
            default: return UNKNOWN;
        }
    }
}
