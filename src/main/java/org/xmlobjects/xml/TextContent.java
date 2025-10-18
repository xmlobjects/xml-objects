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
import java.util.function.Supplier;

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
        return ofOffsetDateTime(content, CalendarField.DATE_TIME_FIELDS, withOffset);
    }

    public static TextContent ofDateTime(OffsetDateTime content) {
        return ofOffsetDateTime(content, CalendarField.DATE_TIME_FIELDS, WITH_TIME_OFFSET);
    }

    public static TextContent ofDateTimeList(List<OffsetDateTime> content, boolean withOffset) {
        return ofOffsetDateTimeList(content, CalendarField.DATE_TIME_FIELDS, withOffset);
    }

    public static TextContent ofDateTimeList(List<OffsetDateTime> content) {
        return ofOffsetDateTimeList(content, CalendarField.DATE_TIME_FIELDS, WITH_TIME_OFFSET);
    }

    public static TextContent ofTime(OffsetDateTime content, boolean withOffset) {
        return ofOffsetDateTime(content, CalendarField.TIME_FIELDS, withOffset);
    }

    public static TextContent ofTime(OffsetDateTime content) {
        return ofOffsetDateTime(content, CalendarField.TIME_FIELDS, WITH_TIME_OFFSET);
    }

    public static TextContent ofTimeList(List<OffsetDateTime> content, boolean withOffset) {
        return ofOffsetDateTimeList(content, CalendarField.TIME_FIELDS, withOffset);
    }

    public static TextContent ofTimeList(List<OffsetDateTime> content) {
        return ofOffsetDateTimeList(content, CalendarField.TIME_FIELDS, WITH_TIME_OFFSET);
    }

    public static TextContent ofDate(OffsetDateTime content, boolean withOffset) {
        return ofOffsetDateTime(content, CalendarField.DATE_FIELDS, withOffset);
    }

    public static TextContent ofDate(OffsetDateTime content) {
        return ofOffsetDateTime(content, CalendarField.DATE_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofDateList(List<OffsetDateTime> content, boolean withOffset) {
        return ofOffsetDateTimeList(content, CalendarField.DATE_FIELDS, withOffset);
    }

    public static TextContent ofDateList(List<OffsetDateTime> content) {
        return ofOffsetDateTimeList(content, CalendarField.DATE_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGYearMonth(OffsetDateTime content, boolean withOffset) {
        return ofOffsetDateTime(content, CalendarField.GYEAR_MONTH_FIELDS, withOffset);
    }

    public static TextContent ofGYearMonth(OffsetDateTime content) {
        return ofOffsetDateTime(content, CalendarField.GYEAR_MONTH_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGYearMonthList(List<OffsetDateTime> content, boolean withOffset) {
        return ofOffsetDateTimeList(content, CalendarField.GYEAR_MONTH_FIELDS, withOffset);
    }

    public static TextContent ofGYearMonthList(List<OffsetDateTime> content) {
        return ofOffsetDateTimeList(content, CalendarField.GYEAR_MONTH_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGMonthDay(OffsetDateTime content, boolean withOffset) {
        return ofOffsetDateTime(content, CalendarField.GMONTH_DAY_FIELDS, withOffset);
    }

    public static TextContent ofGMonthDay(OffsetDateTime content) {
        return ofOffsetDateTime(content, CalendarField.GMONTH_DAY_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGMonthDayList(List<OffsetDateTime> content, boolean withOffset) {
        return ofOffsetDateTimeList(content, CalendarField.GMONTH_DAY_FIELDS, withOffset);
    }

    public static TextContent ofGMonthDayList(List<OffsetDateTime> content) {
        return ofOffsetDateTimeList(content, CalendarField.GMONTH_DAY_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGDay(OffsetDateTime content, boolean withOffset) {
        return ofOffsetDateTime(content, CalendarField.GDAY_FIELDS, withOffset);
    }

    public static TextContent ofGDay(OffsetDateTime content) {
        return ofOffsetDateTime(content, CalendarField.GDAY_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGDayList(List<OffsetDateTime> content, boolean withOffset) {
        return ofOffsetDateTimeList(content, CalendarField.GDAY_FIELDS, withOffset);
    }

    public static TextContent ofGDayList(List<OffsetDateTime> content) {
        return ofOffsetDateTimeList(content, CalendarField.GDAY_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGMonth(OffsetDateTime content, boolean withOffset) {
        return ofOffsetDateTime(content, CalendarField.GMONTH_FIELDS, withOffset);
    }

    public static TextContent ofGMonth(OffsetDateTime content) {
        return ofOffsetDateTime(content, CalendarField.GMONTH_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGMonthList(List<OffsetDateTime> content, boolean withOffset) {
        return ofOffsetDateTimeList(content, CalendarField.GMONTH_FIELDS, withOffset);
    }

    public static TextContent ofGMonthList(List<OffsetDateTime> content) {
        return ofOffsetDateTimeList(content, CalendarField.GMONTH_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGYear(OffsetDateTime content, boolean withOffset) {
        return ofOffsetDateTime(content, CalendarField.GYEAR_FIELDS, withOffset);
    }

    public static TextContent ofGYear(OffsetDateTime content) {
        return ofOffsetDateTime(content, CalendarField.GYEAR_FIELDS, WITH_DATE_OFFSET);
    }

    public static TextContent ofGYearList(List<OffsetDateTime> content, boolean withOffset) {
        return ofOffsetDateTimeList(content, CalendarField.GYEAR_FIELDS, withOffset);
    }

    public static TextContent ofGYearList(List<OffsetDateTime> content) {
        return ofOffsetDateTimeList(content, CalendarField.GYEAR_FIELDS, WITH_DATE_OFFSET);
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

    public boolean isPresent() {
        return this != ABSENT;
    }

    public void ifPresent(Consumer<String> action) {
        if (isPresent()) {
            action.accept(content);
        }
    }

    public String get() {
        return isPresent() ? content : null;
    }

    public String getOrElse(String defaultValue) {
        return getOrElse(get(), defaultValue);
    }

    public String getOrElseGet(Supplier<String> defaultValue) {
        return getOrElseGet(get(), defaultValue);
    }

    public List<String> getAsList() {
        List<String> list = getAsList(String.class);
        if (list != null) {
            return list;
        }

        if (tokenizeContent() == 0) {
            return null;
        }

        list = new ArrayList<>(tokenizedContent.length);
        Collections.addAll(list, tokenizedContent);
        return setValue(list);
    }

    public List<String> getAsListOrElse(List<String> defaultValue) {
        return getOrElse(getAsList(), defaultValue);
    }

    public List<String> getAsListOrElseGet(Supplier<List<String>> defaultValue) {
        return getOrElseGet(getAsList(), defaultValue);
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
        return parseValue(TextHelper::toBoolean, Boolean.class);
    }

    public Boolean getAsBooleanOrElse(Boolean defaultValue) {
        return getOrElse(getAsBoolean(), defaultValue);
    }

    public Boolean getAsBooleanOrElseGet(Supplier<Boolean> defaultValue) {
        return getOrElseGet(getAsBoolean(), defaultValue);
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
        return parseValueList(TextHelper::toBoolean, Boolean.class);
    }

    public List<Boolean> getAsBooleanListOrElse(List<Boolean> defaultValue) {
        return getOrElse(getAsBooleanList(), defaultValue);
    }

    public List<Boolean> getAsBooleanListOrElseGet(Supplier<List<Boolean>> defaultValue) {
        return getOrElseGet(getAsBooleanList(), defaultValue);
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
        return parseValue(Double::parseDouble, Double.class);
    }

    public Double getAsDoubleOrElse(Double defaultValue) {
        return getOrElse(getAsDouble(), defaultValue);
    }

    public Double getAsDoubleOrElseGet(Supplier<Double> defaultValue) {
        return getOrElseGet(getAsDouble(), defaultValue);
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
        return parseValueList(Double::parseDouble, Double.class);
    }

    public List<Double> getAsDoubleListOrElse(List<Double> defaultValue) {
        return getOrElse(getAsDoubleList(), defaultValue);
    }

    public List<Double> getAsDoubleListOrElseGet(Supplier<List<Double>> defaultValue) {
        return getOrElseGet(getAsDoubleList(), defaultValue);
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
        return parseValue(Integer::parseInt, Integer.class);
    }

    public Integer getAsIntegerOrElse(Integer defaultValue) {
        return getOrElse(getAsInteger(), defaultValue);
    }

    public Integer getAsIntegerOrElseGet(Supplier<Integer> defaultValue) {
        return getOrElseGet(getAsInteger(), defaultValue);
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
        return parseValueList(Integer::parseInt, Integer.class);
    }

    public List<Integer> getAsIntegerListOrElse(List<Integer> defaultValue) {
        return getOrElse(getAsIntegerList(), defaultValue);
    }

    public List<Integer> getAsIntegerListOrElseGet(Supplier<List<Integer>> defaultValue) {
        return getOrElseGet(getAsIntegerList(), defaultValue);
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
        return parseValue(TextHelper::toDuration, Duration.class);
    }

    public Duration getAsDurationOrElse(Duration defaultValue) {
        return getOrElse(getAsDuration(), defaultValue);
    }

    public Duration getAsDurationOrElseGet(Supplier<Duration> defaultValue) {
        return getOrElseGet(getAsDuration(), defaultValue);
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
        return parseValueList(TextHelper::toDuration, Duration.class);
    }

    public List<Duration> getAsDurationListOrElse(List<Duration> defaultValue) {
        return getOrElse(getAsDurationList(), defaultValue);
    }

    public List<Duration> getAsDurationListOrElseGet(Supplier<List<Duration>> defaultValue) {
        return getOrElseGet(getAsDurationList(), defaultValue);
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

    public OffsetDateTime getAsDateTimeOrElse(OffsetDateTime defaultValue) {
        return getOrElse(getAsDateTime(), defaultValue);
    }

    public OffsetDateTime getAsDateTimeOrElseGet(Supplier<OffsetDateTime> defaultValue) {
        return getOrElseGet(getAsDateTime(), defaultValue);
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

    public List<OffsetDateTime> getAsDateTimeListOrElse(List<OffsetDateTime> defaultValue) {
        return getOrElse(getAsDateTimeList(), defaultValue);
    }

    public List<OffsetDateTime> getAsDateTimeListOrElseGet(Supplier<List<OffsetDateTime>> defaultValue) {
        return getOrElseGet(getAsDateTimeList(), defaultValue);
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

    public OffsetDateTime getAsTimeOrElse(OffsetDateTime defaultValue) {
        return getOrElse(getAsTime(), defaultValue);
    }

    public OffsetDateTime getAsTimeOrElseGet(Supplier<OffsetDateTime> defaultValue) {
        return getOrElseGet(getAsTime(), defaultValue);
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

    public List<OffsetDateTime> getAsTimeListOrElse(List<OffsetDateTime> defaultValue) {
        return getOrElse(getAsTimeList(), defaultValue);
    }

    public List<OffsetDateTime> getAsTimeListOrElseGet(Supplier<List<OffsetDateTime>> defaultValue) {
        return getOrElseGet(getAsTimeList(), defaultValue);
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

    public OffsetDateTime getAsDateOrElse(OffsetDateTime defaultValue) {
        return getOrElse(getAsDate(), defaultValue);
    }

    public OffsetDateTime getAsDateOrElseGet(Supplier<OffsetDateTime> defaultValue) {
        return getOrElseGet(getAsDate(), defaultValue);
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

    public List<OffsetDateTime> getAsDateListOrElse(List<OffsetDateTime> defaultValue) {
        return getOrElse(getAsDateList(), defaultValue);
    }

    public List<OffsetDateTime> getAsDateListOrElseGet(Supplier<List<OffsetDateTime>> defaultValue) {
        return getOrElseGet(getAsDateList(), defaultValue);
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

    public OffsetDateTime getAsGYearMonthOrElse(OffsetDateTime defaultValue) {
        return getOrElse(getAsGYearMonth(), defaultValue);
    }

    public OffsetDateTime getAsGYearMonthOrElseGet(Supplier<OffsetDateTime> defaultValue) {
        return getOrElseGet(getAsGYearMonth(), defaultValue);
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

    public List<OffsetDateTime> getAsGYearMonthListOrElse(List<OffsetDateTime> defaultValue) {
        return getOrElse(getAsGYearMonthList(), defaultValue);
    }

    public List<OffsetDateTime> getAsGYearMonthListOrElseGet(Supplier<List<OffsetDateTime>> defaultValue) {
        return getOrElseGet(getAsGYearMonthList(), defaultValue);
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

    public OffsetDateTime getAsGMonthDayOrElse(OffsetDateTime defaultValue) {
        return getOrElse(getAsGMonthDay(), defaultValue);
    }

    public OffsetDateTime getAsGMonthDayOrElseGet(Supplier<OffsetDateTime> defaultValue) {
        return getOrElseGet(getAsGMonthDay(), defaultValue);
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

    public List<OffsetDateTime> getAsGMonthDayListOrElse(List<OffsetDateTime> defaultValue) {
        return getOrElse(getAsGMonthDayList(), defaultValue);
    }

    public List<OffsetDateTime> getAsGMonthDayListOrElseGet(Supplier<List<OffsetDateTime>> defaultValue) {
        return getOrElseGet(getAsGMonthDayList(), defaultValue);
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

    public OffsetDateTime getAsGDayOrElse(OffsetDateTime defaultValue) {
        return getOrElse(getAsGDay(), defaultValue);
    }

    public OffsetDateTime getAsGDayOrElseGet(Supplier<OffsetDateTime> defaultValue) {
        return getOrElseGet(getAsGDay(), defaultValue);
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

    public List<OffsetDateTime> getAsGDayListOrElse(List<OffsetDateTime> defaultValue) {
        return getOrElse(getAsGDayList(), defaultValue);
    }

    public List<OffsetDateTime> getAsGDayListOrElseGet(Supplier<List<OffsetDateTime>> defaultValue) {
        return getOrElseGet(getAsGDayList(), defaultValue);
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

    public OffsetDateTime getAsGMonthOrElse(OffsetDateTime defaultValue) {
        return getOrElse(getAsGMonth(), defaultValue);
    }

    public OffsetDateTime getAsGMonthOrElseGet(Supplier<OffsetDateTime> defaultValue) {
        return getOrElseGet(getAsGMonth(), defaultValue);
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

    public List<OffsetDateTime> getAsGMonthListOrElse(List<OffsetDateTime> defaultValue) {
        return getOrElse(getAsGMonthList(), defaultValue);
    }

    public List<OffsetDateTime> getAsGMonthListOrElseGet(Supplier<List<OffsetDateTime>> defaultValue) {
        return getOrElseGet(getAsGMonthList(), defaultValue);
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

    public OffsetDateTime getAsGYearOrElse(OffsetDateTime defaultValue) {
        return getOrElse(getAsGYear(), defaultValue);
    }

    public OffsetDateTime getAsGYearOrElseGet(Supplier<OffsetDateTime> defaultValue) {
        return getOrElseGet(getAsGYear(), defaultValue);
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

    public List<OffsetDateTime> getAsGYearListOrElse(List<OffsetDateTime> defaultValue) {
        return getOrElse(getAsGYearList(), defaultValue);
    }

    public List<OffsetDateTime> getAsGYearListOrElseGet(Supplier<List<OffsetDateTime>> defaultValue) {
        return getOrElseGet(getAsGYearList(), defaultValue);
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
        }

        if (trimContent().isEmpty()) {
            return null;
        }

        XMLGregorianCalendar calendar = TextHelper.toCalendar(trimmedContent, localName);
        return calendar != null ? setValue(calendar) : null;
    }

    private List<XMLGregorianCalendar> getAsCalendarList(String localName) {
        List<XMLGregorianCalendar> list = getAsList(XMLGregorianCalendar.class);
        if (list != null && list.get(0).getXMLSchemaType().getLocalPart().equals(localName)) {
            return list;
        }

        if (tokenizeContent() == 0) {
            return null;
        }

        list = new ArrayList<>(tokenizedContent.length);
        for (String token : tokenizedContent) {
            XMLGregorianCalendar parsed = TextHelper.toCalendar(token, localName);
            if (parsed == null) {
                return null;
            }

            list.add(parsed);
        }

        return setValue(list);
    }

    private <T> T setValue(T value) {
        this.value = value;
        return value;
    }

    private <T> T parseValue(Function<String, T> parser, Class<T> type) {
        Object v = value;
        if (type.isInstance(v)) {
            return type.cast(v);
        }

        if (trimContent().isEmpty()) {
            return null;
        }

        try {
            T parsed = parser.apply(trimmedContent);
            return parsed != null ? setValue(parsed) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private <T> List<T> parseValueList(Function<String, T> parser, Class<T> type) {
        List<T> list = getAsList(type);
        if (list != null) {
            return list;
        }

        if (tokenizeContent() == 0) {
            return null;
        }

        list = new ArrayList<>(tokenizedContent.length);
        for (String token : tokenizedContent) {
            try {
                T parsed = parser.apply(token);
                if (parsed == null) {
                    return null;
                }

                list.add(parsed);
            } catch (Exception e) {
                return null;
            }
        }

        return setValue(list);
    }

    private <T> T getOrElse(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    private <T> T getOrElseGet(T value, Supplier<T> defaultValue) {
        return value != null ? value : defaultValue.get();
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getAsList(Class<T> type) {
        return value instanceof List<?> list
                && !list.isEmpty()
                && type.isInstance(list.get(0)) ?
                (List<T>) list :
                null;
    }

    private static TextContent ofObject(Object value) {
        return value != null ? new TextContent(value.toString()) : ABSENT;
    }

    private static TextContent ofObjectList(List<?> values) {
        return values != null && !values.isEmpty() ? new TextContent(TextHelper.toContent(values)) : ABSENT;
    }

    private static TextContent ofOffsetDateTime(OffsetDateTime dateTime, EnumSet<CalendarField> fields, boolean withOffset) {
        XMLGregorianCalendar calendar = TextHelper.toCalendar(dateTime, fields, withOffset);
        return calendar != null ? new TextContent(calendar.toXMLFormat()) : ABSENT;
    }

    private static TextContent ofOffsetDateTimeList(List<OffsetDateTime> dateTimes, EnumSet<CalendarField> fields, boolean withOffset) {
        return dateTimes != null && !dateTimes.isEmpty() ?
                new TextContent(TextHelper.toContent(dateTimes, fields, withOffset)) :
                ABSENT;
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
