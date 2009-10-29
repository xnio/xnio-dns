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

import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.Random;
import java.util.concurrent.Executor;
import java.nio.ByteBuffer;
import java.nio.BufferUnderflowException;
import java.nio.channels.Channel;
import java.io.IOException;
import org.jboss.xnio.IoFuture;
import org.jboss.xnio.ChannelListener;
import org.jboss.xnio.Pool;
import org.jboss.xnio.Buffers;
import org.jboss.xnio.IoUtils;
import org.jboss.xnio.ChannelSource;
import org.jboss.xnio.FutureResult;
import org.jboss.xnio.log.Logger;
import org.jboss.xnio.channels.UdpChannel;

/**
 * A network resolver which uses UDP to contact a remote server.
 */
public final class UdpNetworkResolver extends AbstractNetworkResolver {

    private static final Logger log = Logger.getLogger("org.jboss.xnio.dns.resolver.udp");

    private final Pool<ByteBuffer> bufferPool = Buffers.createHeapByteBufferAllocator(512);
    private final ChannelSource<UdpChannel> channelSource;
    private final Executor executor;
    private final Random random;

    /**
     * Construct a new UDP network resolver.  In order to provide resilient security, the given channel source
     * should choose port numbers at random.
     *
     * @param executor the executor to use for asynchronous notifications
     * @param channelSource the channel source to use to create new UDP client channels
     * @param random the RNG to use to generate request IDs
     */
    public UdpNetworkResolver(final Executor executor, final ChannelSource<UdpChannel> channelSource, final Random random) {
        this.executor = executor;
        this.channelSource = channelSource;
        this.random = random;
    }

    /** {@inheritDoc} */
    public Resolver resolverFor(final SocketAddress server) {
        return new ResolverImpl((InetSocketAddress) server);
    }

    private class ResolverImpl extends AbstractResolver implements Resolver {
        private final InetSocketAddress serverAddress;

        ResolverImpl(final InetSocketAddress serverAddress) {
            this.serverAddress = serverAddress;
        }

        public IoFuture<Answer> resolve(final Domain name, final RRClass rrClass, final RRType rrType, final Set<ResolverFlag> flags) {
            final int id = random.nextInt() & 0xffff;
            final FutureResult<Answer> manager = new FutureResult<Answer>(executor);
            final IoFuture<? extends UdpChannel> futureChannel = channelSource.open(new ChannelListener<UdpChannel>() {
                public void handleEvent(final UdpChannel channel) {
                    channel.getCloseSetter().set(new ChannelListener<Channel>() {
                        public void handleEvent(final Channel channel) {
                            // cancel request if it isn't done
                            manager.setCancelled();
                        }
                    });
                    manager.addCancelHandler(IoUtils.closingCancellable(channel));
                    channel.getReadSetter().set(new ReadListener(id, manager, name, rrClass, rrType));
                    channel.resumeReads();
                    final ByteBuffer buffer = bufferPool.allocate();
                    buffer.putShort((short) id);
                    buffer.putShort((short) (flags.contains(ResolverFlag.NO_RECURSION) ? 0 : 1 << 7));
                    buffer.putShort((short) 1);
                    buffer.putShort((short) 0);
                    buffer.putShort((short) 0);
                    buffer.putShort((short) 0);
                    for (Domain.Label label : name.getParts()) {
                        final byte[] bytes = label.getBytes();
                        buffer.put((byte) bytes.length);
                        buffer.put(bytes);
                    }
                    buffer.put((byte) 0);
                    buffer.putShort((short) rrType.getId());
                    buffer.putShort((short) rrClass.getId());
                    buffer.flip();
                    try {
                        channel.send(serverAddress, buffer);
                    } catch (IOException e) {
                        manager.setException(e);
                        IoUtils.safeClose(channel);
                    }
                }
            });
            manager.addCancelHandler(futureChannel);
            futureChannel.addNotifier(new IoFuture.HandlingNotifier<Channel, FutureResult<Answer>>() {
                public void handleCancelled(final FutureResult<Answer> attachment) {
                    attachment.setCancelled();
                }
            }, manager);
            return manager.getIoFuture();
        }
    }

    private class ReadListener implements ChannelListener<UdpChannel> {

        private final int id;
        private final Domain name;
        private final RRClass rrClass;
        private final RRType rrType;
        private final FutureResult<Answer> request;

        ReadListener(final int id, final FutureResult<Answer> request, final Domain name, final RRClass rrClass, final RRType rrType) {
            this.id = id;
            this.request = request;
            this.name = name;
            this.rrClass = rrClass;
            this.rrType = rrType;
        }

        public void handleEvent(final UdpChannel channel) {
            final ByteBuffer buffer = bufferPool.allocate();
            if (buffer == null) {
                // todo - delay for a time?
                request.setException(new IOException("No buffers available to receive reply"));
                IoUtils.safeClose(channel);
                return;
            }
            try {
                channel.receive(buffer);
            } catch (IOException e) {
                log.error("Closing channel '%s' due to I/O error on read: %s", channel, e);
                IoUtils.safeClose(channel);
            }
            buffer.flip();
            try {
                final int id = buffer.getShort() & 0xffff;
                if (id != this.id) {
                    // ignore wrong reply ID
                    channel.resumeReads();
                    return;
                }
                final int flags = buffer.getShort() & 0xffff;
                if ((flags & 1 << 0) == 0) {
                    // ignore query
                    channel.resumeReads();
                    return;
                }
                final Answer.Builder builder = Answer.builder();
                if (((flags & 1 << 6) != 0)) {
                    // todo truncation request - handle via TCP some other time
                    request.setResult(builder.setHeaderInfo(name, rrClass, rrType, ResultCode.FORMAT_ERROR).create());
                    IoUtils.safeClose(channel);
                    return;
                }
                builder.setResultCode(ResultCode.fromInt(flags >> 12));
                if (((flags & 1 << 5) != 0)) builder.addFlag(Answer.Flag.AUTHORATIVE);
                if (((flags & 1 << 8) != 0)) builder.addFlag(Answer.Flag.RECURSION_AVAILABLE);
                final int qcnt = buffer.getShort() & 0xffff;
                if (qcnt != 1) {
                    // ignore bogus reply
                    channel.resumeReads();
                    return;
                }
                final int ancnt = buffer.getShort() & 0xffff;
                final int nscnt = buffer.getShort() & 0xffff;
                final int arcnt = buffer.getShort() & 0xffff;
                builder.setQueryDomain(Domain.fromBytes(buffer));
                builder.setQueryRRType(RRType.fromInt(buffer.getShort() & 0xffff));
                builder.setQueryRRClass(RRClass.fromInt(buffer.getShort() & 0xffff));
                for (int i = 0; i < ancnt; i ++) {
                    builder.addAnswerRecord(Record.fromBytes(buffer));
                }
                for (int i = 0; i < nscnt; i ++) {
                    builder.addAuthorityRecord(Record.fromBytes(buffer));
                }
                for (int i = 0; i < arcnt; i ++) {
                    builder.addAdditionalRecord(Record.fromBytes(buffer));
                }
                request.setResult(builder.create());
                IoUtils.safeClose(channel);
            } catch (BufferUnderflowException e) {
                request.setResult(Answer.builder().setHeaderInfo(name, rrClass, rrType, ResultCode.FORMAT_ERROR).create());
                IoUtils.safeClose(channel);
            }
        }
    }
}
