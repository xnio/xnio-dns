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

import java.io.IOException;

public final class DNSException extends IOException {

    private static final long serialVersionUID = 3313733955232311955L;
    private final Code code;

    /**
     * Constructs a <tt>DNSException</tt> with no detail message. The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause(Throwable) initCause}.
     *
     * @param code the reason code
     */
    public DNSException(final Code code) {
        this.code = code;
    }

    /**
     * Constructs a <tt>DNSException</tt> with the specified detail message. The cause is not initialized, and may
     * subsequently be initialized by a call to {@link #initCause(Throwable) initCause}.
     *
     * @param code the reason code
     * @param msg the detail message
     */
    public DNSException(final Code code, final String msg) {
        super(msg);
        this.code = code;
    }

    /**
     * Constructs a <tt>DNSException</tt> with the specified cause. The detail message is set to:
     * <pre>
     *  (cause == null ? null : cause.toString())</pre>
     * (which typically contains the class and detail message of <tt>cause</tt>).
     *
     * @param code the reason code
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method)
     */
    public DNSException(final Code code, final Throwable cause) {
        initCause(cause);
        this.code = code;
    }

    /**
     * Constructs a <tt>DNSException</tt> with the specified detail message and cause.
     *
     * @param code the reason code
     * @param msg the detail message
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method)
     */
    public DNSException(final Code code, final String msg, final Throwable cause) {
        super(msg);
        initCause(cause);
        this.code = code;
    }

    /**
     * Get the reason code.
     *
     * @return the reason code
     */
    public Code getCode() {
        return code;
    }

    /**
     * The possible reason code values.
     */
    public enum Code {
        NOERROR,
        FORMAT_ERROR,
        SERVER_FAILURE,
        NAME_ERROR,
        NOT_IMPLEMENTED,
        REFUSED,
        UNKNOWN,
        ;
    }
}
