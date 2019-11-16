package org.xmlobjects.xml;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TextContent {
    private static final DatatypeFactory XML_TYPE_FACTORY;
    private static final TextContent EMPTY = new TextContent("");

    private static Function<LocalDateTime, ZoneOffset> ZONE_OFFSET_PROVIDER = dateTime -> ZoneOffset.systemDefault().getRules().getOffset(dateTime);
    private static boolean WITH_TIME_OFFSET = true;
    private static boolean WITH_DATE_OFFSET = false;

    private enum Fields { YEAR, MONTH, DAY, HOUR, MINUTE, SECOND, NANO, TIMEZONE }
    private static final EnumSet<Fields> DATE_TIME_FIELDS = EnumSet.allOf(Fields.class);
    private static final EnumSet<Fields> TIME_FIELDS = EnumSet.of(Fields.HOUR, Fields.MINUTE, Fields.SECOND, Fields.NANO, Fields.TIMEZONE);
    private static final EnumSet<Fields> DATE_FIELDS = EnumSet.of(Fields.YEAR, Fields.MONTH, Fields.DAY, Fields.TIMEZONE);
    private static final EnumSet<Fields> GYEAR_MONTH_FIELDS = EnumSet.of(Fields.YEAR, Fields.MONTH, Fields.TIMEZONE);
    private static final EnumSet<Fields> GMONTH_DAY_FIELDS = EnumSet.of(Fields.MONTH, Fields.DAY, Fields.TIMEZONE);
    private static final EnumSet<Fields> GDAY_FIELDS = EnumSet.of(Fields.DAY, Fields.TIMEZONE);
    private static final EnumSet<Fields> GMONTH_FIELDS = EnumSet.of(Fields.MONTH, Fields.TIMEZONE);
    private static final EnumSet<Fields> GYEAR_FIELDS = EnumSet.of(Fields.YEAR, Fields.TIMEZONE);

    static {
        try {
            XML_TYPE_FACTORY = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException("Failed to initialize datatype factory.", e);
        }
    }

    private String content;
    private String formattedContent;
    private String[] tokenizedContent;
    private Object value;

    private Function<LocalDateTime, ZoneOffset> zoneOffsetProvider = ZONE_OFFSET_PROVIDER;

    private TextContent(String content) {
        this.content = Objects.requireNonNull(content, "Content must not be null.");
    }

    public static TextContent empty() {
        return EMPTY;
    }

    public static TextContent of(String content) {
        return ofObject(content);
    }

    public static TextContent ofList(List<String> content) {
        return ofObjectList(content);
    }

    public static TextContent ofBoolean(Boolean content) {
        return ofObject(content);
    }

    public static TextContent ofBooleanList(List<Boolean> content) {
        return ofObjectList(content);
    }

    public static TextContent ofDouble(Double content) {
        return ofObject(content);
    }

    public static TextContent ofDoubleList(List<Double> content) {
        return ofObjectList(content);
    }

    public static TextContent ofInteger(Integer content) {
        return ofObject(content);
    }

    public static TextContent ofIntegerList(List<Integer> content) {
        return ofObjectList(content);
    }

    public static TextContent ofDuration(Duration content) {
        return ofObject(content);
    }

    public static TextContent ofDurationList(List<Duration> content) {
        return ofObjectList(content);
    }

    public static TextContent ofDateTime(OffsetDateTime content, boolean withOffset) {
        return ofOffsetDateTime(content, DATE_TIME_FIELDS, withOffset);
    }

    public static TextContent ofDateTime(OffsetDateTime content) {
        return ofOffsetDateTime(content, DATE_TIME_FIELDS, WITH_TIME_OFFSET);
    }

    public static TextContent ofDateTimeList(List<OffsetDateTime> content, boolean withOffset) {
        return ofOffsetDateTimeList(content, DATE_TIME_FIELDS, withOffset);
    }

    public static TextContent ofDateTimeList(List<OffsetDateTime> content) {
        return ofOffsetDateTimeList(content, DATE_TIME_FIELDS, WITH_TIME_OFFSET);
    }

    public static TextContent ofTime(OffsetDateTime content, boolean withOffset) {
        return ofOffsetDateTime(content, TIME_FIELDS, withOffset);
    }

    public static TextContent ofTime(OffsetDateTime content) {
        return ofOffsetDateTime(content, TIME_FIELDS, WITH_TIME_OFFSET);
    }

    public static TextContent ofTimeList(List<OffsetDateTime> content, boolean withOffset) {
        return ofOffsetDateTimeList(content, TIME_FIELDS, withOffset);
    }

    public static TextContent ofTimeList(List<OffsetDateTime> content) {
        return ofOffsetDateTimeList(content, TIME_FIELDS, WITH_TIME_OFFSET);
    }

    public static TextContent ofDate(OffsetDateTime content, boolean withOffset) {
        return ofOffsetDateTime(content, DATE_FIELDS, withOffset);
    }

    public static TextContent ofDate(OffsetDateTime content) {
        return ofOffsetDateTime(content, DATE_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofDateList(List<OffsetDateTime> content, boolean withOffset) {
        return ofOffsetDateTimeList(content, DATE_FIELDS, withOffset);
    }

    public static TextContent ofDateList(List<OffsetDateTime> content) {
        return ofOffsetDateTimeList(content, DATE_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGYearMonth(OffsetDateTime content, boolean withOffset) {
        return ofOffsetDateTime(content, GYEAR_MONTH_FIELDS, withOffset);
    }

    public static TextContent ofGYearMonth(OffsetDateTime content) {
        return ofOffsetDateTime(content, GYEAR_MONTH_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGYearMonthList(List<OffsetDateTime> content, boolean withOffset) {
        return ofOffsetDateTimeList(content, GYEAR_MONTH_FIELDS, withOffset);
    }

    public static TextContent ofGYearMonthList(List<OffsetDateTime> content) {
        return ofOffsetDateTimeList(content, GYEAR_MONTH_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGMonthDay(OffsetDateTime content, boolean withOffset) {
        return ofOffsetDateTime(content, GMONTH_DAY_FIELDS, withOffset);
    }

    public static TextContent ofGMonthDay(OffsetDateTime content) {
        return ofOffsetDateTime(content, GMONTH_DAY_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGMonthDayList(List<OffsetDateTime> content, boolean withOffset) {
        return ofOffsetDateTimeList(content, GMONTH_DAY_FIELDS, withOffset);
    }

    public static TextContent ofGMonthDayList(List<OffsetDateTime> content) {
        return ofOffsetDateTimeList(content, GMONTH_DAY_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGDay(OffsetDateTime content, boolean withOffset) {
        return ofOffsetDateTime(content, GDAY_FIELDS, withOffset);
    }

    public static TextContent ofGDay(OffsetDateTime content) {
        return ofOffsetDateTime(content, GDAY_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGDayList(List<OffsetDateTime> content, boolean withOffset) {
        return ofOffsetDateTimeList(content, GDAY_FIELDS, withOffset);
    }

    public static TextContent ofGDayList(List<OffsetDateTime> content) {
        return ofOffsetDateTimeList(content, GDAY_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGMonth(OffsetDateTime content, boolean withOffset) {
        return ofOffsetDateTime(content, GMONTH_FIELDS, withOffset);
    }

    public static TextContent ofGMonth(OffsetDateTime content) {
        return ofOffsetDateTime(content, GMONTH_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGMonthList(List<OffsetDateTime> content, boolean withOffset) {
        return ofOffsetDateTimeList(content, GMONTH_FIELDS, withOffset);
    }

    public static TextContent ofGMonthList(List<OffsetDateTime> content) {
        return ofOffsetDateTimeList(content, GMONTH_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGYear(OffsetDateTime content, boolean withOffset) {
        return ofOffsetDateTime(content, GYEAR_FIELDS, withOffset);
    }

    public static TextContent ofGYear(OffsetDateTime content) {
        return ofOffsetDateTime(content, GYEAR_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGYearList(List<OffsetDateTime> content, boolean withOffset) {
        return ofOffsetDateTimeList(content, GYEAR_FIELDS, withOffset);
    }

    public static TextContent ofGYearList(List<OffsetDateTime> content) {
        return ofOffsetDateTimeList(content, GYEAR_FIELDS, WITH_DATE_OFFSET);
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public TextContent format() {
        content = formatContent();
        return this;
    }

    public TextContent filter(Predicate<TextContent> predicate) {
        return predicate.test(this) ? this : EMPTY;
    }

    public String get() {
        return isPresent() ? content : null;
    }

    public boolean isPresent() {
        return this != EMPTY;
    }

    public void ifPresent(Consumer<String> action) {
        if (isPresent())
            action.accept(content);
    }

    @SuppressWarnings("unchecked")
    public List<String> getAsList() {
        if (isListOfType(value, String.class))
            return (List<String>) value;
        else if (tokenizeContent() != 0) {
            List<String> strings = new ArrayList<>(tokenizedContent.length);
            Collections.addAll(strings, tokenizedContent);
            return setValue(strings);
        } else
            return setValue(null);
    }

    public boolean isList() {
        return getAsList() != null;
    }

    public void ifList(Consumer<List<String>> action) {
        List<String> value = getAsList();
        if (value != null)
            action.accept(value);
    }

    public Boolean getAsBoolean() {
        return value instanceof Boolean ? (Boolean) value : setValue(toBoolean(formatContent()));
    }

    public boolean isBoolean() {
        return getAsBoolean() != null;
    }

    public void ifBoolean(Consumer<Boolean> action) {
        Boolean value = getAsBoolean();
        if (value != null)
            action.accept(value);
    }

    @SuppressWarnings("unchecked")
    public List<Boolean> getAsBooleanList() {
        if (isListOfType(value, Boolean.class))
            return (List<Boolean>) value;
        else if (tokenizeContent() != 0) {
            List<Boolean> booleans = new ArrayList<>(tokenizedContent.length);
            for (String token : tokenizedContent) {
                Boolean value = toBoolean(token);
                if (value != null)
                    booleans.add(value);
                else
                    return setValue(null);
            }

            return setValue(booleans);
        } else
            return setValue(null);
    }

    public boolean isBooleanList() {
        return getAsBooleanList() != null;
    }

    public void ifBooleanList(Consumer<List<Boolean>> action) {
        List<Boolean> value = getAsBooleanList();
        if (value != null)
            action.accept(value);
    }

    public Double getAsDouble() {
        if (value instanceof Double)
            return (Double) value;
        else {
            try {
                return setValue(Double.parseDouble(formatContent()));
            } catch (NumberFormatException e) {
                return setValue(null);
            }
        }
    }

    public boolean isDouble() {
        return getAsDouble() != null;
    }

    public void ifDouble(Consumer<Double> action) {
        Double value = getAsDouble();
        if (value != null)
            action.accept(value);
    }

    @SuppressWarnings("unchecked")
    public List<Double> getAsDoubleList() {
        if (isListOfType(value, Double.class))
            return (List<Double>) value;
        else if (tokenizeContent() != 0) {
            List<Double> doubles = new ArrayList<>(tokenizedContent.length);
            for (String token : tokenizedContent) {
                try {
                    doubles.add(Double.parseDouble(token));
                } catch (NumberFormatException e) {
                    return setValue(null);
                }
            }

            return setValue(doubles);
        } else
            return setValue(null);
    }

    public boolean isDoubleList() {
        return getAsDoubleList() != null;
    }

    public void ifDoubleList(Consumer<List<Double>> action) {
        List<Double> value = getAsDoubleList();
        if (value != null)
            action.accept(value);
    }

    public Integer getAsInteger() {
        if (value instanceof Integer)
            return (Integer) value;
        else {
            try {
                return setValue(Integer.parseInt(formatContent()));
            } catch (NumberFormatException e) {
                return setValue(null);
            }
        }
    }

    public boolean isInteger() {
        return getAsInteger() != null;
    }

    public void ifInteger(Consumer<Integer> action) {
        Integer value = getAsInteger();
        if (value != null)
            action.accept(value);
    }

    @SuppressWarnings("unchecked")
    public List<Integer> getAsIntegerList() {
        if (isListOfType(value, Integer.class))
            return (List<Integer>) value;
        else if (tokenizeContent() != 0) {
            List<Integer> integers = new ArrayList<>(tokenizedContent.length);
            for (String token : tokenizedContent) {
                try {
                    integers.add(Integer.parseInt(token));
                } catch (NumberFormatException e) {
                    return setValue(null);
                }
            }

            return setValue(integers);
        } else
            return setValue(null);
    }

    public boolean isIntegerList() {
        return getAsIntegerList() != null;
    }

    public void ifIntegerList(Consumer<List<Integer>> action) {
        List<Integer> value = getAsIntegerList();
        if (value != null)
            action.accept(value);
    }

    public Duration getAsDuration() {
        if (value instanceof Duration)
            return (Duration) value;
        else {
            try {
                return setValue(XML_TYPE_FACTORY.newDuration(formatContent()));
            } catch (Throwable e) {
                return setValue(null);
            }
        }
    }

    public boolean isDuration() {
        return getAsDuration() != null;
    }

    public void ifDuration(Consumer<Duration> action) {
        Duration value = getAsDuration();
        if (value != null)
            action.accept(value);
    }

    @SuppressWarnings("unchecked")
    public List<Duration> getAsDurationList() {
        if (isListOfType(value, Duration.class))
            return (List<Duration>) value;
        else if (tokenizeContent() != 0) {
            List<Duration> durations = new ArrayList<>(tokenizedContent.length);
            for (String token : tokenizedContent) {
                try {
                    durations.add(XML_TYPE_FACTORY.newDuration(token));
                } catch (Throwable e) {
                    return setValue(null);
                }
            }

            return setValue(durations);
        } else
            return setValue(null);
    }

    public boolean isDurationList() {
        return getAsDurationList() != null;
    }

    public void ifDurationList(Consumer<List<Duration>> action) {
        List<Duration> value = getAsDurationList();
        if (value != null)
            action.accept(value);
    }

    public OffsetDateTime getAsDateTime() {
        return getAsOffsetDateTime("dateTime");
    }

    public boolean isDateTime() {
        return getAsDateTime() != null;
    }

    public void ifDateTime(Consumer<OffsetDateTime> action) {
        OffsetDateTime value = getAsDateTime();
        if (value != null)
            action.accept(value);
    }

    public List<OffsetDateTime> getAsDateTimeList() {
        return getAsOffsetDateTimeList("dateTime");
    }

    public boolean isDateTimeList() {
        return getAsDateTimeList() != null;
    }

    public void ifDateTimeList(Consumer<List<OffsetDateTime>> action) {
        List<OffsetDateTime> value = getAsDateTimeList();
        if (value != null)
            action.accept(value);
    }

    public OffsetDateTime getAsTime() {
        return getAsOffsetDateTime("time");
    }

    public boolean isTime() {
        return getAsTime() != null;
    }

    public void ifTime(Consumer<OffsetDateTime> action) {
        OffsetDateTime value = getAsTime();
        if (value != null)
            action.accept(value);
    }

    public List<OffsetDateTime> getAsTimeList() {
        return getAsOffsetDateTimeList("time");
    }

    public boolean isTimeList() {
        return getAsTimeList() != null;
    }

    public void ifTimeList(Consumer<List<OffsetDateTime>> action) {
        List<OffsetDateTime> value = getAsTimeList();
        if (value != null)
            action.accept(value);
    }

    public OffsetDateTime getAsDate() {
        return getAsOffsetDateTime("date");
    }

    public boolean isDate() {
        return getAsDate() != null;
    }

    public void ifDate(Consumer<OffsetDateTime> action) {
        OffsetDateTime value = getAsDate();
        if (value != null)
            action.accept(value);
    }

    public List<OffsetDateTime> getAsDateList() {
        return getAsOffsetDateTimeList("date");
    }

    public boolean isDateList() {
        return getAsDateList() != null;
    }

    public void ifDateList(Consumer<List<OffsetDateTime>> action) {
        List<OffsetDateTime> value = getAsDateList();
        if (value != null)
            action.accept(value);
    }

    public OffsetDateTime getAsGYearMonth() {
        return getAsOffsetDateTime("gYearMonth");
    }

    public boolean isGYearMonth() {
        return getAsGYearMonth() != null;
    }

    public void ifGYearMonth(Consumer<OffsetDateTime> action) {
        OffsetDateTime value = getAsGYearMonth();
        if (value != null)
            action.accept(value);
    }

    public List<OffsetDateTime> getAsGYearMonthList() {
        return getAsOffsetDateTimeList("gYearMonth");
    }

    public boolean isGYearMonthList() {
        return getAsGYearMonthList() != null;
    }

    public void ifGYearMonthList(Consumer<List<OffsetDateTime>> action) {
        List<OffsetDateTime> value = getAsGYearMonthList();
        if (value != null)
            action.accept(value);
    }

    public OffsetDateTime getAsGMonthDay() {
        return getAsOffsetDateTime("gMonthDay");
    }

    public boolean isGMonthDay() {
        return getAsGMonthDay() != null;
    }

    public void ifGMonthDay(Consumer<OffsetDateTime> action) {
        OffsetDateTime value = getAsGMonthDay();
        if (value != null)
            action.accept(value);
    }

    public List<OffsetDateTime> getAsGMonthDayList() {
        return getAsOffsetDateTimeList("gMonthDay");
    }

    public boolean isGMonthDayList() {
        return getAsGMonthDayList() != null;
    }

    public void ifGMonthDayList(Consumer<List<OffsetDateTime>> action) {
        List<OffsetDateTime> value = getAsGMonthDayList();
        if (value != null)
            action.accept(value);
    }

    public OffsetDateTime getAsGDay() {
        return getAsOffsetDateTime("gDay");
    }

    public boolean isGDay() {
        return getAsGDay() != null;
    }

    public void ifGDay(Consumer<OffsetDateTime> action) {
        OffsetDateTime value = getAsGDay();
        if (value != null)
            action.accept(value);
    }

    public List<OffsetDateTime> getAsGDayList() {
        return getAsOffsetDateTimeList("gDay");
    }

    public boolean isGDayList() {
        return getAsGDayList() != null;
    }

    public void ifGDayList(Consumer<List<OffsetDateTime>> action) {
        List<OffsetDateTime> value = getAsGDayList();
        if (value != null)
            action.accept(value);
    }

    public OffsetDateTime getAsGMonth() {
        return getAsOffsetDateTime("gMonth");
    }

    public boolean isGMonth() {
        return getAsGMonth() != null;
    }

    public void ifGMonth(Consumer<OffsetDateTime> action) {
        OffsetDateTime value = getAsGMonth();
        if (value != null)
            action.accept(value);
    }

    public List<OffsetDateTime> getAsGMonthList() {
        return getAsOffsetDateTimeList("gMonth");
    }

    public boolean isGMonthList() {
        return getAsGMonthList() != null;
    }

    public void ifGMonthList(Consumer<List<OffsetDateTime>> action) {
        List<OffsetDateTime> value = getAsGMonthList();
        if (value != null)
            action.accept(value);
    }

    public OffsetDateTime getAsGYear() {
        return getAsOffsetDateTime("gYear");
    }

    public boolean isGYear() {
        return getAsGYear() != null;
    }

    public void ifGYear(Consumer<OffsetDateTime> action) {
        OffsetDateTime value = getAsGYear();
        if (value != null)
            action.accept(value);
    }

    public List<OffsetDateTime> getAsGYearList() {
        return getAsOffsetDateTimeList("gYear");
    }

    public boolean isGYearList() {
        return getAsGYearList() != null;
    }

    public void ifGYearList(Consumer<List<OffsetDateTime>> action) {
        List<OffsetDateTime> value = getAsGYearList();
        if (value != null)
            action.accept(value);
    }

    public TextContent withZoneOffsetProvider(Function<LocalDateTime, ZoneOffset> zoneOffsetProvider) {
        if (zoneOffsetProvider != null)
            this.zoneOffsetProvider = zoneOffsetProvider;

        return this;
    }

    private OffsetDateTime getAsOffsetDateTime(String localName) {
        if (value instanceof XMLGregorianCalendar && ((XMLGregorianCalendar) value).getXMLSchemaType().getLocalPart().equals(localName))
            return toOffsetDateTime((XMLGregorianCalendar) value);
        else {
            XMLGregorianCalendar calendar = setValue(toCalendar(formatContent(), localName));
            return calendar != null ? toOffsetDateTime(calendar) : null;
        }
    }

    @SuppressWarnings("unchecked")
    private List<OffsetDateTime> getAsOffsetDateTimeList(String localName) {
        List<XMLGregorianCalendar> result;
        if (value instanceof List
                && ((List<?>) value).stream().allMatch(v -> v instanceof XMLGregorianCalendar
                && ((XMLGregorianCalendar) v).getXMLSchemaType().getLocalPart().equals(localName)))
            result = (List<XMLGregorianCalendar>) value;
        else if (tokenizeContent() != 0) {
            List<XMLGregorianCalendar> calendars = new ArrayList<>(tokenizedContent.length);
            for (String token : tokenizedContent) {
                XMLGregorianCalendar value = toCalendar(token, localName);
                if (value != null)
                    calendars.add(value);
                else
                    return setValue(null);
            }

            result = setValue(calendars);
        } else
            return setValue(null);

        return result.stream().map(this::toOffsetDateTime).collect(Collectors.toList());
    }

    private <T> T setValue(T value) {
        this.value = value;
        return value;
    }

    private boolean isListOfType(Object value, Class<?> type) {
        return value instanceof List && ((List<?>) value).stream().allMatch(type::isInstance);
    }

    private Boolean toBoolean(String value) {
        if (value.isEmpty())
            return null;
        else if ("true".equals(value)
                || "1".equals(value))
            return Boolean.TRUE;
        else if ("false".equals(value)
                || "0".equals(value))
            return Boolean.FALSE;
        else
            return null;
    }

    private XMLGregorianCalendar toCalendar(String value, String localName) {
        try {
            XMLGregorianCalendar calendar = XML_TYPE_FACTORY.newXMLGregorianCalendar(value);
            if (calendar.getXMLSchemaType().getLocalPart().equals(localName))
                return calendar;
        } catch (Throwable e) {
            //
        }

        return null;
    }

    private OffsetDateTime toOffsetDateTime(XMLGregorianCalendar calendar) {
        int day = calendar.getDay();
        int month = calendar.getMonth();
        int year = calendar.getYear();
        int hour = calendar.getHour();
        int minute = calendar.getMinute();
        int second = calendar.getSecond();
        int offset = calendar.getTimezone();

        BigDecimal fractional = calendar.getFractionalSecond();
        int nano = fractional != null ? (int) (fractional.doubleValue() * 1e+9) : DatatypeConstants.FIELD_UNDEFINED;

        LocalDateTime dateTime = LocalDateTime.of(
                year != DatatypeConstants.FIELD_UNDEFINED ? year : 0,
                month != DatatypeConstants.FIELD_UNDEFINED ? month : 1,
                day != DatatypeConstants.FIELD_UNDEFINED ? day : 1,
                hour != DatatypeConstants.FIELD_UNDEFINED ? hour : 0,
                minute != DatatypeConstants.FIELD_UNDEFINED ? minute : 0,
                second != DatatypeConstants.FIELD_UNDEFINED ? second : 0,
                nano != DatatypeConstants.FIELD_UNDEFINED ? nano : 0);

        return OffsetDateTime.of(dateTime, offset != DatatypeConstants.FIELD_UNDEFINED ?
                ZoneOffset.ofTotalSeconds(offset * 60) :
                zoneOffsetProvider.apply(dateTime));
    }

    private static TextContent ofObject(Object content) {
        return content != null ? new TextContent(content.toString()) : EMPTY;
    }

    private static TextContent ofObjectList(List<?> content) {
        return content != null && !content.isEmpty() ? new TextContent(content.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining(" "))) : EMPTY;
    }

    private static TextContent ofOffsetDateTime(OffsetDateTime content, EnumSet<Fields> fields, boolean withOffset) {
        XMLGregorianCalendar calendar = toCalendar(content, fields, withOffset);
        return calendar != null ? new TextContent(calendar.toXMLFormat()) : EMPTY;
    }

    private static TextContent ofOffsetDateTimeList(List<OffsetDateTime> content, EnumSet<Fields> fields, boolean withOffset) {
        return content != null && !content.isEmpty() ? new TextContent(content.stream()
                .map(v -> toCalendar(v, fields, withOffset))
                .filter(Objects::nonNull)
                .map(XMLGregorianCalendar::toXMLFormat)
                .collect(Collectors.joining(" "))) : EMPTY;
    }

    private static XMLGregorianCalendar toCalendar(OffsetDateTime dateTime, EnumSet<Fields> fields, boolean withOffset) {
        XMLGregorianCalendar calendar = null;
        if (dateTime != null) {
            calendar = XML_TYPE_FACTORY.newXMLGregorianCalendar(
                    fields.contains(Fields.YEAR) ? dateTime.getYear() : DatatypeConstants.FIELD_UNDEFINED,
                    fields.contains(Fields.MONTH) ? dateTime.getMonthValue() : DatatypeConstants.FIELD_UNDEFINED,
                    fields.contains(Fields.DAY) ? dateTime.getDayOfMonth() : DatatypeConstants.FIELD_UNDEFINED,
                    fields.contains(Fields.HOUR) ? dateTime.getHour() : DatatypeConstants.FIELD_UNDEFINED,
                    fields.contains(Fields.MINUTE) ? dateTime.getMinute() : DatatypeConstants.FIELD_UNDEFINED,
                    fields.contains(Fields.SECOND) ? dateTime.getSecond() : DatatypeConstants.FIELD_UNDEFINED,
                    DatatypeConstants.FIELD_UNDEFINED,
                    withOffset && fields.contains(Fields.TIMEZONE) ? dateTime.getOffset().getTotalSeconds() / 60 : DatatypeConstants.FIELD_UNDEFINED);

            if (fields.contains(Fields.NANO) && dateTime.getNano() != 0)
                calendar.setFractionalSecond(BigDecimal.valueOf(dateTime.getNano(), 9).stripTrailingZeros());
        }

        return calendar;
    }

    public static void setZoneOffsetProvider(Function<LocalDateTime, ZoneOffset> zoneOffsetProvider) {
        if (zoneOffsetProvider != null)
            ZONE_OFFSET_PROVIDER = zoneOffsetProvider;
    }

    public static void serializeDateWithOffset(boolean useDateOffset) {
        WITH_DATE_OFFSET = useDateOffset;
    }

    private String formatContent() {
        if (formattedContent == null)
            formattedContent = content.trim();

        return formattedContent;
    }

    private int tokenizeContent() {
        int length = content.length();
        String[] tokens = new String[(length / 2) + 1];
        int noOfTokens = 0;
        int current = -1;
        int next;

        do {
            next = nextWhiteSpace(content, current + 1, length);
            if (next != current + 1)
                tokens[noOfTokens++] = content.substring(current + 1, next);

            current = next;
        } while (next != length);

        tokenizedContent = new String[noOfTokens];
        System.arraycopy(tokens, 0, tokenizedContent, 0, noOfTokens);
        return tokenizedContent.length;
    }

    private int nextWhiteSpace(String value, int pos, int length) {
        for (int i = pos; i < length; i++) {
            char ch = value.charAt(i);
            if (ch == ' ' || ch == '\n' || Character.isWhitespace(ch))
                return i;
        }

        return length;
    }

    @Override
    public String toString() {
        return content;
    }
}
