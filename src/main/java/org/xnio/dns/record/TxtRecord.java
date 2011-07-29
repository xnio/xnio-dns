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

package org.xnio.dns.record;

import org.xnio.dns.Record;
import org.xnio.dns.Domain;
import org.xnio.dns.RRClass;
import org.xnio.dns.RRType;
import org.xnio.dns.TTLSpec;
import java.nio.ByteBuffer;

/**
 * A record of type {@link RRType#TXT}.
 */
public class TxtRecord extends Record {

    private static final long serialVersionUID = 8852841335529670195L;

    private final String text;

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param rrClass the resource record class
     * @param ttlSpec the TTL spec
     * @param text the text data
     * @param parseArg
     */
    public TxtRecord(final Domain name, final RRClass rrClass, final TTLSpec ttlSpec, final String text, final boolean parseArg) {
        super(name, rrClass, RRType.TXT, ttlSpec);
        if (parseArg) {
            final int len = text.length();
            final StringBuilder builder = new StringBuilder(len);
            boolean in = false;
            for (int i = 0; i < len; i ++) {
                final char ch = text.charAt(i);
                switch (ch) {
                    case '"': in = !in; break;
                    case '\\': if (in) {
                        if (i == len - 1) {
                            throw new IllegalArgumentException("Unexpected end of string");
                        }
                        switch (text.charAt(++i)) {
                            case '\\': builder.append('\\'); break;
                            case 'n': builder.append('\n'); break;
                            case 'f': builder.append('\f'); break;
                            case 't': builder.append('\t'); break;
                            case 'r': builder.append('\r'); break;
                            case 'b': builder.append('\b'); break;
                            case '"': builder.append('"'); break;
                            case '0': builder.append('\0'); break;
                            case 'x': builder.append(Integer.parseInt(text.substring(i + 1, i + 3), 16)); i += 2; break;
                            default: throw invalidChar();
                        }
                        break;
                    } else {
                        throw invalidChar();
                    }
                    case ' ': {
                        if (in) {
                            builder.append(' ');
                        }
                        break;
                    }
                    case '\t':
                    case '\n':
                    case '\r':
                    case '\f': {
                        if (in) {
                            throw invalidChar();
                        }
                        break;
                    }
                    default: {
                        builder.append(ch);
                        if (Character.isISOControl(ch) || ! in) {
                            throw invalidChar();
                        }
                        break;
                    }
                }
            }
            if (in) {
                throw new IllegalArgumentException("Unexpected end of string");
            }
            this.text = builder.toString();
        } else {
            this.text = text;
        }
    }

    private static IllegalArgumentException invalidChar() {
        return new IllegalArgumentException("Invalid character");
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param ttlSpec the TTL spec
     * @param text the text data
     */
    public TxtRecord(final Domain name, final TTLSpec ttlSpec, final String text) {
        this(name, RRClass.IN, ttlSpec, text, false);
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param text the text data
     */
    public TxtRecord(final Domain name, final String text) {
        this(name, TTLSpec.ZERO, text);
    }

    /**
     * Construct a new instance.
     *
     * @param name the domain name
     * @param rrClass the resource record class
     * @param ttlSpec the TTL spec
     * @param recordBuffer the buffer from which the record data should be built
     */
    public TxtRecord(final Domain name, final RRClass rrClass, final TTLSpec ttlSpec, final ByteBuffer recordBuffer) {
        super(name, rrClass, RRType.TXT, ttlSpec);
        final StringBuilder builder = new StringBuilder(recordBuffer.remaining());
        while (recordBuffer.hasRemaining()) {
            for (int len = recordBuffer.get() & 0xff; len >= 0; len --) {
                // Latin-1
                builder.append((char) recordBuffer.get());
            }
        }
        text = builder.toString();
    }

    /**
     * Get the text data.
     *
     * @return the text data
     */
    public String getText() {
        return text;
    }

    /**
     * Get the deparsed text in a form suitable for writing to a file.
     *
     * @return the deparsed text
     */
    public String getDeparsedText() {
        final String text = this.text;
        final int len = text.length();
        final StringBuilder builder = new StringBuilder(len + 2);
        builder.append('"');
        for (int i = 0; i < len; i ++) {
            final char ch = text.charAt(i);
            switch (ch) {
                case '"': builder.append('\\').append('"'); break;
                case '\\': builder.append('\\').append('\\'); break;
                case '\b': builder.append('\\').append('b'); break;
                case '\r': builder.append('\\').append('r'); break;
                case '\n': builder.append('\\').append('n'); break;
                case '\t': builder.append('\\').append('t'); break;
                case '\f': builder.append('\\').append('f'); break;
                case '\0': builder.append('\\').append('0'); break;
                default: if (Character.isISOControl(ch)) {
                    if (ch < 16) {
                        builder.append("\\x0").append(Integer.toHexString((int)ch));
                    } else if (ch < 256) {
                        builder.append("\\x").append(Integer.toHexString((int)ch));
                    } else {
                        builder.append('\\').append('?');
                    }
                } else {
                    builder.append(ch);
                }
            }
        }
        builder.append('"');
        return builder.toString();
    }

    /** {@inheritDoc} */
    protected void appendRData(final StringBuilder builder) {
        builder.append(' ').append(getDeparsedText());
    }
}
