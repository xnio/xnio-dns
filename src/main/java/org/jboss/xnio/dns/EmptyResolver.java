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
import org.jboss.xnio.FinishedIoFuture;
import java.util.Set;

/**
 * A resolver which returns no results.
 */
public final class EmptyResolver extends AbstractResolver {

    private final ResultCode resultCode;

    /**
     * Create a new instance.
     *
     * @param resultCode the result code to return
     */
    public EmptyResolver(final ResultCode resultCode) {
        this.resultCode = resultCode;
    }

    /**
     * Create a new instance with a result code of {@link ResultCode#NOERROR}.
     */
    public EmptyResolver() {
        this(ResultCode.NOERROR);
    }

    /** {@inheritDoc}  This implementation always returns an empty answer. */
    public IoFuture<Answer> resolve(final Domain name, final RRClass rrClass, final RRType rrType, final Set<ResolverFlag> flags) {
        return new FinishedIoFuture<Answer>(Answer.builder().setQueryDomain(name).setQueryRRClass(rrClass).setQueryRRType(rrType).setResultCode(resultCode).create());
    }
}
