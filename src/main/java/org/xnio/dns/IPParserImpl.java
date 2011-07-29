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

import java.nio.CharBuffer;
import java.net.InetAddress;
import java.net.Inet6Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import static org.xnio.Buffers.unget;

final class IPParserImpl {

    private IPParserImpl() {
    }

    private static boolean isHexDigit(char ch) {
        switch (ch) {
            case '0': case '1': case '2': case '3':
            case '4': case '5': case '6': case '7':
            case '8': case '9': case 'a': case 'A': case 'b': case 'B':
            case 'c': case 'C': case 'd': case 'D': case 'e': case 'E': case 'f': case 'F': return true;
            default: return false;
        }
    }

    private static final int DIGITS = 0;
    private static final int DOT = 1;
    private static final int COLON = 2;
    private static final int DBL_COLON = 3;
    private static final int SCOPE = 4;

    private static final class TextHolder {
        String text;
    }

    private static int nextToken(TextHolder state, String str, CharBuffer buffer) {
        if (! buffer.hasRemaining()) {
            return -1;
        }
        char next = buffer.get();
        if (isHexDigit(next)) {
            unget(buffer, 1);
            final int start = buffer.position();
            while (buffer.hasRemaining()) {
                if (! isHexDigit(buffer.get())) {
                    unget(buffer, 1);
                    break;
                }
            }
            state.text = str.substring(start, buffer.position());
            return DIGITS;
        } else if (next == ':') {
            if (buffer.hasRemaining()) {
                if (buffer.get() == ':') {
                    return DBL_COLON;
                } else {
                    unget(buffer, 1);
                }
            }
            return COLON;
        } else if (next == '%') {
            final int start = buffer.position();
            while (buffer.hasRemaining()) {
                if (! Character.isLetterOrDigit(buffer.get())) {
                    unget(buffer, 1);
                    break;
                }
            }
            state.text = str.substring(start, buffer.position());
            return SCOPE;
        } else if (next == '.') {
            return DOT;
        } else {
            throw notValid();
        }
    }

    private static byte parseOctet(String str) {
        try {
            final int val = Integer.parseInt(str);
            if (val >= 0 && val <= 255) {
                return (byte) val;
            }
            throw notValid();
        } catch (NumberFormatException e) {
            throw notValid();
        }
    }

    private static int parseSeg(String str) {
        try {
            final int val = Integer.parseInt(str, 16);
            if (val >= 0 && val <= 0xffff) {
                return val;
            }
            throw notValid();
        } catch (NumberFormatException e) {
            throw notValid();
        }
    }

    private static final int STATE_INITIAL          = 0; // initial state
    private static final int STATE_DIGITS           = 1; // got initial digits, expecting separator.
    private static final int STATE_IP4              = 2; // got ip4 separator, expecting ip4 digits.
    private static final int STATE_IP4_DIG          = 3; // got ip4 digits, expecting ip4 separator or end.
    private static final int STATE_IP6_FIRST        = 4; // got ip6 separator, expecting ip4 or ip6 digits (first section)
    private static final int STATE_IP6_FIRST_DIG    = 5; // got ip6 leading digits, expecting a separator : or :: or % or . or end
    private static final int STATE_IP6_TRAIL_FIRST  = 6; // got ip6 :: separator, expecting ip4 or ip6 digits or % or end (trailing section)
    private static final int STATE_IP6_TRAIL        = 7; // got ip6 separator, expecting ip4 or ip6 digits (trailing section)
    private static final int STATE_IP6_TRAIL_DIG    = 8; // got ip6 trailing digits, expecting a separator : or % or . or end
    private static final int STATE_IP6_TRAIL_IP4    = 9; // got ip4 separator (ip6 suffix), expecting ip4 digits
    private static final int STATE_IP6_TRAIL_IP4_DIG= 10; // got ip4 digits (ip6 suffix), expecting ip4 separator or % or end
    private static final int STATE_IP6_TRAIL_SCOPE  = 11; // got scope ID, expecting end

    enum Kind {
        IP,
        IPv4,
        IPv6,
    }

    static InetAddress parseAddress(final Kind kind, final String hostName, final String str) {
        final CharBuffer buffer = CharBuffer.wrap(str);
        final TextHolder textHolder = new TextHolder();
        String num = null;
        int ip4segments = 0, ip6leaders = 0, ip6trailers = 0;
        int state = 0;
        byte[] address = null, trailer = null;
        try {
            try {
                for (;;) {
                    switch (nextToken(textHolder, str, buffer)) {
                        case -1: { // end of input.  Does everything check out?
                            switch (state) {
                                case STATE_IP4_DIG: {
                                    if (ip4segments == 4) {
                                        return InetAddress.getByAddress(hostName, address);
                                    } else {
                                        throw notValid();
                                    }
                                }
                                case STATE_IP6_FIRST_DIG: {
                                    if (ip6leaders == 16) {
                                        return InetAddress.getByAddress(hostName, address);
                                    } else {
                                        throw notValid();
                                    }
                                }
                                case STATE_IP6_TRAIL_DIG: {
                                    int seg = parseSeg(num);
                                    trailer[ip6trailers++] = (byte) (seg >> 8);
                                    trailer[ip6trailers++] = (byte) seg;
                                    if (ip6trailers + ip6leaders > 16) {
                                        // too long
                                        throw notValid();
                                    }
                                    System.arraycopy(trailer, 0, address, 16 - ip6trailers, ip6trailers);
                                    return InetAddress.getByAddress(hostName, address);
                                }
                                case STATE_IP6_TRAIL_FIRST: {
                                    if (ip6leaders > 16) {
                                        // too long
                                        throw notValid();
                                    }
                                    return InetAddress.getByAddress(hostName, address);
                                }
                                case STATE_IP6_TRAIL_IP4_DIG: {
                                    if (ip4segments != 4) {
                                        throw notValid();
                                    }
                                    if (ip6trailers + ip6leaders > 16) {
                                        // too long
                                        throw notValid();
                                    }
                                    System.arraycopy(trailer, 0, address, 16 - ip6trailers, ip6trailers);
                                    return InetAddress.getByAddress(hostName, address);
                                }
                                case STATE_IP6_TRAIL_SCOPE: {
                                    if (ip6trailers + ip6leaders > 16) {
                                        // too long
                                        throw notValid();
                                    }
                                    System.arraycopy(trailer, 0, address, 16 - ip6trailers, ip6trailers);
                                    final String nid = textHolder.text;
                                    try {
                                        int si = Integer.parseInt(nid);
                                        return Inet6Address.getByAddress(hostName, address, si);
                                    } catch (NumberFormatException ignored) {
                                        try {
                                            final NetworkInterface ni = NetworkInterface.getByName(nid);
                                            if (ni == null) {
                                                throw notValid();
                                            }
                                            return Inet6Address.getByAddress(hostName, address, ni);
                                        } catch (SocketException e) {
                                            throw notValid();
                                        }
                                    }
                                }
                                default: throw notValid();
                            }
                        }
                        case DIGITS: {
                            switch (state) {
                                case STATE_INITIAL: {
                                    num = textHolder.text;
                                    state = STATE_DIGITS;
                                    continue;
                                }
                                case STATE_IP4: {
                                    address[ip4segments++] = parseOctet(textHolder.text);
                                    state = STATE_IP4_DIG;
                                    continue;
                                }
                                case STATE_IP6_FIRST: {
                                    num = textHolder.text;
                                    state = STATE_IP6_FIRST_DIG;
                                    continue;
                                }
                                case STATE_IP6_TRAIL_FIRST:
                                case STATE_IP6_TRAIL: {
                                    num = textHolder.text;
                                    state = STATE_IP6_TRAIL_DIG;
                                    continue;
                                }
                                case STATE_IP6_TRAIL_IP4: {
                                    trailer[ip6trailers++] = parseOctet(textHolder.text);
                                    ip4segments++;
                                    state = STATE_IP6_TRAIL_IP4_DIG;
                                    continue;
                                }
                                default: throw notValid();
                            }
                        }
                        case DOT: {
                            switch (state) {
                                case STATE_DIGITS: {
                                    if (kind != Kind.IP && kind != Kind.IPv4) {
                                        throw notValid();
                                    }
                                    // def. ip4
                                    address = new byte[4];
                                    address[0] = parseOctet(num);
                                    state = STATE_IP4;
                                    ip4segments = 1;
                                    continue;
                                }
                                case STATE_IP4_DIG: {
                                    if (ip4segments == 4) throw notValid();
                                    state = STATE_IP4;
                                    continue;
                                }
                                case STATE_IP6_FIRST_DIG: {
                                    // only valid if we've already got 12 ip6 digits
                                    if (ip6leaders != 12) throw notValid();
                                    address[ip6leaders ++] = parseOctet(num);
                                    ip4segments = 1;
                                    state = STATE_IP6_TRAIL_IP4;
                                    continue;
                                }
                                case STATE_IP6_TRAIL_DIG: {
                                    // trailing IP4 address
                                    trailer[ip6trailers++] = parseOctet(num);
                                    state = STATE_IP6_TRAIL_IP4;
                                    ip4segments = 1;
                                    continue;
                                }
                                case STATE_IP6_TRAIL_IP4_DIG: {
                                    if (ip4segments == 4) throw notValid();
                                    state = STATE_IP6_TRAIL_IP4;
                                    continue;
                                }
                                default: throw notValid();
                            }
                        }
                        case COLON: {
                            switch (state) {
                                case STATE_DIGITS: {
                                    if (kind != Kind.IP && kind != Kind.IPv6) {
                                        throw notValid();
                                    }
                                    // definitely ipv6
                                    address = new byte[16];
                                    trailer = new byte[16];
                                    int seg = parseSeg(num);
                                    address[ip6leaders++] = (byte) (seg >> 8);
                                    address[ip6leaders++] = (byte) seg;
                                    state = STATE_IP6_FIRST;
                                    continue;
                                }
                                case STATE_IP6_FIRST_DIG: {
                                    int seg = parseSeg(num);
                                    address[ip6leaders++] = (byte) (seg >> 8);
                                    address[ip6leaders++] = (byte) seg;
                                    state = STATE_IP6_FIRST;
                                    continue;
                                }
                                case STATE_IP6_TRAIL_DIG: {
                                    int seg = parseSeg(num);
                                    trailer[ip6trailers++] = (byte) (seg >> 8);
                                    trailer[ip6trailers++] = (byte) seg;
                                    state = STATE_IP6_TRAIL;
                                    continue;
                                }
                                default: throw notValid();
                            }
                        }
                        case DBL_COLON: {
                            switch (state) {
                                case STATE_INITIAL: {
                                    if (kind != Kind.IP && kind != Kind.IPv6) {
                                        throw notValid();
                                    }
                                    address = new byte[16];
                                    trailer = new byte[16];
                                    state = STATE_IP6_TRAIL;
                                    continue;
                                }
                                case STATE_DIGITS: {
                                    if (kind != Kind.IP && kind != Kind.IPv6) {
                                        throw notValid();
                                    }
                                    address = new byte[16];
                                    trailer = new byte[16];
                                    int seg = parseSeg(num);
                                    address[ip6leaders++] = (byte) (seg >> 8);
                                    address[ip6leaders++] = (byte) seg;
                                    state = STATE_IP6_TRAIL;
                                    continue;
                                }
                                case STATE_IP6_FIRST_DIG: {
                                    int seg = parseSeg(num);
                                    address[ip6leaders++] = (byte) (seg >> 8);
                                    address[ip6leaders++] = (byte) seg;
                                    state = STATE_IP6_TRAIL;
                                    continue;
                                }
                                default: throw notValid();
                            }
                        }
                        case SCOPE: {
                            switch (state) {
                                case STATE_IP6_FIRST_DIG: {
                                    if (ip6leaders != 16) {
                                        throw notValid();
                                    }
                                    state = STATE_IP6_TRAIL_SCOPE;
                                    continue;
                                }
                                case STATE_IP6_TRAIL_FIRST: {
                                    state = STATE_IP6_TRAIL_SCOPE;
                                    continue;
                                }
                                case STATE_IP6_TRAIL_DIG: {
                                    // counts will be validated at end
                                    state = STATE_IP6_TRAIL_SCOPE;
                                    continue;
                                }
                                case STATE_IP6_TRAIL_IP4_DIG: {
                                    if (ip4segments != 4) {
                                        throw notValid();
                                    }
                                    state = STATE_IP6_TRAIL_SCOPE;
                                    continue;
                                }
                                default: throw notValid();
                            }
                        }
                        default: throw notValid();
                    }
                }
            } catch (NumberFormatException e) {
                throw notValid();
            } catch (UnknownHostException e) {
                throw notValid();
            } catch (ArrayIndexOutOfBoundsException e) {
                throw notValid();
            }
        } catch (AddressParseException e) {
            final AddressParseException ne = new AddressParseException("Invalid " + kind + " addres string");
            ne.setStackTrace(e.getStackTrace());
            ne.setAddress(str);
            throw ne;
        }
    }

    private static AddressParseException notValid() {
        return new AddressParseException("Invalid IP address string");
    }

}
