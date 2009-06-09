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
import java.util.Set;
import java.util.List;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.Inet6Address;

/**
 * A DNS resolver.
 */
public interface Resolver {

    /**
     * Execute a DNS query.
     *
     * @param name the domain name
     * @param rrClass the resource record class
     * @param rrType the resource record type
     * @param flags the query flags
     * @return the future answer
     */
    IoFuture<Answer> resolve(Domain name, RRClass rrClass, RRType rrType, Set<ResolverFlag> flags);

    /**
     * Execute a DNS query.
     *
     * @param name the domain name
     * @param rrClass the resource record class
     * @param rrType the resource record type
     * @return the future answer
     */
    IoFuture<Answer> resolve(Domain name, RRClass rrClass, RRType rrType);

    /**
     * Execute a DNS query.  A resource record class of {@link org.jboss.xnio.dns.RRClass#IN} is assumed.
     *
     * @param name the domain name
     * @param rrType the resource record type
     * @return the future answer
     */
    IoFuture<Answer> resolve(Domain name, RRType rrType);

    /**
     * Get all the IP addresses (IPv4 or IPv6) for the given domain name.
     *
     * @param name the domain name
     * @return the future list of IP addresses
     */
    IoFuture<List<InetAddress>> resolveAllInet(Domain name);

    /**
     * Get an IP address (IPv4 or IPv6) for the given domain name.
     *
     * @param name the domain name
     * @return the future IP address
     */
    IoFuture<InetAddress> resolveInet(Domain name);

    /**
     * Get all the IPv4 addresses for the given domain name.
     *
     * @param name the domain name
     * @return the future list of IP addresses
     */
    IoFuture<List<Inet4Address>> resolveAllInet4(Domain name);

    /**
     * Get all the IPv4 addresses for the given domain name.
     *
     * @param name the domain name
     * @return the future IP address
     */
    IoFuture<Inet4Address> resolveInet4(Domain name);

    /**
     * Get all the IPv6 addresses for the given domain name.
     *
     * @param name the domain name
     * @return the future list of IP addresses
     */
    IoFuture<List<Inet6Address>> resolveAllInet6(Domain name);

    /**
     * Get all the IPv6 addresses for the given domain name.
     *
     * @param name the domain name
     * @return the future IP address
     */
    IoFuture<Inet6Address> resolveInet6(Domain name);

    /**
     * Perform a reverse lookup of an IP address.
     *
     * @param address the IP address (IPv4 or IPv6)
     * @return the future domain name
     */
    IoFuture<Domain> resolveReverse(InetAddress address);

    /**
     * Perform a text-record lookup of a domain name.
     *
     * @param name the domain name
     * @return the future list of text record data
     */
    IoFuture<List<String>> resolveText(Domain name);
}
