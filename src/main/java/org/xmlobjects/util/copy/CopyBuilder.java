/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
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
    private static final AbstractCloner<?> IDENTITY_CLONER = new IdentityCloner();
    private static final AbstractCloner<?> NULL_CLONER = new NullCloner();

    private final Map<Class<?>, AbstractCloner<?>> cloners = new ConcurrentHashMap<>();
    private final AbstractCloner<?> collectionCloner = new CollectionCloner<>(this);
    private final AbstractCloner<?> mapCloner = new MapCloner<>(this);
    private final AbstractCloner<?> arrayCloner = new ArrayCloner(this);

    private volatile boolean failOnError = true;

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
        for (Class<?> type : types) {
            cloners.put(type, IDENTITY_CLONER);
        }

        return this;
    }

    public CopyBuilder registerNullCopy(Class<?>... types) {
        for (Class<?> type : types) {
            cloners.put(type, NULL_CLONER);
        }

        return this;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public CopyBuilder failOnError(boolean failOnError) {
        this.failOnError = failOnError;
        return this;
    }

    CopyContext getContext() {
        return contexts.get();
    }

    @SuppressWarnings("unchecked")
    private <T> T copy(T src, T dest, Class<T> template, boolean shallowCopy) {
        if (src == null || src == dest) {
            return dest;
        }

        CopyContext context = contexts.get();
        boolean isInitial = context.isInitial();

        T clone = (T) context.getClone(src);
        try {
            if (clone == null) {
                if (shallowCopy
                        && isInitial
                        && dest == null
                        && src instanceof Copyable copyable) {
                    clone = (T) copyable.shallowCopy(this, context);
                } else {
                    if (template == null || src.getClass().isArray()) {
                        template = (Class<T>) src.getClass();
                    }

                    // avoid copying parents not belonging to the hierarchy of the initial source object
                    if (src instanceof Child child) {
                        Child parent = child.getParent();
                        if (parent != null) {
                            context.addCloneIfAbsent(parent, parent);
                        }
                    }

                    AbstractCloner<T> cloner = (AbstractCloner<T>) findCloner(template);
                    if (cloner != IDENTITY_CLONER && cloner != NULL_CLONER) {
                        if (dest == null) {
                            dest = cloner.newInstance(src, shallowCopy);
                        }

                        context.addClone(src, dest);
                    }

                    clone = cloner.copy(src, dest, shallowCopy);
                }
            } else if (context.isNullClone(clone)) {
                clone = null;
            }
        } catch (Exception e) {
            if (failOnError) {
                throw e instanceof CopyException copyException ?
                        copyException :
                        new CopyException("Failed to copy " + src + ".", e);
            }
        } finally {
            if (isInitial) {
                context.clear();
                contexts.remove();

                // unset parent on clone
                if (clone instanceof Child child) {
                    child.setParent(null);
                }
            }
        }

        return clone;
    }

    private AbstractCloner<?> findCloner(Class<?> type) {
        return cloners.computeIfAbsent(type, this::createCloner);
    }

    private AbstractCloner<?> createCloner(Class<?> type) {
        if (Enum.class.isAssignableFrom(type)) {
            return IDENTITY_CLONER;
        } else if (Collection.class.isAssignableFrom(type)) {
            return collectionCloner;
        } else if (Map.class.isAssignableFrom(type)) {
            return mapCloner;
        } else if (type.isArray()) {
            return arrayCloner;
        }

        return new ObjectCloner<>(type, this);
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
