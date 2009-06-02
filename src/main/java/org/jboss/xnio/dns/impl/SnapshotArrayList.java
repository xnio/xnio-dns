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

package org.jboss.xnio.dns.impl;

import java.util.List;
import java.util.RandomAccess;
import java.util.Iterator;
import java.util.Collection;
import java.util.ListIterator;
import java.util.Arrays;
import java.util.AbstractList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

final class SnapshotArrayList<T> implements List<T>, RandomAccess {

    private final Lock writeLock = new ReentrantLock();
    private static final Object[] EMPTY = new Object[0];
    private volatile Object[] entries = EMPTY;

    public int size() {
        return entries.length;
    }

    public boolean isEmpty() {
        return entries.length == 0;
    }

    public boolean contains(final Object o) {
        final Object[] entries = this.entries;
        return indexOf(entries, o) != -1;
    }

    private static int indexOf(final Object[] entries, final Object subject) {
        final int len = entries.length;
        for (int i = 0; i < len; i++) {
            Object entry = entries[i];
            if (subject == null ? entry == null : subject.equals(entry)) {
                return i;
            }
        }
        return -1;
    }

    public Iterator<T> iterator() {
        return snapshot().iterator();
    }

    public Object[] toArray() {
        return entries.clone();
    }

    @SuppressWarnings({ "SuspiciousSystemArraycopy", "unchecked" })
    public <T> T[] toArray(final T[] array) {
        final Object[] entries = this.entries;
        final int len = entries.length;
        if (array.length < len) {
            return (T[]) Arrays.copyOf(entries, len, array.getClass());
        } else {
            System.arraycopy(entries, 0, array, 0, len);
            if (array.length > len) {
                array[len] = null;
            }
            return array;
        }
    }

    public boolean addIfAbsent(final T t) {
        final Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            final Object[] entries = this.entries;
            if (indexOf(entries, t) != -1) {
                return false;
            }
            final int len = entries.length;
            final Object[] newEntries = new Object[len + 1];
            System.arraycopy(entries, 0, newEntries, 0, len);
            newEntries[len] = t;
            this.entries = newEntries;
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    public boolean add(final T t) {
        final Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            final Object[] entries = this.entries;
            final int len = entries.length;
            final Object[] newEntries = new Object[len + 1];
            System.arraycopy(entries, 0, newEntries, 0, len);
            newEntries[len] = t;
            this.entries = newEntries;
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    public List<T> snapshot() {
        final Object[] entries = this.entries;
        return new AbstractList<T>() {
            @SuppressWarnings({ "unchecked" })
            public T get(final int index) {
                return (T) entries[index];
            }

            public int size() {
                return entries.length;
            }
        };
    }

    public boolean remove(final Object o) {
        final Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            final Object[] entries = this.entries;
            final int idx = indexOf(entries, o);
            if (idx == -1) {
                return false;
            }
            final int len = entries.length;
            final Object[] newEntries = new Object[len - 1];
            System.arraycopy(entries, 0, newEntries, 0, idx);
            System.arraycopy(entries, idx + 1, newEntries, idx, len - idx - 1);
            this.entries = newEntries;
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    public boolean containsAll(final Collection<?> collection) {
        final Object[] entries = this.entries;
        for (Object obj : collection) {
            if (indexOf(entries, obj) == -1) {
                return false;
            }
        }
        return true;
    }

    public boolean addAll(final Collection<? extends T> c) {
        return addAll(0, c);
    }

    public boolean addAll(final int index, final Collection<? extends T> c) {
        final Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            final Object[] entries = this.entries;
            final Object[] collectionEntries = c.toArray();
            final int len = entries.length;
            final int clen = collectionEntries.length;
            final Object[] newEntries = new Object[len + clen];
            System.arraycopy(entries, 0, newEntries, 0, index);
            System.arraycopy(collectionEntries, 0, newEntries, index, clen);
            System.arraycopy(entries, index, newEntries, index + clen, len - index);
            this.entries = newEntries;
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    public boolean removeAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        final Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            entries = EMPTY;
        } finally {
            writeLock.unlock();
        }
    }

    @SuppressWarnings({ "unchecked" })
    public T get(final int index) {
        return (T) entries[index];
    }

    @SuppressWarnings({ "unchecked" })
    public T set(final int index, final T element) {
        final Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            final Object[] newEntries = entries.clone();
            try {
                return (T) newEntries[index];
            } finally {
                newEntries[index] = element;
                entries = newEntries;
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void add(final int index, final T element) {
        throw new UnsupportedOperationException();
    }

    public T remove(final int index) {
        throw new UnsupportedOperationException();
    }

    public int indexOf(final Object o) {
        return indexOf(entries, o);
    }

    public int lastIndexOf(final Object o) {
        final int len = entries.length;
        for (int i = len - 1; i >= 0; i--) {
            Object entry = entries[i];
            if (o == null ? entry == null : o.equals(entry)) {
                return i;
            }
        }
        return -1;
    }

    public ListIterator<T> listIterator() {
        return snapshot().listIterator();
    }

    public ListIterator<T> listIterator(final int index) {
        return snapshot().listIterator(index);
    }

    public List<T> subList(final int fromIndex, final int toIndex) {
        return snapshot().subList(fromIndex, toIndex);
    }
}
