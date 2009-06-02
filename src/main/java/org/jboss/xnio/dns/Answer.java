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

import java.util.List;
import java.util.ArrayList;

public final class Answer {
    private final Domain queryDomain;
    private final RRClass queryRrClass;
    private final RRType queryRrType;
    private final List<Record> answerRecords;
    private final List<Record> authorityRecords;
    private final List<Record> additionalRecords;

    private static List<Record> arrayList() {
        return new ArrayList<Record>();
    }

    public Answer(final Domain queryDomain, final RRClass queryRrClass, final RRType queryRrType) {
        this.queryDomain = queryDomain;
        this.queryRrClass = queryRrClass;
        this.queryRrType = queryRrType;
        answerRecords = arrayList();
        authorityRecords = arrayList();
        additionalRecords = arrayList();
    }

    public Domain getQueryDomain() {
        return queryDomain;
    }

    public RRClass getQueryRrClass() {
        return queryRrClass;
    }

    public RRType getQueryRrType() {
        return queryRrType;
    }

    public List<Record> getAnswerRecords() {
        return answerRecords;
    }

    public List<Record> getAuthorityRecords() {
        return authorityRecords;
    }

    public List<Record> getAdditionalRecords() {
        return additionalRecords;
    }
}
