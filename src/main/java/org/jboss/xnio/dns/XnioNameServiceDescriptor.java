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

import sun.net.spi.nameservice.NameServiceDescriptor;
import sun.net.spi.nameservice.NameService;

/**
 * A name service descriptor which specifies the "xnio" DNS service.  This class is not intended to be
 * instantiated directly by user code.
 *
 * @apiviz.exclude
 */
public final class XnioNameServiceDescriptor implements NameServiceDescriptor {

    /**
     * Create a name service for this descriptor.
     *
     * @return the name service
     */
    public NameService createNameService() {
        return new XnioNameService();
    }

    /**
     * Get the provider name.
     *
     * @return the string "xnio"
     */
    public String getProviderName() {
        return "xnio";
    }

    /**
     * Get the provider type.
     *
     * @return the string "dns"
     */
    public String getType() {
        return "dns";
    }
}
