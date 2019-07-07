package org.xmlobjects.xml;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

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

    public TextContent format() {
        content = format(content);
        return this;
    }

    public String get() {
        return isPresent() ? content : null;
    }

    public boolean isPresent() {
        return !content.isEmpty();
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

    public void ifPresent(Consumer<String> action) {
        if (isPresent())
            action.accept(content);
    }

    public TextContent filter(Predicate<TextContent> predicate) {
        return predicate.test(this) ? this : empty();
    }

    public Boolean getAsBoolean() {
        if (value instanceof Boolean)
            return (Boolean) value;
        else if ("true".equals(formattedContent())
                || "1".equals(formattedContent()))
            return setValue(Boolean.TRUE);
        else if ("false".equals(formattedContent())
                || "0".equals(formattedContent()))
            return setValue(Boolean.FALSE);
        else
            return setValue(null);
    }

    public boolean isBoolean() {
        return getAsBoolean() != null;
    }

    public void ifBoolean(Consumer<Boolean> action) {
        if (isBoolean())
            action.accept(getAsBoolean());
    }

    public Double getAsDouble() {
        if (value instanceof Double)
            return (Double) value;
        else {
            try {
                return setValue(Double.parseDouble(formattedContent()));
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

    public Integer getAsInteger() {
        if (value instanceof Integer)
            return (Integer) value;
        else {
            try {
                return setValue(Integer.parseInt(formattedContent()));
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

    public Duration getAsDuration() {
        if (value instanceof Duration)
            return (Duration) value;
        else {
            try {
                return setValue(XML_TYPE_FACTORY.newDuration(formattedContent()));
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

    public OffsetDateTime getAsGDay() {
        return getAsOffsetDateTime("gDay");
    }

    public boolean isGDay() {
        return getAsGDay() != null;
    }

    public void isGDay(Consumer<OffsetDateTime> action) {
        if (isGDay())
            action.accept(getAsGDay());
    }

    public OffsetDateTime getAsGMonth() {
        return getAsOffsetDateTime("gMonth");
    }

    public boolean isGMonth() {
        return getAsGMonth() != null;
    }

    public void isGMonth(Consumer<OffsetDateTime> action) {
        if (isGMonth())
            action.accept(getAsGMonth());
    }

    public OffsetDateTime getAsGYear() {
        return getAsOffsetDateTime("gYear");
    }

    public boolean isGYear() {
        return getAsGYear() != null;
    }

    public void isGYear(Consumer<OffsetDateTime> action) {
        if (isGYear())
            action.accept(getAsGYear());
    }

    private OffsetDateTime getAsOffsetDateTime(String localName) {
        if (value instanceof XMLGregorianCalendar && ((XMLGregorianCalendar) value).getXMLSchemaType().getLocalPart().equals(localName))
            return toOffsetDateTime((XMLGregorianCalendar) value);
        else {
            OffsetDateTime dateTime = null;
            try {
                XMLGregorianCalendar calendar = XML_TYPE_FACTORY.newXMLGregorianCalendar(formattedContent());
                setValue(calendar);
                if (calendar.getXMLSchemaType().getLocalPart().equals(localName))
                    dateTime = toOffsetDateTime(calendar);
            } catch (Throwable e) {
                setValue(null);
            }

            return dateTime;
        }
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

    private <T> T setValue(T value) {
        this.value = value;
        return value;
    }

    private String format(String value) {
        return value.trim().replaceAll("\\R", " ");
    }

    private String formattedContent() {
        if (formattedContent == null)
            formattedContent = format(content);

        return formattedContent;
    }

    @Override
    public String toString() {
        return content;
    }
}
