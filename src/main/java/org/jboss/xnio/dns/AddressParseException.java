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
 * An address parse exception.  Thrown when a network address contains invalid characters or is improperly formatted.
 */
public class AddressParseException extends IllegalArgumentException {

    private static final long serialVersionUID = -8829167161458971075L;

    /**
     * Constructs a <tt>DomainParseException</tt> with no detail message. The cause is not initialized, and may subsequently
     * be initialized by a call to {@link #initCause(Throwable) initCause}.
     */
    public AddressParseException() {
    }

    /**
     * Constructs a <tt>DomainParseException</tt> with the specified detail message. The cause is not initialized, and may
     * subsequently be initialized by a call to {@link #initCause(Throwable) initCause}.
     *
     * @param msg the detail message
     */
    public AddressParseException(String msg) {
        super(msg);
    }

    /**
     * Constructs a <tt>DomainParseException</tt> with the specified cause. The detail message is set to:
     * <pre>
     *  (cause == null ? null : cause.toString())</pre>
     * (which typically contains the class and detail message of <tt>cause</tt>).
     *
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method)
     */
    public AddressParseException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a <tt>DomainParseException</tt> with the specified detail message and cause.
     *
     * @param msg the detail message
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method)
     */
    public AddressParseException(String msg, Throwable cause) {
        super(msg, cause);
    }

    private String address;

    /**
     * Set the address string that failed to parse.
     *
     * @param address the address string
     */
    public void setAddress(final String address) {
        this.address = address;
    }

    /** {@inheritDoc} */
    public String getMessage() {
        final String msg = super.getMessage();
        final String address = this.address;
        if (address != null) {
            if (msg != null) {
                return msg + ": \"" + address + "\"";
            } else {
                return '"' + address + '"';
            }
        } else {
            return msg;
        }
    }
}