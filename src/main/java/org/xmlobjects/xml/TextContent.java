package org.xmlobjects.xml;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TextContent {
    public static final DatatypeFactory XML_TYPE_FACTORY;
    private static final TextContent EMPTY = new TextContent("");

    static {
        try {
            XML_TYPE_FACTORY = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException("Failed to initialize datatype factory.", e);
        }
    }

    private String content;
    private String formattedContent;
    private Object value;

    private TextContent(String content) {
        this.content = Objects.requireNonNull(content, "Content must not be null.");
    }

    public static TextContent empty() {
        return EMPTY;
    }

    public static TextContent of(String content) {
        return new TextContent(content);
    }

    public static TextContent of(Boolean content) {
        return new TextContent(content != null && content ? "true" : "false");
    }

    public static TextContent of(Double content) {
        return new TextContent(content.toString());
    }

    public static TextContent of(Integer content) {
        return new TextContent(content.toString());
    }

    public static TextContent ofNullable(String content) {
        return content != null ? new TextContent(content) : EMPTY;
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

    public TextContent format() {
        content = formatContent();
        return this;
    }

    public TextContent filter(Predicate<TextContent> predicate) {
        return predicate.test(this) ? this : empty();
    }

    public String get() {
        return isPresent() ? content : null;
    }

    public boolean isPresent() {
        return !content.isEmpty();
    }

    public void ifPresent(Consumer<String> action) {
        if (isPresent())
            action.accept(content);
    }

    @SuppressWarnings("unchecked")
    public List<String> getAsList() {
        return (isListOfType(value, String.class)) ?
                (List<String>) value :
                setValue(!formatContent().isEmpty() ? Arrays.asList(tokenizeContent()) : null);
    }

    public boolean isList() {
        return getAsList() != null;
    }

    public void ifList(Consumer<List<String>> action) {
        if (isList())
            action.accept(getAsList());
    }

    public Boolean getAsBoolean() {
        return value instanceof Boolean ? (Boolean) value : setValue(toBoolean(formatContent()));
    }

    public boolean isBoolean() {
        return getAsBoolean() != null;
    }

    public void ifBoolean(Consumer<Boolean> action) {
        if (isBoolean())
            action.accept(getAsBoolean());
    }

    @SuppressWarnings("unchecked")
    public List<Boolean> getAsBooleanList() {
        if (isListOfType(value, Boolean.class))
            return (List<Boolean>) value;
        else {
            String[] tokens = tokenizeContent();
            Boolean[] booleans = new Boolean[tokens.length];
            for (int i = 0; i < tokens.length; i++) {
                booleans[i] = toBoolean(tokens[i]);
                if (booleans[i] == null)
                    return setValue(null);
            }

            return setValue(Arrays.asList(booleans));
        }
    }

    public boolean isBooleanList() {
        return getAsBooleanList() != null;
    }

    public void ifBooleanList(Consumer<List<Boolean>> action) {
        if (isBooleanList())
            action.accept(getAsBooleanList());
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
        if (isDouble())
            action.accept(getAsDouble());
    }

    @SuppressWarnings("unchecked")
    public List<Double> getAsDoubleList() {
        if (isListOfType(value, Double.class))
            return (List<Double>) value;
        else {
            String[] tokens = tokenizeContent();
            Double[] doubles = new Double[tokens.length];
            for (int i = 0; i < tokens.length; i++) {
                try {
                    doubles[i] = Double.parseDouble(tokens[i]);
                } catch (NumberFormatException e) {
                    return setValue(null);
                }
            }

            return setValue(Arrays.asList(doubles));
        }
    }

    public boolean isDoubleList() {
        return getAsDoubleList() != null;
    }

    public void ifDoubleList(Consumer<List<Double>> action) {
        if (isDoubleList())
            action.accept(getAsDoubleList());
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
        if (isInteger())
            action.accept(getAsInteger());
    }

    @SuppressWarnings("unchecked")
    public List<Integer> getAsIntegerList() {
        if (isListOfType(value, Integer.class))
            return (List<Integer>) value;
        else {
            String[] tokens = tokenizeContent();
            Integer[] integers = new Integer[tokens.length];
            for (int i = 0; i < tokens.length; i++) {
                try {
                    integers[i] = Integer.parseInt(tokens[i]);
                } catch (NumberFormatException e) {
                    return setValue(null);
                }
            }

            return setValue(Arrays.asList(integers));
        }
    }

    public boolean isIntegerList() {
        return getAsIntegerList() != null;
    }

    public void ifIntegerList(Consumer<List<Integer>> action) {
        if (isIntegerList())
            action.accept(getAsIntegerList());
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
        if (isDuration())
            action.accept(getAsDuration());
    }

    @SuppressWarnings("unchecked")
    public List<Duration> getAsDurationList() {
        if (isListOfType(value, Duration.class))
            return (List<Duration>) value;
        else {
            String[] tokens = tokenizeContent();
            Duration[] durations = new Duration[tokens.length];
            for (int i = 0; i < tokens.length; i++) {
                try {
                    durations[i] = XML_TYPE_FACTORY.newDuration(tokens[i]);
                } catch (Throwable e) {
                    return setValue(null);
                }
            }

            return setValue(Arrays.asList(durations));
        }
    }

    public boolean isDurationList() {
        return getAsDurationList() != null;
    }

    public void ifDurationList(Consumer<List<Duration>> action) {
        if (isDurationList())
            action.accept(getAsDurationList());
    }

    public OffsetDateTime getAsDateTime() {
        return getAsOffsetDateTime("dateTime");
    }

    public boolean isDateTime() {
        return getAsDateTime() != null;
    }

    public void ifDateTime(Consumer<OffsetDateTime> action) {
        if (isDateTime())
            action.accept(getAsDateTime());
    }

    public List<OffsetDateTime> getAsDateTimeList() {
        return getAsOffsetDateTimeList("dateTime");
    }

    public boolean isDateTimeList() {
        return getAsDateTimeList() != null;
    }

    public void ifDateTimeList(Consumer<List<OffsetDateTime>> action) {
        if (isDateTimeList())
            action.accept(getAsDateTimeList());
    }

    public OffsetDateTime getAsTime() {
        return getAsOffsetDateTime("time");
    }

    public boolean isTime() {
        return getAsTime() != null;
    }

    public void ifTime(Consumer<OffsetDateTime> action) {
        if (isTime())
            action.accept(getAsTime());
    }

    public List<OffsetDateTime> getAsTimeList() {
        return getAsOffsetDateTimeList("time");
    }

    public boolean isTimeList() {
        return getAsTimeList() != null;
    }

    public void ifTimeList(Consumer<List<OffsetDateTime>> action) {
        if (isTimeList())
            action.accept(getAsTimeList());
    }

    public OffsetDateTime getAsDate() {
        return getAsOffsetDateTime("date");
    }

    public boolean isDate() {
        return getAsDate() != null;
    }

    public void ifDate(Consumer<OffsetDateTime> action) {
        if (isDate())
            action.accept(getAsDate());
    }

    public List<OffsetDateTime> getAsDateList() {
        return getAsOffsetDateTimeList("date");
    }

    public boolean isDateList() {
        return getAsDateList() != null;
    }

    public void ifDateList(Consumer<List<OffsetDateTime>> action) {
        if (isDateList())
            action.accept(getAsDateList());
    }

    public OffsetDateTime getAsGYearMonth() {
        return getAsOffsetDateTime("gYearMonth");
    }

    public boolean isGYearMonth() {
        return getAsGYearMonth() != null;
    }

    public void ifGYearMonth(Consumer<OffsetDateTime> action) {
        if (isGYearMonth())
            action.accept(getAsGYearMonth());
    }

    public List<OffsetDateTime> getAsGYearMonthList() {
        return getAsOffsetDateTimeList("gYearMonth");
    }

    public boolean isGYearMonthList() {
        return getAsGYearMonthList() != null;
    }

    public void ifGYearMonthList(Consumer<List<OffsetDateTime>> action) {
        if (isGYearMonthList())
            action.accept(getAsGYearMonthList());
    }

    public OffsetDateTime getAsGMonthDay() {
        return getAsOffsetDateTime("gMonthDay");
    }

    public boolean isGMonthDay() {
        return getAsGMonthDay() != null;
    }

    public void ifGMonthDay(Consumer<OffsetDateTime> action) {
        if (isGMonthDay())
            action.accept(getAsGMonthDay());
    }

    public List<OffsetDateTime> getAsGMonthDayList() {
        return getAsOffsetDateTimeList("gMonthDay");
    }

    public boolean isGMonthDayList() {
        return getAsGMonthDayList() != null;
    }

    public void ifGMonthDayList(Consumer<List<OffsetDateTime>> action) {
        if (isGMonthDayList())
            action.accept(getAsGMonthDayList());
    }

    public OffsetDateTime getAsGDay() {
        return getAsOffsetDateTime("gDay");
    }

    public boolean isGDay() {
        return getAsGDay() != null;
    }

    public void ifGDay(Consumer<OffsetDateTime> action) {
        if (isGDay())
            action.accept(getAsGDay());
    }

    public List<OffsetDateTime> getAsGDayList() {
        return getAsOffsetDateTimeList("gDay");
    }

    public boolean isGDayList() {
        return getAsGDayList() != null;
    }

    public void ifGDayList(Consumer<List<OffsetDateTime>> action) {
        if (isGDayList())
            action.accept(getAsGDayList());
    }

    public OffsetDateTime getAsGMonth() {
        return getAsOffsetDateTime("gMonth");
    }

    public boolean isGMonth() {
        return getAsGMonth() != null;
    }

    public void ifGMonth(Consumer<OffsetDateTime> action) {
        if (isGMonth())
            action.accept(getAsGMonth());
    }

    public List<OffsetDateTime> getAsGMonthList() {
        return getAsOffsetDateTimeList("gMonth");
    }

    public boolean isGMonthList() {
        return getAsGMonthList() != null;
    }

    public void ifGMonthList(Consumer<List<OffsetDateTime>> action) {
        if (isGMonthList())
            action.accept(getAsGMonthList());
    }

    public OffsetDateTime getAsGYear() {
        return getAsOffsetDateTime("gYear");
    }

    public boolean isGYear() {
        return getAsGYear() != null;
    }

    public void ifGYear(Consumer<OffsetDateTime> action) {
        if (isGYear())
            action.accept(getAsGYear());
    }

    public List<OffsetDateTime> getAsGYearList() {
        return getAsOffsetDateTimeList("gYear");
    }

    public boolean isGYearList() {
        return getAsGYearList() != null;
    }

    public void ifGYearList(Consumer<List<OffsetDateTime>> action) {
        if (isGYearList())
            action.accept(getAsGYearList());
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
        else {
            String[] tokens = tokenizeContent();
            XMLGregorianCalendar[] calendars = new XMLGregorianCalendar[tokens.length];
            for (int i = 0; i < tokens.length; i++) {
                calendars[i] = toCalendar(tokens[i], localName);
                if (calendars[i] == null)
                    return setValue(null);
            }

            result = setValue(Arrays.asList(calendars));
        }

        return result.stream().map(this::toOffsetDateTime).collect(Collectors.toList());
    }

    private <T> T setValue(T value) {
        this.value = value;
        return value;
    }

    private String formatContent() {
        if (formattedContent == null)
            formattedContent = content.replaceAll("\\R", " ").trim();

        return formattedContent;
    }

    private String[] tokenizeContent() {
        return formatContent().split("\\s+");
    }

    private boolean isListOfType(Object value, Class<?> type) {
        return value instanceof List && ((List<?>) value).stream().allMatch(type::isInstance);
    }

    private Boolean toBoolean(String value) {
        if ("true".equals(value)
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
        int millisecond = calendar.getMillisecond();
        int offset = calendar.getTimezone();

        return OffsetDateTime.of(
                year != DatatypeConstants.FIELD_UNDEFINED ? year : 0,
                month != DatatypeConstants.FIELD_UNDEFINED ? month : 1,
                day != DatatypeConstants.FIELD_UNDEFINED ? day : 1,
                hour != DatatypeConstants.FIELD_UNDEFINED ? hour : 0,
                minute != DatatypeConstants.FIELD_UNDEFINED ? minute : 0,
                second != DatatypeConstants.FIELD_UNDEFINED ? second : 0,
                millisecond != DatatypeConstants.FIELD_UNDEFINED ? millisecond : 0,
                ZoneOffset.ofHours(offset != DatatypeConstants.FIELD_UNDEFINED ? offset / 60 : 0));
    }

    @Override
    public String toString() {
        return content;
    }
}
