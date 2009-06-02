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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import static java.lang.Character.toLowerCase;
import static java.lang.Math.min;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Domain implements Serializable {

    private static final long serialVersionUID = 958839132904552342L;

    private static class Root extends Domain {

        private static final long serialVersionUID = 9129338222551955950L;

        private Root() {
            //noinspection ZeroLengthArrayAllocation
            super(new Label[0]);
        }

        public boolean equals(final Object obj) {
            return obj == ROOT;
        }

        public int hashCode() {
            return 1;
        }

        public String toString() {
            return "Domain: .";
        }

        public Domain getParent() {
            return null;
        }

        public boolean isSubdomainOf(final Domain parent) {
            return parent == ROOT;
        }
    }

    private final Label[] parts;
    private transient int hashCode;

    public static final Domain ROOT = new Root();

    private Domain(final Label[] parts) {
        this.parts = parts;
    }

    private static Label[] getLabels(final int cnt, final int index, final String hostName) {
        final int nextDot = hostName.indexOf('.', index);
        final Label[] labels;
        if (nextDot == -1) {
            labels = new Label[cnt + 1];
            labels[cnt] = Label.fromString(hostName.substring(index));
        } else {
            labels = getLabels(cnt + 1, nextDot + 1, hostName);
            labels[cnt] = Label.fromString(hostName.substring(index, nextDot));
        }
        return labels;
    }

    public static Domain fromString(final String hostName) {
        if (".".equals(hostName)) {
            return ROOT;
        }
        final int len = hostName.length();
        if (len == 0) {
            throw new IllegalArgumentException("Empty string is not a valid domain");
        }
        if (hostName.lastIndexOf('.') == len - 1) {
            return new Domain(getLabels(0, 0, hostName.substring(0, len - 1)));
        } else {
            return new Domain(getLabels(0, 0, hostName));
        }
    }

    public static Domain reverseArpa(final InetAddress address) {
        if (address instanceof Inet4Address) {
            return reverseArpa((Inet4Address) address);
        } else if (address instanceof Inet6Address) {
            return reverseArpa((Inet6Address) address);
        } else {
            throw new IllegalArgumentException("Unknown address type: " + address.getClass());
        }
    }

    public static Domain reverseArpa(final Inet4Address address) {
        if (address == null) {
            throw new NullPointerException("address is null");
        }
        final byte[] bytes = address.getAddress();
        final Label[] labels = new Label[6];
        labels[0] = Label.fromString(Integer.toString(bytes[3] & 0xff));
        labels[1] = Label.fromString(Integer.toString(bytes[2] & 0xff));
        labels[2] = Label.fromString(Integer.toString(bytes[1] & 0xff));
        labels[3] = Label.fromString(Integer.toString(bytes[0] & 0xff));
        labels[4] = Label.IN_ADDR;
        labels[5] = Label.ARPA;
        return new Domain(labels);
    }

    public static Domain reverseArpa(final Inet6Address address) {
        if (address == null) {
            throw new NullPointerException("address is null");
        }
        final byte[] bytes = address.getAddress();
        final Label[] labels = new Label[34];
        int j = 0;
        for (int i = 15; i >= 0; i--) {
            byte b = bytes[i];
            labels[j++] = Label.HEX_DIGITS[(b & 0xf0) >> 4];
            labels[j++] = Label.HEX_DIGITS[b & 0x0f];
        }
        labels[33] = Label.IN_ADDR;
        labels[34] = Label.ARPA;
        return new Domain(labels);
    }

    public int hashCode() {
        int hc;
        //noinspection NonFinalFieldReferencedInHashCode
        return ((hc = hashCode) == 0) ? (hashCode = Arrays.hashCode(parts)) : hc;
    }

    public boolean equals(final Object obj) {
        return this == obj || obj instanceof Domain && Arrays.equals(parts, ((Domain)obj).parts);
    }

    public Label[] getParts() {
        return parts.clone();
    }

    public String toString() {
        final StringBuilder builder = new StringBuilder();
        for (Label part : parts) {
            builder.append(part);
            builder.append('.');
        }
        return builder.toString();
    }

    public Domain getParent() {
        final Label[] parts = this.parts;
        final int len = parts.length;
        if (len == 0) {
            return ROOT;
        }
        final Label[] newParts = new Label[len - 1];
        System.arraycopy(parts, 1, newParts, 0, len - 1);
        return new Domain(newParts);
    }

    public Domain getSubdomain(Label subLabel) {
        if (subLabel == null) {
            throw new NullPointerException("subLabel is null");
        }
        final Label[] parts = this.parts;
        final int len = parts.length;
        final Label[] newParts = new Label[len + 1];
        System.arraycopy(parts, 0, newParts, 0, len);
        newParts[len] = subLabel;
        return new Domain(newParts);
    }

    public boolean isSubdomainOf(Domain parent) {
        final Label[] parts = this.parts;
        final int len = parts.length;
        final Label[] parentParts = parent.parts;
        final int parentLen = parentParts.length;
        if (len > parentLen) {
            return false;
        } else if (len == parentLen) {
            return Arrays.equals(parts, parentParts);
        } else {
            final int offs = len - parentLen;
            for (int i = 0; i < parentLen; i ++) {
                if (! parts[i + offs].equals(parentParts[i])) {
                    return false;
                }
            }
            return true;
        }
    }

    // string parser methods

    private static boolean isLabelChar(char ch) {
        switch (ch) {
            case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':

            case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g': case 'h': case 'i': case 'j':
            case 'k': case 'l': case 'm': case 'n': case 'o': case 'p': case 'q': case 'r': case 's': case 't':
            case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':

            case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G': case 'H': case 'I': case 'J':
            case 'K': case 'L': case 'M': case 'N': case 'O': case 'P': case 'Q': case 'R': case 'S': case 'T':
            case 'U': case 'V': case 'W': case 'X': case 'Y': case 'Z':
            
            case '-': {
                return true;
            }
        }
        return false;
    }

    private static boolean isLabelEndChar(char ch) {
        switch (ch) {
            case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':

            case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g': case 'h': case 'i': case 'j':
            case 'k': case 'l': case 'm': case 'n': case 'o': case 'p': case 'q': case 'r': case 's': case 't':
            case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':

            case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G': case 'H': case 'I': case 'J':
            case 'K': case 'L': case 'M': case 'N': case 'O': case 'P': case 'Q': case 'R': case 'S': case 'T':
            case 'U': case 'V': case 'W': case 'X': case 'Y': case 'Z': {
                return true;
            }
        }
        return false;
    }

    public static class Label implements CharSequence, Serializable, Comparable<Label> {

        private static final long serialVersionUID = 8060913779411633107L;

        private Label(final byte[] bytes) {
            this.bytes = bytes;
        }

        public static Label fromString(final CharSequence charSequence) {
            if (charSequence == null) {
                throw new NullPointerException("charSequence is null");
            }
            if (charSequence instanceof Label) {
                return (Label) charSequence;
            }
            final int len = charSequence.length();
            if (len == 0) {
                throw new IllegalArgumentException("length must be at least 1");
            }
            final byte[] bytes = new byte[len];
            if (! isLabelEndChar(charSequence.charAt(0))) {
                throw new DomainParseException("Invalid label start character '" + charSequence.charAt(0) + "'", 0);
            }
            bytes[0] = (byte) toLowerCase(charSequence.charAt(0));
            for (int i = 1; i < len - 1; i ++) {
                final char ch = charSequence.charAt(i);
                if (!isLabelChar(ch)) {
                    throw new DomainParseException("Invalid character '" + ch + "'", i);
                }
                bytes[i] = (byte) toLowerCase(ch);
            }
            if (! isLabelEndChar(charSequence.charAt(len - 1))) {
                throw new DomainParseException("Invalid label end character '" + charSequence.charAt(len - 1) + "'", len - 1);
            }
            bytes[len - 1] = (byte) toLowerCase(charSequence.charAt(len - 1));
            return new Label(bytes);
        }

        public static Label fromBytes(final int length, final ByteBuffer buffer) {
            final byte[] bytes = new byte[length];
            for (int i = 0; i < bytes.length; i++) {
                char ch = (char) buffer.get();
                if (!(i == 0 || i == bytes.length - 1 ? isLabelEndChar(ch) : isLabelChar(ch))) {
                    throw new DomainParseException("Invalid character '" + ch + "'", i);
                }
                bytes[i] = (byte) toLowerCase(ch);
            }
            return new Label(bytes);
        }

        public static final Label IN_ADDR = fromString("in-addr");
        public static final Label IP6 = fromString("ip6");
        public static final Label ARPA = fromString("arpa");

        private static final Label[] HEX_DIGITS;

        static {
            final Label[] labels = new Label[16];
            for (int i = 0; i < labels.length; i++) {
                labels[i] = fromString(Integer.toHexString(i));
            }
            HEX_DIGITS = labels;
        }

        private final byte[] bytes;
        private transient int hashCode;

        public void writeTo(ByteBuffer buf) {
            buf.put(bytes);
        }

        public byte[] getBytes() {
            return bytes.clone();
        }

        public String getAscii() {
            try {
                return new String(bytes, "US-ASCII");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("US-ASCII is not supported", e);
            }
        }

        public int length() {
            return bytes.length;
        }

        public char charAt(final int index) {
            return (char) bytes[index];
        }

        public CharSequence subSequence(final int start, final int end) {
            final int len = bytes.length;
            if (start < 0 || start > len || end < 0 || end > len || end < start) {
                throw new IndexOutOfBoundsException();
            }
            return new SubSeq(this, start, end - start);
        }

        public String toString() {
            return "\"" + getAscii() + "\"";
        }

        public boolean equals(final Object obj) {
            return this == obj || obj instanceof Label && Arrays.equals(bytes, ((Label)obj).bytes);
        }

        public int hashCode() {
            int hc;
            //noinspection NonFinalFieldReferencedInHashCode
            return ((hc = hashCode) == 0) ? (hashCode = Arrays.hashCode(bytes)) : hc;
        }

        public int compareTo(final Label o) {
            if (this == o) { return 0; }
            final byte[] bytes = this.bytes;
            final byte[] oBytes = o.bytes;
            final int bytesLen = bytes.length;
            final int oBytesLen = oBytes.length;
            final int len = min(bytesLen, oBytesLen);
            for (int i = 0; i < len; i ++) {
                final int cmp = Integer.signum(bytes[i] - oBytes[i]);
                if (cmp != 0) {
                    return cmp;
                }
            }
            return Integer.signum(bytesLen - oBytesLen);
        }

        public static Domain fromBytes(ByteBuffer buffer) {
            return fromBytes(buffer, new ArrayList<Label>(), 0);
        }

        private static Domain fromBytes(ByteBuffer buffer, List<Label> labels, int depth) {
            if (depth > 32) {
                throw new IllegalStateException("Nested level too deep");
            }
            final byte leadByte = buffer.get();
            for (;;) switch (leadByte & 0xc0) {
                case 0x00: {
                    if (leadByte == 0) {
                        if (labels.size() == 0) {
                            return ROOT;
                        } else {
                            return new Domain(labels.toArray(new Label[labels.size()]));
                        }
                    }
                    labels.add(Label.fromBytes(leadByte & 0x1f, buffer));
                }
                case 0xc0: {
                    final int offs = ((leadByte & 0x3F) << 8) | (buffer.get() & 0xff);
                    final ByteBuffer newBuf = buffer.duplicate();
                    newBuf.position(offs);
                    return fromBytes(newBuf, labels, depth + 1);
                }
                default: {
                    throw new IllegalStateException("Invalid label byte");
                }
            }
        }

        private static final class SubSeq implements CharSequence, Serializable {

            private static final long serialVersionUID = 7148702431124781511L;

            private final Label label;
            private final int offs;
            private final int len;

            private SubSeq(final Label label, final int offs, final int len) {
                this.label = label;
                this.offs = offs;
                this.len = len;
            }

            public int length() {
                return len;
            }

            public char charAt(final int index) {
                if (index < 0 || index > len) {
                    throw new IndexOutOfBoundsException();
                }
                return (char) label.bytes[offs + index];
            }

            public CharSequence subSequence(final int start, final int end) {
                if (start < 0 || start > len || end < 0 || end > len || end < start) {
                    throw new IndexOutOfBoundsException();
                }
                return new SubSeq(label, offs + start, end - start);
            }
        }
    }
}
