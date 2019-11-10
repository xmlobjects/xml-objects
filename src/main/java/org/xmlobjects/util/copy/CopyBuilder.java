package org.xmlobjects.util.copy;

import org.xmlobjects.model.Child;
import org.xmlobjects.model.ChildList;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class CopyBuilder {
    private final Map<Class<?>, Cloner> cloners = new IdentityHashMap<>();
    private final Map<Object, Object> clones = new IdentityHashMap<>();
    private final Set<Class<?>> immutables = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Class<?>> nulls = Collections.newSetFromMap(new IdentityHashMap<>());

    private final Cloner<Object> IDENTITY_CLONER = (src, dest, clones, shallowCopy, builder) -> src;
    private final Cloner<Object> NULL_CLONER = (src, dest, clones, shallowCopy, builder) -> null;
    private final Cloner<Collection> COLLECTION_CLONER = new CollectionCloner<>();
    private final Cloner<Map> MAP_CLONER = new MapCloner<>();
    private final Cloner<Object[]> ARRAY_CLONER = new ArrayCloner();

    private boolean isCloning;
    private boolean failOnError;

    public CopyBuilder() {
        registerKnownCloners();
    }

    public <T> T shallowCopy(T src) {
        return copy(src, null, true);
    }

    public <T> T shallowCopy(T src, T dest) {
        return copy(src, dest, true);
    }

    public <T> T deepCopy(T src) {
        return copy(src, null, false);
    }

    public <T> T deepCopy(T src, T dest) {
        return copy(src, dest, false);
    }

    public <T> CopyBuilder registerCloner(Class<T> type, Cloner<T> cloner) {
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

    public <T> CopyBuilder withClone(T src, Supplier<T> supplier) {
        if (src != null) {
            T clone = supplier.get();
            if (clone != null && !src.getClass().isAssignableFrom(clone.getClass()) && failOnError)
                throw new CopyException("Type mismatch between object '" + src + "' and clone '" + clone + "'.");

            clones.put(src, clone);
        }

        return this;
    }

    public <T> CopyBuilder withSelfCopy(T src) {
        if (src != null)
            clones.put(src, src);

        return this;
    }

    public CopyBuilder failOnError(boolean failOnError) {
        this.failOnError = failOnError;
        return this;
    }

    @SuppressWarnings("unchecked")
    private <T> T copy(T src, T dest, boolean shallowCopy) {
        if (src == null || src == dest)
            return src;

        boolean isInitial = !isCloning;
        if (isInitial) {
            isCloning = true;
            if (src instanceof Child) {
                // avoid copying the parent of the initial object
                Child parent = ((Child) src).getParent();
                if (parent != null)
                    clones.put(parent, parent);
            }
        }

        T clone = (T) clones.get(src);
        if (clone == null) {
            try {
                Cloner<T> cloner = (Cloner<T>) findCloner(src.getClass());
                clone = cloner.copy(src, dest, clones, shallowCopy, this);
            } catch (Throwable e) {
                if (failOnError)
                    throw e instanceof CopyException ? (CopyException) e : new CopyException("Failed to copy " + src + ".", e);
            }
        }

        if (isInitial) {
            isCloning = false;
            clones.clear();
            if (clone instanceof Child)
                ((Child) clone).setParent(null);
        }

        return clone;
    }

    private Cloner findCloner(Class<?> type) {
        Cloner cloner = cloners.get(type);
        if (cloner == null) {
            if (immutables.contains(type))
                return IDENTITY_CLONER;
            else if (nulls.contains(type))
                return NULL_CLONER;
            else if (Enum.class.isAssignableFrom(type))
                return IDENTITY_CLONER;
            else if (Collection.class.isAssignableFrom(type))
                return COLLECTION_CLONER;
            else if (Map.class.isAssignableFrom(type))
                return MAP_CLONER;
            else if (type.isArray())
                return ARRAY_CLONER;

            cloner = new ObjectCloner<>(type);
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

        // predefined cloners
        cloners.put(ChildList.class, new ChildListCloner());
    }
}
