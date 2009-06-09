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
import java.util.Collections;

/**
 * A query answer.
 */
public final class Answer {
    private final Domain queryDomain;
    private final RRClass queryRrClass;
    private final RRType queryRrType;
    private final ResultCode resultCode;
    private final List<Record> answerRecords;
    private final List<Record> authorityRecords;
    private final List<Record> additionalRecords;

    private static List<Record> emptyList() {
        return Collections.emptyList();
    }

    /**
     * Construct a new instance with empty result record lists.
     *
     * @param queryDomain the query domain
     * @param queryRrClass the query class
     * @param queryRrType the query type
     */
    public Answer(final Domain queryDomain, final RRClass queryRrClass, final RRType queryRrType) {
        this(queryDomain, queryRrClass, queryRrType, ResultCode.NOERROR, emptyList(), emptyList(), emptyList());
    }

    /**
     * Construct a new instance with empty result record lists.
     *
     * @param queryDomain the query domain
     * @param queryRrClass the query class
     * @param queryRrType the query type
     * @param resultCode the result code
     */
    public Answer(final Domain queryDomain, final RRClass queryRrClass, final RRType queryRrType, final ResultCode resultCode) {
        this(queryDomain, queryRrClass, queryRrType, resultCode, emptyList(), emptyList(), emptyList());
    }

    /**
     * Construct a new instance.
     *
     * @param queryDomain the query domain
     * @param queryRrClass the query class
     * @param queryRrType the query type
     * @param resultCode the result code
     * @param answerRecords the answer record list (should be immutable)
     */
    public Answer(final Domain queryDomain, final RRClass queryRrClass, final RRType queryRrType, final ResultCode resultCode, final List<Record> answerRecords) {
        this(queryDomain, queryRrClass, queryRrType, resultCode, answerRecords, emptyList(), emptyList());
    }

    /**
     * Construct a new instance.
     *
     * @param queryDomain the query domain
     * @param queryRrClass the query class
     * @param queryRrType the query type
     * @param resultCode the result code
     * @param answerRecords the answer record list (should be immutable)
     * @param authorityRecords the authority record list (should be immutable)
     * @param additionalRecords the additional record list (should be immutable)
     */
    public Answer(final Domain queryDomain, final RRClass queryRrClass, final RRType queryRrType, final ResultCode resultCode, final List<Record> answerRecords, final List<Record> authorityRecords, final List<Record> additionalRecords) {
        this.queryDomain = queryDomain;
        this.queryRrClass = queryRrClass;
        this.queryRrType = queryRrType;
        this.resultCode = resultCode;
        this.answerRecords = answerRecords;
        this.authorityRecords = authorityRecords;
        this.additionalRecords = additionalRecords;
    }

    /**
     * Get the query domain.
     *
     * @return the query domain
     */
    public Domain getQueryDomain() {
        return queryDomain;
    }

    /**
     * Get the query class.
     *
     * @return the query class
     */
    public RRClass getQueryRrClass() {
        return queryRrClass;
    }

    /**
     * Get the query type.
     *
     * @return the query type
     */
    public RRType getQueryRrType() {
        return queryRrType;
    }

    /**
     * Get the result code.
     *
     * @return the result code
     */
    public ResultCode getResultCode() {
        return resultCode;
    }

    /**
     * Get the answer records.
     *
     * @return the answer records
     */
    public List<Record> getAnswerRecords() {
        return answerRecords;
    }

    /**
     * Get the authority records.
     *
     * @return the authority records
     */
    public List<Record> getAuthorityRecords() {
        return authorityRecords;
    }

    /**
     * Get the additional records.
     *
     * @return the additional records
     */
    public List<Record> getAdditionalRecords() {
        return additionalRecords;
    }
}
