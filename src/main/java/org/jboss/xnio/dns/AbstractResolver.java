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
import org.jboss.xnio.AbstractConvertingIoFuture;
import org.jboss.xnio.dns.record.InetARecord;
import org.jboss.xnio.dns.record.InetAAAARecord;
import org.jboss.xnio.dns.record.PtrRecord;
import org.jboss.xnio.dns.record.TxtRecord;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.io.IOException;

public abstract class AbstractResolver implements Resolver {

    public IoFuture<Answer> resolve(final InetAddress server, final Domain name, final RRClass rrClass, final RRType rrType, final Set<Flag> flags) {
        return resolve(new InetSocketAddress(server, 53), name, rrClass, rrType, flags);
    }

    protected abstract SocketAddress getServerAddress();

    public IoFuture<Answer> resolve(final Domain name, final RRClass rrClass, final RRType rrType, final Set<Flag> flags) {
        return resolve(getServerAddress(), name, rrClass, rrType, flags);
    }

    public IoFuture<Answer> resolve(final Domain name, final RRClass rrClass, final RRType rrType) {
        return resolve(name, rrClass, rrType, Collections.<Flag>emptySet());
    }

    public IoFuture<Answer> resolve(final Domain name, final RRType rrType) {
        return resolve(name, RRClass.IN, rrType);
    }

    public IoFuture<List<InetAddress>> resolveAllInet(final Domain name) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        return new FutureInetAddressList(resolve(name, RRClass.IN, RRType.ANY));
    }

    public IoFuture<InetAddress> resolveInet(final Domain name) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        return new FutureInetAddress(resolve(name, RRClass.IN, RRType.ANY));
    }

    public IoFuture<List<Inet4Address>> resolveAllInet4(final Domain name) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        return new FutureInet4AddressList(resolve(name, RRClass.IN, RRType.A));
    }

    public IoFuture<Inet4Address> resolveInet4(final Domain name) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        return new FutureInet4Address(resolve(name, RRClass.IN, RRType.A));
    }

    public IoFuture<List<Inet6Address>> resolveAllInet6(final Domain name) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        return new FutureInet6AddressList(resolve(name, RRClass.IN, RRType.AAAA));
    }

    public IoFuture<Inet6Address> resolveInet6(final Domain name) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        return new FutureInet6Address(resolve(name, RRClass.IN, RRType.AAAA));
    }

    public IoFuture<Domain> resolveReverse(final InetAddress address) {
        if (address == null) {
            throw new NullPointerException("address is null");
        }
        return new FuturePtrDomain(resolve(Domain.reverseArpa(address), RRClass.IN, RRType.PTR));
    }

    public IoFuture<List<String>> resolveText(final Domain name) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        return new FutureText(resolve(name, RRClass.IN, RRType.TXT));
    }

    private static final class FutureText extends AbstractConvertingIoFuture<List<String>, Answer> {

        protected FutureText(final IoFuture<? extends Answer> delegate) {
            super(delegate);
        }

        protected List<String> convert(final Answer arg) throws IOException {
            final List<Record> answers = arg.getAnswerRecords();
            final List<String> strings = new ArrayList<String>(answers.size());
            for (Record answer : answers) {
                if (answer instanceof TxtRecord) {
                    strings.add(((TxtRecord)answer).getText());
                }
            }
            return strings;
        }
    }

    private static final class FuturePtrDomain extends AbstractConvertingIoFuture<Domain, Answer> {

        protected FuturePtrDomain(final IoFuture<? extends Answer> delegate) {
            super(delegate);
        }

        protected Domain convert(final Answer arg) throws IOException {
            final List<Record> answers = arg.getAnswerRecords();
            for (Record answer : answers) {
                if (answer instanceof PtrRecord) {
                    return ((PtrRecord)answer).getTarget();
                }
            }
            // not found...
            return null;
        }
    }

    private static final class FutureInetAddressList extends AbstractConvertingIoFuture<List<InetAddress>, Answer> {

        protected FutureInetAddressList(final IoFuture<? extends Answer> delegate) {
            super(delegate);
        }

        protected List<InetAddress> convert(final Answer arg) throws IOException {
            final List<Record> answers = arg.getAnswerRecords();
            final List<InetAddress> list = new ArrayList<InetAddress>(answers.size());
            for (Record record : answers) {
                if (record instanceof InetARecord) {
                    final InetARecord aRecord = (InetARecord) record;
                    list.add(aRecord.getAddress());
                } else if (record instanceof InetAAAARecord) {
                    final InetAAAARecord aaaaRecord = (InetAAAARecord) record;
                    list.add(aaaaRecord.getAddress());
                }
            }
            return list;
        }
    }

    private static final class FutureInetAddress extends AbstractConvertingIoFuture<InetAddress, Answer> {

        protected FutureInetAddress(final IoFuture<? extends Answer> delegate) {
            super(delegate);
        }

        protected InetAddress convert(final Answer arg) throws IOException {
            final List<Record> answers = arg.getAnswerRecords();
            for (Record record : answers) {
                if (record instanceof InetARecord) {
                    final InetARecord aRecord = (InetARecord) record;
                    return aRecord.getAddress();
                } else if (record instanceof InetAAAARecord) {
                    final InetAAAARecord aaaaRecord = (InetAAAARecord) record;
                    return aaaaRecord.getAddress();
                }
            }
            return null;
        }
    }

    private static final class FutureInet4AddressList extends AbstractConvertingIoFuture<List<Inet4Address>, Answer> {

        protected FutureInet4AddressList(final IoFuture<? extends Answer> delegate) {
            super(delegate);
        }

        protected List<Inet4Address> convert(final Answer arg) throws IOException {
            final List<Record> answers = arg.getAnswerRecords();
            List<Inet4Address> list = new ArrayList<Inet4Address>(answers.size());
            for (Record record : answers) {
                if (record instanceof InetARecord) {
                    final InetARecord aRecord = (InetARecord) record;
                    list.add(aRecord.getAddress());
                }
            }
            return list;
        }
    }

    private static final class FutureInet4Address extends AbstractConvertingIoFuture<Inet4Address, Answer> {

        protected FutureInet4Address(final IoFuture<? extends Answer> delegate) {
            super(delegate);
        }

        protected Inet4Address convert(final Answer arg) throws IOException {
            final List<Record> answers = arg.getAnswerRecords();
            for (Record record : answers) {
                if (record instanceof InetARecord) {
                    final InetARecord aRecord = (InetARecord) record;
                    return aRecord.getAddress();
                }
            }
            return null;
        }
    }

    private static final class FutureInet6AddressList extends AbstractConvertingIoFuture<List<Inet6Address>, Answer> {

        protected FutureInet6AddressList(final IoFuture<? extends Answer> delegate) {
            super(delegate);
        }

        protected List<Inet6Address> convert(final Answer arg) throws IOException {
            final List<Record> answers = arg.getAnswerRecords();
            List<Inet6Address> list = new ArrayList<Inet6Address>(answers.size());
            for (Record record : answers) {
                if (record instanceof InetAAAARecord) {
                    final InetAAAARecord aaaaRecord = (InetAAAARecord) record;
                    list.add(aaaaRecord.getAddress());
                }
            }
            return list;
        }
    }

    private static final class FutureInet6Address extends AbstractConvertingIoFuture<Inet6Address, Answer> {

        protected FutureInet6Address(final IoFuture<? extends Answer> delegate) {
            super(delegate);
        }

        protected Inet6Address convert(final Answer arg) throws IOException {
            final List<Record> answers = arg.getAnswerRecords();
            for (Record record : answers) {
                if (record instanceof InetAAAARecord) {
                    final InetAAAARecord aRecord = (InetAAAARecord) record;
                    return aRecord.getAddress();
                }
            }
            return null;
        }
    }

}
