/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, JBoss Inc., and individual contributors as indicated
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

package org.xnio.dns;

import java.util.List;
import java.util.Collections;
import java.util.Set;
import java.util.ArrayList;
import java.util.EnumSet;

/**
 * A query answer.
 */
public final class Answer {
    private final Domain queryDomain;
    private final RRClass queryRRClass;
    private final RRType queryRRType;
    private final ResultCode resultCode;
    private final List<Record> answerRecords;
    private final List<Record> authorityRecords;
    private final List<Record> additionalRecords;
    private final Set<Flag> flags;

    private static List<Record> emptyList() {
        return Collections.emptyList();
    }

    private static Set<Flag> emptySet() {
        return Collections.emptySet();
    }

    private Answer(final Domain queryDomain, final RRClass queryRRClass, final RRType queryRRType, final ResultCode resultCode, final List<Record> answerRecords, final List<Record> authorityRecords, final List<Record> additionalRecords, final Set<Flag> flags) {
        this.queryDomain = queryDomain;
        this.queryRRClass = queryRRClass;
        this.queryRRType = queryRRType;
        this.resultCode = resultCode;
        this.answerRecords = answerRecords;
        this.authorityRecords = authorityRecords;
        this.additionalRecords = additionalRecords;
        this.flags = flags;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Domain queryDomain;
        private RRClass queryRRClass;
        private RRType queryRRType;
        private ResultCode resultCode;
        private List<Record> answerRecords;
        private List<Record> authorityRecords;
        private List<Record> additionalRecords;
        private Set<Flag> flags;

        public Builder setAnswerRecords(List<Record> list) {
            answerRecords = copy(list);
            return this;
        }

        public Builder setAuthorityRecords(List<Record> list) {
            authorityRecords = copy(list);
            return this;
        }

        public Builder setAdditionalRecords(List<Record> list) {
            additionalRecords = copy(list);
            return this;
        }

        public Builder setHeaderInfo(Answer original) {
            queryDomain = original.queryDomain;
            queryRRClass = original.queryRRClass;
            queryRRType = original.queryRRType;
            resultCode = original.resultCode;
            return this;
        }

        public Builder setHeaderInfo(Domain queryDomain, RRClass queryRRClass, RRType queryRRType, ResultCode resultCode) {
            this.queryDomain = queryDomain;
            this.queryRRClass = queryRRClass;
            this.queryRRType = queryRRType;
            this.resultCode = resultCode;
            return this;
        }

        public Builder setAll(Answer original) {
            setHeaderInfo(original);
            setAnswerRecords(original.answerRecords);
            setAuthorityRecords(original.authorityRecords);
            setAdditionalRecords(original.additionalRecords);
            return this;
        }

        private static List<Record> copy(List<Record> orig) {
            if (orig.isEmpty()) {
                return null;
            }
            final ArrayList<Record> list = new ArrayList<Record>(orig.size());
            for (Record record : orig) {
                if (record == null) {
                    throw new IllegalArgumentException("Null record in original list");
                }
                list.add(record);
            }
            return list;
        }

        public Builder setQueryDomain(Domain queryDomain) {
            this.queryDomain = queryDomain;
            return this;
        }

        public Builder setQueryRRClass(RRClass rrClass) {
            queryRRClass = rrClass;
            return this;
        }

        public Builder setQueryRRType(RRType rrType) {
            queryRRType = rrType;
            return this;
        }

        public Builder setResultCode(ResultCode resultCode) {
            this.resultCode = resultCode;
            return this;
        }

        public Builder addAnswerRecord(Record record) {
            if (answerRecords == null) {
                answerRecords = new ArrayList<Record>();
            }
            answerRecords.add(record);
            return this;
        }

        public Builder addAuthorityRecord(Record record) {
            if (authorityRecords == null) {
                authorityRecords = new ArrayList<Record>();
            }
            authorityRecords.add(record);
            return this;
        }

        public Builder addAdditionalRecord(Record record) {
            if (additionalRecords == null) {
                additionalRecords = new ArrayList<Record>();
            }
            additionalRecords.add(record);
            return this;
        }

        public Builder addFlag(Flag flag) {
            if (flags == null) {
                flags = EnumSet.of(flag);
            } else {
                flags.add(flag);
            }
            return this;
        }

        public Answer create() {
            return new Answer(
                    queryDomain,
                    queryRRClass,
                    queryRRType, 
                    resultCode, 
                    answerRecords == null ? emptyList() : Collections.unmodifiableList(answerRecords),
                    authorityRecords == null ? emptyList() : Collections.unmodifiableList(authorityRecords),
                    additionalRecords == null ? emptyList() : Collections.unmodifiableList(additionalRecords),
                    flags == null ? emptySet() : Collections.unmodifiableSet(flags)
            );
        }
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
    public RRClass getQueryRRClass() {
        return queryRRClass;
    }

    /**
     * Get the query type.
     *
     * @return the query type
     */
    public RRType getQueryRRType() {
        return queryRRType;
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

    /**
     * Get the answer flags.
     *
     * @return the answer flag set
     */
    public Set<Flag> getFlags() {
        return flags;
    }

    public enum Flag {
        AUTHORATIVE,
        TRUNCATED,
        RECURSION_DESIRED,
        RECURSION_AVAILABLE,
    }
}
