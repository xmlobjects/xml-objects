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

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class TextContent {
    private static final TextContent ABSENT = new TextContent("");
    private static boolean WITH_TIME_OFFSET = true;
    private static boolean WITH_DATE_OFFSET = false;

    private String content;
    private String trimmedContent;
    private String[] tokenizedContent;
    private Object value;

    private TextContent(String content) {
        this.content = Objects.requireNonNull(content, "Content must not be null.");
    }

    public static TextContent absent() {
        return ABSENT;
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
        return ofOffsetDateTime(content, CalendarFields.DATE_TIME_FIELDS, withOffset);
    }

    public static TextContent ofDateTime(OffsetDateTime content) {
        return ofOffsetDateTime(content, CalendarFields.DATE_TIME_FIELDS, WITH_TIME_OFFSET);
    }

    public static TextContent ofDateTimeList(List<OffsetDateTime> content, boolean withOffset) {
        return ofOffsetDateTimeList(content, CalendarFields.DATE_TIME_FIELDS, withOffset);
    }

    public static TextContent ofDateTimeList(List<OffsetDateTime> content) {
        return ofOffsetDateTimeList(content, CalendarFields.DATE_TIME_FIELDS, WITH_TIME_OFFSET);
    }

    public static TextContent ofTime(OffsetDateTime content, boolean withOffset) {
        return ofOffsetDateTime(content, CalendarFields.TIME_FIELDS, withOffset);
    }

    public static TextContent ofTime(OffsetDateTime content) {
        return ofOffsetDateTime(content, CalendarFields.TIME_FIELDS, WITH_TIME_OFFSET);
    }

    public static TextContent ofTimeList(List<OffsetDateTime> content, boolean withOffset) {
        return ofOffsetDateTimeList(content, CalendarFields.TIME_FIELDS, withOffset);
    }

    public static TextContent ofTimeList(List<OffsetDateTime> content) {
        return ofOffsetDateTimeList(content, CalendarFields.TIME_FIELDS, WITH_TIME_OFFSET);
    }

    public static TextContent ofDate(OffsetDateTime content, boolean withOffset) {
        return ofOffsetDateTime(content, CalendarFields.DATE_FIELDS, withOffset);
    }

    public static TextContent ofDate(OffsetDateTime content) {
        return ofOffsetDateTime(content, CalendarFields.DATE_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofDateList(List<OffsetDateTime> content, boolean withOffset) {
        return ofOffsetDateTimeList(content, CalendarFields.DATE_FIELDS, withOffset);
    }

    public static TextContent ofDateList(List<OffsetDateTime> content) {
        return ofOffsetDateTimeList(content, CalendarFields.DATE_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGYearMonth(OffsetDateTime content, boolean withOffset) {
        return ofOffsetDateTime(content, CalendarFields.GYEAR_MONTH_FIELDS, withOffset);
    }

    public static TextContent ofGYearMonth(OffsetDateTime content) {
        return ofOffsetDateTime(content, CalendarFields.GYEAR_MONTH_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGYearMonthList(List<OffsetDateTime> content, boolean withOffset) {
        return ofOffsetDateTimeList(content, CalendarFields.GYEAR_MONTH_FIELDS, withOffset);
    }

    public static TextContent ofGYearMonthList(List<OffsetDateTime> content) {
        return ofOffsetDateTimeList(content, CalendarFields.GYEAR_MONTH_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGMonthDay(OffsetDateTime content, boolean withOffset) {
        return ofOffsetDateTime(content, CalendarFields.GMONTH_DAY_FIELDS, withOffset);
    }

    public static TextContent ofGMonthDay(OffsetDateTime content) {
        return ofOffsetDateTime(content, CalendarFields.GMONTH_DAY_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGMonthDayList(List<OffsetDateTime> content, boolean withOffset) {
        return ofOffsetDateTimeList(content, CalendarFields.GMONTH_DAY_FIELDS, withOffset);
    }

    public static TextContent ofGMonthDayList(List<OffsetDateTime> content) {
        return ofOffsetDateTimeList(content, CalendarFields.GMONTH_DAY_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGDay(OffsetDateTime content, boolean withOffset) {
        return ofOffsetDateTime(content, CalendarFields.GDAY_FIELDS, withOffset);
    }

    public static TextContent ofGDay(OffsetDateTime content) {
        return ofOffsetDateTime(content, CalendarFields.GDAY_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGDayList(List<OffsetDateTime> content, boolean withOffset) {
        return ofOffsetDateTimeList(content, CalendarFields.GDAY_FIELDS, withOffset);
    }

    public static TextContent ofGDayList(List<OffsetDateTime> content) {
        return ofOffsetDateTimeList(content, CalendarFields.GDAY_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGMonth(OffsetDateTime content, boolean withOffset) {
        return ofOffsetDateTime(content, CalendarFields.GMONTH_FIELDS, withOffset);
    }

    public static TextContent ofGMonth(OffsetDateTime content) {
        return ofOffsetDateTime(content, CalendarFields.GMONTH_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGMonthList(List<OffsetDateTime> content, boolean withOffset) {
        return ofOffsetDateTimeList(content, CalendarFields.GMONTH_FIELDS, withOffset);
    }

    public static TextContent ofGMonthList(List<OffsetDateTime> content) {
        return ofOffsetDateTimeList(content, CalendarFields.GMONTH_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGYear(OffsetDateTime content, boolean withOffset) {
        return ofOffsetDateTime(content, CalendarFields.GYEAR_FIELDS, withOffset);
    }

    public static TextContent ofGYear(OffsetDateTime content) {
        return ofOffsetDateTime(content, CalendarFields.GYEAR_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGYearList(List<OffsetDateTime> content, boolean withOffset) {
        return ofOffsetDateTimeList(content, CalendarFields.GYEAR_FIELDS, withOffset);
    }

    public static TextContent ofGYearList(List<OffsetDateTime> content) {
        return ofOffsetDateTimeList(content, CalendarFields.GYEAR_FIELDS, WITH_DATE_OFFSET);
    }

    public TextContent trim() {
        content = trimContent();
        return this;
    }

    public TextContent normalize() {
        if (isPresent()) {
            content = TextHelper.normalize(content);
        }

        return this;
    }

    public TextContent collapse() {
        if (isPresent()) {
            content = trimmedContent = TextHelper.collapse(content);
        }

        return this;
    }

    public String get() {
        return isPresent() ? content : null;
    }

    public boolean isPresent() {
        return this != ABSENT;
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
        return value instanceof Boolean bool ?
                bool :
                setValue(TextHelper.toBoolean(trimContent()));
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
                Boolean value = TextHelper.toBoolean(token);
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
        } else if (!trimContent().isEmpty()) {
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
        } else if (!trimContent().isEmpty()) {
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
        } else if (!trimContent().isEmpty()) {
            try {
                return setValue(TextHelper.toDuration(trimmedContent));
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
                    list.add(TextHelper.toDuration(token));
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
        return TextHelper.toOffsetDateTime(getAsCalendar("dateTime"));
    }

    public boolean isDateTime() {
        return getAsCalendar("dateTime") != null;
    }

    public void ifDateTime(Consumer<OffsetDateTime> action) {
        OffsetDateTime value = getAsDateTime();
        if (value != null) {
            action.accept(value);
        }
    }

    public List<OffsetDateTime> getAsDateTimeList() {
        return TextHelper.toOffsetDateTimeList(getAsCalendarList("dateTime"));
    }

    public boolean isDateTimeList() {
        return getAsCalendarList("dateTime") != null;
    }

    public void ifDateTimeList(Consumer<List<OffsetDateTime>> action) {
        List<OffsetDateTime> value = getAsDateTimeList();
        if (value != null) {
            action.accept(value);
        }
    }

    public OffsetDateTime getAsTime() {
        return TextHelper.toOffsetDateTime(getAsCalendar("time"));
    }

    public boolean isTime() {
        return getAsCalendar("time") != null;
    }

    public void ifTime(Consumer<OffsetDateTime> action) {
        OffsetDateTime value = getAsTime();
        if (value != null) {
            action.accept(value);
        }
    }

    public List<OffsetDateTime> getAsTimeList() {
        return TextHelper.toOffsetDateTimeList(getAsCalendarList("time"));
    }

    public boolean isTimeList() {
        return getAsCalendarList("time") != null;
    }

    public void ifTimeList(Consumer<List<OffsetDateTime>> action) {
        List<OffsetDateTime> value = getAsTimeList();
        if (value != null) {
            action.accept(value);
        }
    }

    public OffsetDateTime getAsDate() {
        return TextHelper.toOffsetDateTime(getAsCalendar("date"));
    }

    public boolean isDate() {
        return getAsCalendar("date") != null;
    }

    public void ifDate(Consumer<OffsetDateTime> action) {
        OffsetDateTime value = getAsDate();
        if (value != null) {
            action.accept(value);
        }
    }

    public List<OffsetDateTime> getAsDateList() {
        return TextHelper.toOffsetDateTimeList(getAsCalendarList("date"));
    }

    public boolean isDateList() {
        return getAsCalendarList("date") != null;
    }

    public void ifDateList(Consumer<List<OffsetDateTime>> action) {
        List<OffsetDateTime> value = getAsDateList();
        if (value != null) {
            action.accept(value);
        }
    }

    public OffsetDateTime getAsGYearMonth() {
        return TextHelper.toOffsetDateTime(getAsCalendar("gYearMonth"));
    }

    public boolean isGYearMonth() {
        return getAsCalendar("gYearMonth") != null;
    }

    public void ifGYearMonth(Consumer<OffsetDateTime> action) {
        OffsetDateTime value = getAsGYearMonth();
        if (value != null) {
            action.accept(value);
        }
    }

    public List<OffsetDateTime> getAsGYearMonthList() {
        return TextHelper.toOffsetDateTimeList(getAsCalendarList("gYearMonth"));
    }

    public boolean isGYearMonthList() {
        return getAsCalendarList("gYearMonth") != null;
    }

    public void ifGYearMonthList(Consumer<List<OffsetDateTime>> action) {
        List<OffsetDateTime> value = getAsGYearMonthList();
        if (value != null) {
            action.accept(value);
        }
    }

    public OffsetDateTime getAsGMonthDay() {
        return TextHelper.toOffsetDateTime(getAsCalendar("gMonthDay"));
    }

    public boolean isGMonthDay() {
        return getAsCalendar("gMonthDay") != null;
    }

    public void ifGMonthDay(Consumer<OffsetDateTime> action) {
        OffsetDateTime value = getAsGMonthDay();
        if (value != null) {
            action.accept(value);
        }
    }

    public List<OffsetDateTime> getAsGMonthDayList() {
        return TextHelper.toOffsetDateTimeList(getAsCalendarList("gMonthDay"));
    }

    public boolean isGMonthDayList() {
        return getAsCalendarList("gMonthDay") != null;
    }

    public void ifGMonthDayList(Consumer<List<OffsetDateTime>> action) {
        List<OffsetDateTime> value = getAsGMonthDayList();
        if (value != null) {
            action.accept(value);
        }
    }

    public OffsetDateTime getAsGDay() {
        return TextHelper.toOffsetDateTime(getAsCalendar("gDay"));
    }

    public boolean isGDay() {
        return getAsCalendar("gDay") != null;
    }

    public void ifGDay(Consumer<OffsetDateTime> action) {
        OffsetDateTime value = getAsGDay();
        if (value != null) {
            action.accept(value);
        }
    }

    public List<OffsetDateTime> getAsGDayList() {
        return TextHelper.toOffsetDateTimeList(getAsCalendarList("gDay"));
    }

    public boolean isGDayList() {
        return getAsCalendarList("gDay") != null;
    }

    public void ifGDayList(Consumer<List<OffsetDateTime>> action) {
        List<OffsetDateTime> value = getAsGDayList();
        if (value != null) {
            action.accept(value);
        }
    }

    public OffsetDateTime getAsGMonth() {
        return TextHelper.toOffsetDateTime(getAsCalendar("gMonth"));
    }

    public boolean isGMonth() {
        return getAsCalendar("gMonth") != null;
    }

    public void ifGMonth(Consumer<OffsetDateTime> action) {
        OffsetDateTime value = getAsGMonth();
        if (value != null) {
            action.accept(value);
        }
    }

    public List<OffsetDateTime> getAsGMonthList() {
        return TextHelper.toOffsetDateTimeList(getAsCalendarList("gMonth"));
    }

    public boolean isGMonthList() {
        return getAsCalendarList("gMonth") != null;
    }

    public void ifGMonthList(Consumer<List<OffsetDateTime>> action) {
        List<OffsetDateTime> value = getAsGMonthList();
        if (value != null) {
            action.accept(value);
        }
    }

    public OffsetDateTime getAsGYear() {
        return TextHelper.toOffsetDateTime(getAsCalendar("gYear"));
    }

    public boolean isGYear() {
        return getAsCalendar("gYear") != null;
    }

    public void ifGYear(Consumer<OffsetDateTime> action) {
        OffsetDateTime value = getAsGYear();
        if (value != null) {
            action.accept(value);
        }
    }

    public List<OffsetDateTime> getAsGYearList() {
        return TextHelper.toOffsetDateTimeList(getAsCalendarList("gYear"));
    }

    public boolean isGYearList() {
        return getAsCalendarList("gYear") != null;
    }

    public void ifGYearList(Consumer<List<OffsetDateTime>> action) {
        List<OffsetDateTime> value = getAsGYearList();
        if (value != null) {
            action.accept(value);
        }
    }

    private XMLGregorianCalendar getAsCalendar(String localName) {
        if (value instanceof XMLGregorianCalendar calendar
                && calendar.getXMLSchemaType().getLocalPart().equals(localName)) {
            return calendar;
        } else if (!trimContent().isEmpty()) {
            return setValue(TextHelper.toCalendar(trimmedContent, localName));
        } else {
            return setValue(null);
        }
    }

    private List<XMLGregorianCalendar> getAsCalendarList(String localName) {
        List<XMLGregorianCalendar> list = getAsList(XMLGregorianCalendar.class);
        if (list != null && list.get(0).getXMLSchemaType().getLocalPart().equals(localName)) {
            return list;
        } else if (tokenizeContent() != 0) {
            list = new ArrayList<>(tokenizedContent.length);
            for (String token : tokenizedContent) {
                XMLGregorianCalendar value = TextHelper.toCalendar(token, localName);
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

    private static TextContent ofObject(Object content) {
        return content != null ? new TextContent(content.toString()) : ABSENT;
    }

    private static TextContent ofObjectList(List<?> content) {
        if (content != null && !content.isEmpty()) {
            StringBuilder builder = new StringBuilder();
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
            return ABSENT;
        }
    }

    private static TextContent ofOffsetDateTime(OffsetDateTime content, EnumSet<CalendarFields> fields, boolean withOffset) {
        XMLGregorianCalendar calendar = TextHelper.toCalendar(content, fields, withOffset);
        return calendar != null ? new TextContent(calendar.toXMLFormat()) : ABSENT;
    }

    private static TextContent ofOffsetDateTimeList(List<OffsetDateTime> content, EnumSet<CalendarFields> fields, boolean withOffset) {
        if (content != null && !content.isEmpty()) {
            StringBuilder builder = new StringBuilder(content.size());
            boolean first = true;

            for (OffsetDateTime dateTime : content) {
                XMLGregorianCalendar calendar = TextHelper.toCalendar(dateTime, fields, withOffset);
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
            return ABSENT;
        }
    }

    public static void setZoneOffsetProvider(Function<LocalDateTime, ZoneOffset> zoneOffsetProvider) {
        TextHelper.setZoneOffsetProvider(zoneOffsetProvider);
    }

    public static void serializeTimeWithOffset(boolean useTimeOffset) {
        WITH_TIME_OFFSET = useTimeOffset;
    }

    public static void serializeDateWithOffset(boolean useDateOffset) {
        WITH_DATE_OFFSET = useDateOffset;
    }

    private String trimContent() {
        if (trimmedContent == null) {
            trimmedContent = isPresent() ? content.trim() : "";
        }

        return trimmedContent;
    }

    private int tokenizeContent() {
        if (tokenizedContent == null) {
            tokenizedContent = isPresent() ? TextHelper.tokenize(content) : new String[0];
        }

        return tokenizedContent.length;
    }

    @Override
    public String toString() {
        return content;
    }
}
