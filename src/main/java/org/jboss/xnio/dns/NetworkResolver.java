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

import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Set;
import org.jboss.xnio.IoFuture;

/**
 * A resolver which can query an external server.  All of the standard resolver methods are implemented by using
 * a default server of some sort.
 */
public interface NetworkResolver extends Resolver {

    IoFuture<Answer> resolve(Domain server, Domain name, RRClass rrClass, RRType rrType, Set<Flag> flags);

    IoFuture<Answer> resolve(InetAddress server, Domain name, RRClass rrClass, RRType rrType, Set<Flag> flags);

    IoFuture<Answer> resolve(SocketAddress server, Domain name, RRClass rrClass, RRType rrType, Set<Flag> flags);
}
