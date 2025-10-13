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

package org.xmlobjects.xml;

import javax.xml.datatype.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class TextContent {
    private static final DatatypeFactory XML_TYPE_FACTORY;
    private static final TextContent EMPTY = new TextContent("");

    private static Function<LocalDateTime, ZoneOffset> ZONE_OFFSET_PROVIDER = dateTime -> ZoneOffset.systemDefault().getRules().getOffset(dateTime);
    private static boolean WITH_TIME_OFFSET = true;
    private static boolean WITH_DATE_OFFSET = false;

    private enum Fields {YEAR, MONTH, DAY, HOUR, MINUTE, SECOND, NANO, TIMEZONE}

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
    private String trimmedContent;
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

    public TextContent trim() {
        content = trimmedContent();
        return this;
    }

    public TextContent normalize() {
        int length = content.length();
        if (length != 0) {
            StringBuilder normalized = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                char ch = content.charAt(i);
                normalized.append(isWhiteSpace(ch) ? ' ' : ch);
            }

            content = normalized.toString();
        }

        return this;
    }

    public TextContent collapse() {
        int length = content.length();
        if (length != 0) {
            char ch = 0;
            int i = 0;

            while (i < length && isWhiteSpace(content.charAt(i))) {
                i++;
            }

            if (i != length) {
                StringBuilder collapsed = new StringBuilder(length - i).append(ch);
                boolean isWhiteSpace = false;

                for (i += 1; i < length; i++) {
                    ch = content.charAt(i);
                    if (isWhiteSpace(ch)) {
                        isWhiteSpace = true;
                    } else {
                        if (isWhiteSpace) {
                            collapsed.append(' ');
                            isWhiteSpace = false;
                        }

                        collapsed.append(ch);
                    }
                }

                content = trimmedContent = collapsed.toString();
            } else {
                content = trimmedContent = "";
            }
        }

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
        if (isPresent()) {
            action.accept(content);
        }
    }

    public List<String> getAsList() {
        List<String> list = getAsList(String.class);
        if (list != null) {
            return list;
        } else if (tokenizeContent() != 0) {
            list = new ArrayList<>(tokenizedContent.length);
            Collections.addAll(list, tokenizedContent);
            return setValue(list);
        } else {
            return setValue(null);
        }
    }

    public boolean isList() {
        return getAsList() != null;
    }

    public void ifList(Consumer<List<String>> action) {
        List<String> value = getAsList();
        if (value != null) {
            action.accept(value);
        }
    }

    public Boolean getAsBoolean() {
        return value instanceof Boolean bool ? bool : setValue(toBoolean(trimmedContent()));
    }

    public boolean isBoolean() {
        return getAsBoolean() != null;
    }

    public void ifBoolean(Consumer<Boolean> action) {
        Boolean value = getAsBoolean();
        if (value != null) {
            action.accept(value);
        }
    }

    public List<Boolean> getAsBooleanList() {
        List<Boolean> list = getAsList(Boolean.class);
        if (list != null) {
            return list;
        } else if (tokenizeContent() != 0) {
            list = new ArrayList<>(tokenizedContent.length);
            for (String token : tokenizedContent) {
                Boolean value = toBoolean(token);
                if (value != null) {
                    list.add(value);
                } else {
                    return setValue(null);
                }
            }

            return setValue(list);
        } else {
            return setValue(null);
        }
    }

    public boolean isBooleanList() {
        return getAsBooleanList() != null;
    }

    public void ifBooleanList(Consumer<List<Boolean>> action) {
        List<Boolean> value = getAsBooleanList();
        if (value != null) {
            action.accept(value);
        }
    }

    public Double getAsDouble() {
        if (value instanceof Double doubleValue) {
            return doubleValue;
        } else if (!isEmpty() && !trimmedContent().isEmpty()) {
            try {
                return setValue(Double.parseDouble(trimmedContent));
            } catch (NumberFormatException e) {
                return setValue(null);
            }
        } else {
            return setValue(null);
        }
    }

    public boolean isDouble() {
        return getAsDouble() != null;
    }

    public void ifDouble(Consumer<Double> action) {
        Double value = getAsDouble();
        if (value != null) {
            action.accept(value);
        }
    }

    public List<Double> getAsDoubleList() {
        List<Double> list = getAsList(Double.class);
        if (list != null) {
            return list;
        } else if (tokenizeContent() != 0) {
            list = new ArrayList<>(tokenizedContent.length);
            for (String token : tokenizedContent) {
                try {
                    list.add(Double.parseDouble(token));
                } catch (NumberFormatException e) {
                    return setValue(null);
                }
            }

            return setValue(list);
        } else {
            return setValue(null);
        }
    }

    public boolean isDoubleList() {
        return getAsDoubleList() != null;
    }

    public void ifDoubleList(Consumer<List<Double>> action) {
        List<Double> value = getAsDoubleList();
        if (value != null) {
            action.accept(value);
        }
    }

    public Integer getAsInteger() {
        if (value instanceof Integer intValue) {
            return intValue;
        } else if (!isEmpty() && !trimmedContent().isEmpty()) {
            try {
                return setValue(Integer.parseInt(trimmedContent));
            } catch (NumberFormatException e) {
                return setValue(null);
            }
        } else {
            return setValue(null);
        }
    }

    public boolean isInteger() {
        return getAsInteger() != null;
    }

    public void ifInteger(Consumer<Integer> action) {
        Integer value = getAsInteger();
        if (value != null) {
            action.accept(value);
        }
    }

    public List<Integer> getAsIntegerList() {
        List<Integer> list = getAsList(Integer.class);
        if (list != null) {
            return list;
        } else if (tokenizeContent() != 0) {
            list = new ArrayList<>(tokenizedContent.length);
            for (String token : tokenizedContent) {
                try {
                    list.add(Integer.parseInt(token));
                } catch (NumberFormatException e) {
                    return setValue(null);
                }
            }

            return setValue(list);
        } else {
            return setValue(null);
        }
    }

    public boolean isIntegerList() {
        return getAsIntegerList() != null;
    }

    public void ifIntegerList(Consumer<List<Integer>> action) {
        List<Integer> value = getAsIntegerList();
        if (value != null) {
            action.accept(value);
        }
    }

    public Duration getAsDuration() {
        if (value instanceof Duration duration) {
            return duration;
        } else if (!isEmpty() && !trimmedContent().isEmpty()) {
            try {
                return setValue(XML_TYPE_FACTORY.newDuration(trimmedContent));
            } catch (Throwable e) {
                return setValue(null);
            }
        } else {
            return setValue(null);
        }
    }

    public boolean isDuration() {
        return getAsDuration() != null;
    }

    public void ifDuration(Consumer<Duration> action) {
        Duration value = getAsDuration();
        if (value != null) {
            action.accept(value);
        }
    }

    public List<Duration> getAsDurationList() {
        List<Duration> list = getAsList(Duration.class);
        if (list != null) {
            return list;
        } else if (tokenizeContent() != 0) {
            list = new ArrayList<>(tokenizedContent.length);
            for (String token : tokenizedContent) {
                try {
                    list.add(XML_TYPE_FACTORY.newDuration(token));
                } catch (Throwable e) {
                    return setValue(null);
                }
            }

            return setValue(list);
        } else {
            return setValue(null);
        }
    }

    public boolean isDurationList() {
        return getAsDurationList() != null;
    }

    public void ifDurationList(Consumer<List<Duration>> action) {
        List<Duration> value = getAsDurationList();
        if (value != null) {
            action.accept(value);
        }
    }

    public OffsetDateTime getAsDateTime() {
        return toOffsetDateTime(getAsCalender("dateTime"));
    }

    public boolean isDateTime() {
        return getAsCalender("dateTime") != null;
    }

    public void ifDateTime(Consumer<OffsetDateTime> action) {
        OffsetDateTime value = getAsDateTime();
        if (value != null) {
            action.accept(value);
        }
    }

    public List<OffsetDateTime> getAsDateTimeList() {
        return toOffsetDateTimeList(getAsCalenderList("dateTime"));
    }

    public boolean isDateTimeList() {
        return getAsCalenderList("dateTime") != null;
    }

    public void ifDateTimeList(Consumer<List<OffsetDateTime>> action) {
        List<OffsetDateTime> value = getAsDateTimeList();
        if (value != null) {
            action.accept(value);
        }
    }

    public OffsetDateTime getAsTime() {
        return toOffsetDateTime(getAsCalender("time"));
    }

    public boolean isTime() {
        return getAsCalender("time") != null;
    }

    public void ifTime(Consumer<OffsetDateTime> action) {
        OffsetDateTime value = getAsTime();
        if (value != null) {
            action.accept(value);
        }
    }

    public List<OffsetDateTime> getAsTimeList() {
        return toOffsetDateTimeList(getAsCalenderList("time"));
    }

    public boolean isTimeList() {
        return getAsCalenderList("time") != null;
    }

    public void ifTimeList(Consumer<List<OffsetDateTime>> action) {
        List<OffsetDateTime> value = getAsTimeList();
        if (value != null) {
            action.accept(value);
        }
    }

    public OffsetDateTime getAsDate() {
        return toOffsetDateTime(getAsCalender("date"));
    }

    public boolean isDate() {
        return getAsCalender("date") != null;
    }

    public void ifDate(Consumer<OffsetDateTime> action) {
        OffsetDateTime value = getAsDate();
        if (value != null) {
            action.accept(value);
        }
    }

    public List<OffsetDateTime> getAsDateList() {
        return toOffsetDateTimeList(getAsCalenderList("date"));
    }

    public boolean isDateList() {
        return getAsCalenderList("date") != null;
    }

    public void ifDateList(Consumer<List<OffsetDateTime>> action) {
        List<OffsetDateTime> value = getAsDateList();
        if (value != null) {
            action.accept(value);
        }
    }

    public OffsetDateTime getAsGYearMonth() {
        return toOffsetDateTime(getAsCalender("gYearMonth"));
    }

    public boolean isGYearMonth() {
        return getAsCalender("gYearMonth") != null;
    }

    public void ifGYearMonth(Consumer<OffsetDateTime> action) {
        OffsetDateTime value = getAsGYearMonth();
        if (value != null) {
            action.accept(value);
        }
    }

    public List<OffsetDateTime> getAsGYearMonthList() {
        return toOffsetDateTimeList(getAsCalenderList("gYearMonth"));
    }

    public boolean isGYearMonthList() {
        return getAsCalenderList("gYearMonth") != null;
    }

    public void ifGYearMonthList(Consumer<List<OffsetDateTime>> action) {
        List<OffsetDateTime> value = getAsGYearMonthList();
        if (value != null) {
            action.accept(value);
        }
    }

    public OffsetDateTime getAsGMonthDay() {
        return toOffsetDateTime(getAsCalender("gMonthDay"));
    }

    public boolean isGMonthDay() {
        return getAsCalender("gMonthDay") != null;
    }

    public void ifGMonthDay(Consumer<OffsetDateTime> action) {
        OffsetDateTime value = getAsGMonthDay();
        if (value != null) {
            action.accept(value);
        }
    }

    public List<OffsetDateTime> getAsGMonthDayList() {
        return toOffsetDateTimeList(getAsCalenderList("gMonthDay"));
    }

    public boolean isGMonthDayList() {
        return getAsCalenderList("gMonthDay") != null;
    }

    public void ifGMonthDayList(Consumer<List<OffsetDateTime>> action) {
        List<OffsetDateTime> value = getAsGMonthDayList();
        if (value != null) {
            action.accept(value);
        }
    }

    public OffsetDateTime getAsGDay() {
        return toOffsetDateTime(getAsCalender("gDay"));
    }

    public boolean isGDay() {
        return getAsCalender("gDay") != null;
    }

    public void ifGDay(Consumer<OffsetDateTime> action) {
        OffsetDateTime value = getAsGDay();
        if (value != null) {
            action.accept(value);
        }
    }

    public List<OffsetDateTime> getAsGDayList() {
        return toOffsetDateTimeList(getAsCalenderList("gDay"));
    }

    public boolean isGDayList() {
        return getAsCalenderList("gDay") != null;
    }

    public void ifGDayList(Consumer<List<OffsetDateTime>> action) {
        List<OffsetDateTime> value = getAsGDayList();
        if (value != null) {
            action.accept(value);
        }
    }

    public OffsetDateTime getAsGMonth() {
        return toOffsetDateTime(getAsCalender("gMonth"));
    }

    public boolean isGMonth() {
        return getAsCalender("gMonth") != null;
    }

    public void ifGMonth(Consumer<OffsetDateTime> action) {
        OffsetDateTime value = getAsGMonth();
        if (value != null) {
            action.accept(value);
        }
    }

    public List<OffsetDateTime> getAsGMonthList() {
        return toOffsetDateTimeList(getAsCalenderList("gMonth"));
    }

    public boolean isGMonthList() {
        return getAsCalenderList("gMonth") != null;
    }

    public void ifGMonthList(Consumer<List<OffsetDateTime>> action) {
        List<OffsetDateTime> value = getAsGMonthList();
        if (value != null) {
            action.accept(value);
        }
    }

    public OffsetDateTime getAsGYear() {
        return toOffsetDateTime(getAsCalender("gYear"));
    }

    public boolean isGYear() {
        return getAsCalender("gYear") != null;
    }

    public void ifGYear(Consumer<OffsetDateTime> action) {
        OffsetDateTime value = getAsGYear();
        if (value != null) {
            action.accept(value);
        }
    }

    public List<OffsetDateTime> getAsGYearList() {
        return toOffsetDateTimeList(getAsCalenderList("gYear"));
    }

    public boolean isGYearList() {
        return getAsCalenderList("gYear") != null;
    }

    public void ifGYearList(Consumer<List<OffsetDateTime>> action) {
        List<OffsetDateTime> value = getAsGYearList();
        if (value != null) {
            action.accept(value);
        }
    }

    public TextContent withZoneOffsetProvider(Function<LocalDateTime, ZoneOffset> zoneOffsetProvider) {
        if (zoneOffsetProvider != null) {
            this.zoneOffsetProvider = zoneOffsetProvider;
        }

        return this;
    }

    private XMLGregorianCalendar getAsCalender(String localName) {
        if (value instanceof XMLGregorianCalendar calendar
                && calendar.getXMLSchemaType().getLocalPart().equals(localName)) {
            return calendar;
        } else if (!isEmpty() && !trimmedContent().isEmpty()) {
            return setValue(toCalendar(trimmedContent(), localName));
        } else {
            return setValue(null);
        }
    }

    private List<XMLGregorianCalendar> getAsCalenderList(String localName) {
        List<XMLGregorianCalendar> list = getAsList(XMLGregorianCalendar.class);
        if (list != null && list.get(0).getXMLSchemaType().getLocalPart().equals(localName)) {
            return list;
        } else if (tokenizeContent() != 0) {
            list = new ArrayList<>(tokenizedContent.length);
            for (String token : tokenizedContent) {
                XMLGregorianCalendar value = toCalendar(token, localName);
                if (value != null) {
                    list.add(value);
                } else {
                    return setValue(null);
                }
            }

            return setValue(list);
        } else {
            return setValue(null);
        }
    }

    private <T> T setValue(T value) {
        this.value = value;
        return value;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getAsList(Class<T> type) {
        return value instanceof List<?> list
                && !list.isEmpty()
                && type.isInstance(list.get(0)) ?
                (List<T>) list :
                null;
    }

    private Boolean toBoolean(String value) {
        return switch (value) {
            case "true", "1" -> Boolean.TRUE;
            case "false", "0" -> Boolean.FALSE;
            default -> null;
        };
    }

    private XMLGregorianCalendar toCalendar(String value, String localName) {
        try {
            XMLGregorianCalendar calendar = XML_TYPE_FACTORY.newXMLGregorianCalendar(value);
            if (calendar.getXMLSchemaType().getLocalPart().equals(localName)) {
                return calendar;
            }
        } catch (Throwable e) {
            //
        }

        return null;
    }

    private OffsetDateTime toOffsetDateTime(XMLGregorianCalendar calendar) {
        if (calendar != null) {
            int day = calendar.getDay();
            int month = calendar.getMonth();
            int year = calendar.getYear();
            int hour = calendar.getHour();
            int minute = calendar.getMinute();
            int second = calendar.getSecond();
            int offset = calendar.getTimezone();

            BigDecimal fractional = calendar.getFractionalSecond();
            int nano = fractional != null ?
                    (int) (fractional.doubleValue() * 1e+9) :
                    DatatypeConstants.FIELD_UNDEFINED;

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
        } else {
            return null;
        }
    }

    private List<OffsetDateTime> toOffsetDateTimeList(List<XMLGregorianCalendar> calenders) {
        if (calenders != null) {
            List<OffsetDateTime> dateTimes = new ArrayList<>(calenders.size());
            calenders.stream()
                    .map(this::toOffsetDateTime)
                    .forEach(dateTimes::add);
            return dateTimes;
        } else {
            return null;
        }
    }

    private static TextContent ofObject(Object content) {
        return content != null ? new TextContent(content.toString()) : EMPTY;
    }

    private static TextContent ofObjectList(List<?> content) {
        if (content != null && !content.isEmpty()) {
            StringBuilder builder = new StringBuilder(content.size());
            boolean first = true;

            for (Object object : content) {
                if (object != null) {
                    if (!first) {
                        builder.append(" ");
                    } else {
                        first = false;
                    }

                    builder.append(object);
                }
            }

            return new TextContent(builder.toString());
        } else {
            return EMPTY;
        }
    }

    private static TextContent ofOffsetDateTime(OffsetDateTime content, EnumSet<Fields> fields, boolean withOffset) {
        XMLGregorianCalendar calendar = toCalendar(content, fields, withOffset);
        return calendar != null ? new TextContent(calendar.toXMLFormat()) : EMPTY;
    }

    private static TextContent ofOffsetDateTimeList(List<OffsetDateTime> content, EnumSet<Fields> fields, boolean withOffset) {
        if (content != null && !content.isEmpty()) {
            StringBuilder builder = new StringBuilder(content.size());
            boolean first = true;

            for (OffsetDateTime dateTime : content) {
                XMLGregorianCalendar calendar = toCalendar(dateTime, fields, withOffset);
                if (calendar != null) {
                    if (!first) {
                        builder.append(" ");
                    } else {
                        first = false;
                    }

                    builder.append(calendar.toXMLFormat());
                }
            }

            return new TextContent(builder.toString());
        } else {
            return EMPTY;
        }
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
                    withOffset && fields.contains(Fields.TIMEZONE) ?
                            dateTime.getOffset().getTotalSeconds() / 60 :
                            DatatypeConstants.FIELD_UNDEFINED);

            if (fields.contains(Fields.NANO) && dateTime.getNano() != 0) {
                calendar.setFractionalSecond(BigDecimal.valueOf(dateTime.getNano(), 9).stripTrailingZeros());
            }
        }

        return calendar;
    }

    public static void setZoneOffsetProvider(Function<LocalDateTime, ZoneOffset> zoneOffsetProvider) {
        if (zoneOffsetProvider != null) {
            ZONE_OFFSET_PROVIDER = zoneOffsetProvider;
        }
    }

    public static void serializeTimeWithOffset(boolean useTimeOffset) {
        WITH_TIME_OFFSET = useTimeOffset;
    }

    public static void serializeDateWithOffset(boolean useDateOffset) {
        WITH_DATE_OFFSET = useDateOffset;
    }

    private String trimmedContent() {
        if (trimmedContent == null) {
            trimmedContent = content.trim();
        }

        return trimmedContent;
    }

    private int tokenizeContent() {
        if (tokenizedContent == null) {
            int length = content.length();
            String[] tokens = new String[(length / 2) + 1];
            int noOfTokens = 0;
            int current = -1;
            int next;

            do {
                next = nextWhiteSpace(content, current + 1, length);
                if (next != current + 1) {
                    tokens[noOfTokens++] = content.substring(current + 1, next);
                }

                current = next;
            } while (next != length);

            tokenizedContent = new String[noOfTokens];
            System.arraycopy(tokens, 0, tokenizedContent, 0, noOfTokens);
        }

        return tokenizedContent.length;
    }

    private int nextWhiteSpace(String value, int pos, int length) {
        for (int i = pos; i < length; i++) {
            if (isWhiteSpace(value.charAt(i))) {
                return i;
            }
        }

        return length;
    }

    private boolean isWhiteSpace(char ch) {
        return ch == ' ' || ch == '\n' || Character.isWhitespace(ch);
    }

    @Override
    public String toString() {
        return content;
    }
}
