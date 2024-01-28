/*
 * xml-objects - A simple and lightweight XML-to-object mapping library
 * https://github.com/xmlobjects
 *
 * Copyright 2019-2024 Claus Nagel <claus.nagel@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xmlobjects.util.xml;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.EmptyStackException;
import java.util.Iterator;

public class ArrayBuffer<T> implements Iterable<T> {
    static final int DEFAULT_BUFFER_SIZE = 512;
    private static final Object[] EMPTY = {};

    private final Class<T> type;
    private final int containerSize;
    private Container<T> current;
    private transient int modCount;

    ArrayBuffer(Class<T> type, int containerSize) {
        if (containerSize <= 0) {
            throw new IllegalArgumentException("Container size must be greater than zero.");
        }

        this.type = type;
        this.containerSize = containerSize;
        current = new Container<>(type, containerSize);
    }

    void push(T item) {
        modCount++;
        if (current.index == current.items.length) {
            current = new Container<>(type, containerSize, current);
        }

        current.items[current.index++] = item;
    }

    T peek() {
        Container<T> container = current.index == 0 ? current.previous : current;
        return container != null ? container.items[container.index - 1] : null;
    }

    T pop() {
        modCount++;
        if (current.index == 0) {
            if (current.previous == null) {
                throw new EmptyStackException();
            }

            current = current.previous;
            current.next = null;
        }

        T item = current.items[--current.index];
        current.items[current.index] = null;

        return item;
    }

    @SuppressWarnings("unchecked")
    public void trimToSize() {
        modCount++;
        if (current.index < current.items.length) {
            current.items = current.index == 0 ?
                    (T[]) EMPTY :
                    Arrays.copyOf(current.items, current.index);
        }
    }

    void clear() {
        modCount++;
        current = new Container<>(type, containerSize);
    }

    boolean isEmpty() {
        return current.index == 0 && current.previous == null;
    }

    ArrayBufferIterator<T> iterator(boolean release) {
        return new ArrayBufferIterator<>(this, release);
    }

    @Override
    public Iterator<T> iterator() {
        return iterator(false);
    }

    private static class Container<T> {
        private T[] items;
        private int index;
        private Container<T> next;
        private Container<T> previous;

        @SuppressWarnings("unchecked")
        Container(Class<T> type, int containerSize) {
            items = (T[]) Array.newInstance(type, containerSize);
        }

        Container(Class<T> type, int containerSize, Container<T> previous) {
            this(type, containerSize);
            this.previous = previous;
            previous.next = this;
        }
    }

    static class ArrayBufferIterator<T> implements Iterator<T> {
        private final ArrayBuffer<T> buffer;
        private final boolean release;
        private final int modCount;
        private Container<T> current;
        private int index;

        ArrayBufferIterator(ArrayBuffer<T> buffer, boolean release) {
            this.buffer = buffer;
            this.release = release;

            modCount = buffer.modCount;
            current = buffer.current;
            while (current.previous != null) {
                current = current.previous;
            }

            if (release) {
                buffer.current = new Container<>(buffer.type, buffer.containerSize);
            }
        }

        @Override
        public boolean hasNext() {
            if (index < current.index) {
                return true;
            } else if (index == current.items.length) {
                return current.next != null && current.next.index > 0;
            } else {
                return false;
            }
        }

        T peek() {
            if (modCount != buffer.modCount) {
                throw new ConcurrentModificationException();
            }

            if (index < current.index) {
                return current.items[index];
            } else if (index == current.items.length
                    && current.next != null
                    && current.next.index > 0) {
                return current.next.items[0];
            }

            return null;
        }

        @Override
        public T next() {
            T item = peek();

            if (index == current.items.length) {
                current = current.next;
                index = 0;
                if (release) {
                    current.previous = null;
                }
            }

            if (release) {
                current.items[index] = null;
            }

            index++;
            return item;
        }
    }
}
