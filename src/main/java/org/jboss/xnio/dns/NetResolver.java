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
import org.jboss.xnio.IoHandlerFactory;
import org.jboss.xnio.IoHandler;
import org.jboss.xnio.FailedIoFuture;
import org.jboss.xnio.AbstractIoFuture;
import org.jboss.xnio.channels.UdpChannel;
import java.util.Set;
import java.util.Collection;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.net.SocketAddress;
import java.io.IOException;
import java.nio.ByteBuffer;

public class NetResolver extends AbstractNetworkResolver {

    private final ScheduledExecutorService scheduledExecutorService;

    private final AtomicInteger serverCounter = new AtomicInteger();
    private final AtomicInteger socketCounter = new AtomicInteger();

    private volatile SocketAddress[] servers = null;
    private volatile UdpChannel[] channels = null;

    private static final SocketAddress[] EMPTY_SERVERS = new SocketAddress[0];

    private static final AtomicReferenceFieldUpdater<NetResolver, SocketAddress[]> SERVERS_UPDATER = AtomicReferenceFieldUpdater.newUpdater(NetResolver.class, SocketAddress[].class, "servers");
    private static final AtomicReferenceFieldUpdater<NetResolver, UdpChannel[]> CHANNELS_UPDATER = AtomicReferenceFieldUpdater.newUpdater(NetResolver.class, UdpChannel[].class, "channels");

    private volatile long retryInterval = 5000L;

    private final Lock writeLock = new ReentrantLock();
    private final Random random = new Random();

    private final IoHandlerFactory<UdpChannel> handlerFactory = new IoHandlerFactory<UdpChannel>() {
        public IoHandler<? super UdpChannel> createHandler() {
            return new Handler();
        }
    };

    public NetResolver(final ScheduledExecutorService scheduler, Collection<SocketAddress> initialServers) {
        scheduledExecutorService = scheduler;
        final SocketAddress[] servers = initialServers.toArray(initialServers.toArray(new SocketAddress[initialServers.size()]));
        for (SocketAddress server : servers) {
            if (server == null) {
                throw new NullPointerException("A server argument was null");
            }
        }
        this.servers = servers;
    }

    public IoHandlerFactory<UdpChannel> getHandlerFactory() {
        return handlerFactory;
    }

    public SocketAddress[] getServers() {
        return servers == null ? EMPTY_SERVERS : servers.clone();
    }

    public long getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(final long retryInterval) {
        this.retryInterval = retryInterval;
    }

    private <T> void add(final AtomicReferenceFieldUpdater<NetResolver, T[]> updater, final T value) {
        if (value == null) {
            throw new NullPointerException("value is null");
        }
        final Lock lock = writeLock;
        lock.lock();
        try {
            final T[] oldVal = updater.get(this);
            final int oldLen = oldVal.length;
            final T[] newVal = Arrays.copyOf(oldVal, oldLen + 1);
            newVal[oldLen] = value;
            updater.set(this, newVal);
        } finally {
            lock.unlock();
        }
    }

    private <T> void remove(final AtomicReferenceFieldUpdater<NetResolver, T[]> updater, final T value) {
        if (value == null) {
            return;
        }
        final Lock lock = writeLock;
        lock.lock();
        try {
            final T[] oldVal = updater.get(this);
            final int oldLen = oldVal.length;
            if (oldLen == 0) {
                return;
            } else if (oldLen == 1 && oldVal[0].equals(value)) {
                updater.set(this, null);
                return;
            } else {
                for (int i = 0; i < oldLen; i++) {
                    if (oldVal[i].equals(value)) {
                        final T[] newVal = Arrays.copyOf(oldVal, oldLen - 1);
                        System.arraycopy(oldVal, 0, newVal, 0, i);
                        System.arraycopy(oldVal, i + 1, newVal, i, oldLen - i - 1);
                        updater.set(this, newVal);
                        return;
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void addServer(SocketAddress address) {
        add(SERVERS_UPDATER, address);
    }

    public void removeServer(SocketAddress address) {
        remove(SERVERS_UPDATER, address);
    }

    protected SocketAddress getDefaultServerAddress() {
        final SocketAddress[] servers = this.servers;
        if (servers == null) {
            throw new IllegalStateException("No servers defined for this resolver!");
        }
        return servers[serverCounter.getAndIncrement() % servers.length];
    }

    public IoFuture<Answer> resolve(final SocketAddress server, final Domain name, final RRClass rrClass, final RRType rrType, final Set<Flag> flags) {
        final UdpChannel[] channels = this.channels;
        if (channels == null) {
            return new FailedIoFuture<Answer>(new IOException("No sockets bound for this resolver!"));
        }
        final int channelLen = channels.length;
        final UdpChannel channel = channels[socketCounter.getAndIncrement() % channelLen];
        final ByteBuffer buffer = ByteBuffer.allocate(512);

        try {
            buffer.flip();
            while (! channel.send(server, buffer)) {
                channel.awaitWritable();
            }
        } catch (IOException e) {
            return new FailedIoFuture<Answer>(e);
        }
        return null;
    }

    private final class FutureAnswer extends AbstractIoFuture<Answer> implements IoFuture<Answer> {
        private final ScheduledFuture<Void> rescheduler;

        private FutureAnswer(final ScheduledFuture<Void> rescheduler) {
            this.rescheduler = rescheduler;
        }

        protected boolean setException(final IOException exception) {
            rescheduler.cancel(false);
            return super.setException(exception);
        }

        protected boolean setResult(final Answer result) {
            rescheduler.cancel(false);
            return super.setResult(result);
        }

        protected boolean finishCancel() {
            rescheduler.cancel(false);
            return super.finishCancel();
        }
    }

    private final class Handler implements IoHandler<UdpChannel> {

        public void handleOpened(final UdpChannel channel) {
            add(CHANNELS_UPDATER, channel);
            channel.resumeReads();
        }

        public void handleClosed(final UdpChannel channel) {
            remove(CHANNELS_UPDATER, channel);
        }

        public void handleReadable(final UdpChannel channel) {
            
        }

        public void handleWritable(final UdpChannel channel) {
        }
    }
}
