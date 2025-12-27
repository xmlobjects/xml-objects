/*
 * xml-objects - A simple and lightweight XML-to-object mapping library
 * https://github.com/xmlobjects
 *
 * Copyright 2019-2025 Claus Nagel <claus.nagel@gmail.com>
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

package org.xmlobjects.util.copy;

import org.xmlobjects.model.Child;
import org.xmlobjects.model.ChildList;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class CopyBuilder {
    private static final ThreadLocal<CopyContext> contexts = ThreadLocal.withInitial(CopyContext::new);
    private static final AbstractCloner<Object> IDENTITY_CLONER = new IdentityCloner();
    private static final AbstractCloner<Object> NULL_CLONER = new NullCloner();
    private static final Object NULL = new Object();

    private final Map<Class<?>, AbstractCloner<?>> cloners = new ConcurrentHashMap<>();
    private final Set<Class<?>> immutables = ConcurrentHashMap.newKeySet();
    private final Set<Class<?>> nulls = ConcurrentHashMap.newKeySet();
    private final AbstractCloner<Collection<?>> COLLECTION_CLONER = new CollectionCloner<>(this);
    private final AbstractCloner<Map<?, ?>> MAP_CLONER = new MapCloner<>(this);
    private final AbstractCloner<Object[]> ARRAY_CLONER = new ArrayCloner(this);

    private volatile boolean failOnError;

    private static class CopyContext {
        private final Map<Object, Object> clones = new IdentityHashMap<>();
        private boolean isCopying;
    }

    public CopyBuilder() {
        registerKnownCloners();
    }

    public <S> S shallowCopy(S src) {
        return copy(src, null, null, true);
    }

    public <S, D extends S> D shallowCopy(S src, D dest) {
        return shallowCopy(src, dest, null);
    }

    @SuppressWarnings("unchecked")
    public <S extends T, D extends T, T> D shallowCopy(S src, D dest, Class<T> template) {
        return (D) copy(src, Objects.requireNonNull(dest, "The target object must not be null."), template, true);
    }

    public <S> S deepCopy(S src) {
        return copy(src, null, null, false);
    }

    public <S, D extends S> D deepCopy(S src, D dest) {
        return deepCopy(src, dest, null);
    }

    @SuppressWarnings("unchecked")
    public <S extends T, D extends T, T> D deepCopy(S src, D dest, Class<T> template) {
        return (D) copy(src, Objects.requireNonNull(dest, "The target object must not be null."), template, false);
    }

    public <T> CopyBuilder registerCloner(Class<T> type, AbstractCloner<T> cloner) {
        cloner.setCopyBuilder(this);
        cloners.put(type, cloner);
        return this;
    }

    public CopyBuilder registerSelfCopy(Class<?>... types) {
        Collections.addAll(immutables, types);
        return this;
    }

    public CopyBuilder registerNullCopy(Class<?>... types) {
        Collections.addAll(nulls, types);
        return this;
    }

    public CopyBuilder failOnError(boolean failOnError) {
        this.failOnError = failOnError;
        return this;
    }

    @SuppressWarnings("unchecked")
    private <T> T copy(T src, T dest, Class<T> template, boolean shallowCopy) {
        if (src == null || src == dest) {
            return dest;
        }

        CopyContext context = contexts.get();
        boolean isInitial = !context.isCopying;
        if (isInitial) {
            context.isCopying = true;
        }

        T clone = (T) context.clones.get(src);
        try {
            if (clone == null) {
                if (shallowCopy
                        && isInitial
                        && dest == null
                        && src instanceof Copyable copyable) {
                    clone = (T) copyable.shallowCopy(this);
                } else {
                    if (template == null) {
                        template = (Class<T>) src.getClass();
                    }

                    // avoid copying parents not belonging to the hierarchy of the initial source object
                    if (src instanceof Child child) {
                        Child parent = child.getParent();
                        if (parent != null) {
                            context.clones.putIfAbsent(parent, parent);
                        }
                    }

                    AbstractCloner<T> cloner = (AbstractCloner<T>) findCloner(template);
                    if (cloner != IDENTITY_CLONER && cloner != NULL_CLONER) {
                        if (dest == null) {
                            dest = cloner.newInstance(src, shallowCopy);
                        }

                        context.clones.put(src, dest != null ? dest : NULL);
                    }

                    clone = cloner.copy(src, dest, shallowCopy);
                }
            } else if (clone == NULL) {
                clone = null;
            }
        } catch (Throwable e) {
            if (failOnError) {
                throw e instanceof CopyException copyException ?
                        copyException :
                        new CopyException("Failed to copy " + src + ".", e);
            }
        } finally {
            if (isInitial) {
                context.isCopying = false;
                context.clones.clear();
                contexts.remove();

                // unset parent on initial source object
                if (clone instanceof Child child) {
                    child.setParent(null);
                }
            }
        }

        return clone;
    }

    private AbstractCloner<?> findCloner(Class<?> type) {
        AbstractCloner<?> cloner = cloners.get(type);
        if (cloner == null) {
            if (immutables.contains(type)) {
                return IDENTITY_CLONER;
            } else if (nulls.contains(type)) {
                return NULL_CLONER;
            } else if (Enum.class.isAssignableFrom(type)) {
                return IDENTITY_CLONER;
            } else if (Collection.class.isAssignableFrom(type)) {
                return COLLECTION_CLONER;
            } else if (Map.class.isAssignableFrom(type)) {
                return MAP_CLONER;
            } else if (type.isArray()) {
                return ARRAY_CLONER;
            }

            cloner = new ObjectCloner<>(type, this);
            cloners.put(type, cloner);
        }

        return cloner;
    }

    private void registerKnownCloners() {
        // identity cloner
        cloners.put(String.class, IDENTITY_CLONER);
        cloners.put(Integer.class, IDENTITY_CLONER);
        cloners.put(Long.class, IDENTITY_CLONER);
        cloners.put(Boolean.class, IDENTITY_CLONER);
        cloners.put(Class.class, IDENTITY_CLONER);
        cloners.put(Float.class, IDENTITY_CLONER);
        cloners.put(Double.class, IDENTITY_CLONER);
        cloners.put(Character.class, IDENTITY_CLONER);
        cloners.put(Byte.class, IDENTITY_CLONER);
        cloners.put(Short.class, IDENTITY_CLONER);
        cloners.put(Void.class, IDENTITY_CLONER);
        cloners.put(BigDecimal.class, IDENTITY_CLONER);
        cloners.put(BigInteger.class, IDENTITY_CLONER);
        cloners.put(URI.class, IDENTITY_CLONER);
        cloners.put(URL.class, IDENTITY_CLONER);
        cloners.put(UUID.class, IDENTITY_CLONER);
        cloners.put(Pattern.class, IDENTITY_CLONER);
        cloners.put(Clock.class, IDENTITY_CLONER);
        cloners.put(Duration.class, IDENTITY_CLONER);
        cloners.put(Instant.class, IDENTITY_CLONER);
        cloners.put(LocalDate.class, IDENTITY_CLONER);
        cloners.put(LocalDateTime.class, IDENTITY_CLONER);
        cloners.put(LocalTime.class, IDENTITY_CLONER);
        cloners.put(MonthDay.class, IDENTITY_CLONER);
        cloners.put(OffsetDateTime.class, IDENTITY_CLONER);
        cloners.put(OffsetTime.class, IDENTITY_CLONER);
        cloners.put(Period.class, IDENTITY_CLONER);
        cloners.put(Year.class, IDENTITY_CLONER);
        cloners.put(YearMonth.class, IDENTITY_CLONER);
        cloners.put(ZonedDateTime.class, IDENTITY_CLONER);
        cloners.put(Collections.EMPTY_LIST.getClass(), IDENTITY_CLONER);
        cloners.put(Collections.EMPTY_MAP.getClass(), IDENTITY_CLONER);
        cloners.put(Collections.EMPTY_SET.getClass(), IDENTITY_CLONER);

        // predefined cloners
        cloners.put(ChildList.class, new ChildListCloner(this));
    }

    private static final class IdentityCloner extends AbstractCloner<Object> {
        @Override
        public Object copy(Object src, Object dest, boolean shallowCopy) {
            return src;
        }
    }

    private static final class NullCloner extends AbstractCloner<Object> {
        @Override
        public Object copy(Object src, Object dest, boolean shallowCopy) {
            return null;
        }
    }
}
