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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;

public class TextHelper {
    private static final DatatypeFactory XML_TYPE_FACTORY;
    private static Function<LocalDateTime, ZoneOffset> ZONE_OFFSET_PROVIDER = dateTime ->
            ZoneOffset.systemDefault().getRules().getOffset(dateTime);

    private TextHelper() {
    }

    static {
        try {
            XML_TYPE_FACTORY = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException("Failed to initialize datatype factory.", e);
        }
    }

    static void setZoneOffsetProvider(Function<LocalDateTime, ZoneOffset> zoneOffsetProvider) {
        if (zoneOffsetProvider != null) {
            ZONE_OFFSET_PROVIDER = zoneOffsetProvider;
        }
    }

    static Boolean toBoolean(String value) {
        return switch (value) {
            case "true", "1" -> Boolean.TRUE;
            case "false", "0" -> Boolean.FALSE;
            default -> null;
        };
    }

    static Duration toDuration(String value) {
        return XML_TYPE_FACTORY.newDuration(value);
    }

    static XMLGregorianCalendar toCalendar(String value, String localName) {
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

    static XMLGregorianCalendar toCalendar(OffsetDateTime dateTime, EnumSet<CalendarField> fields, boolean withOffset) {
        XMLGregorianCalendar calendar = null;
        if (dateTime != null) {
            calendar = TextHelper.XML_TYPE_FACTORY.newXMLGregorianCalendar(
                    fields.contains(CalendarField.YEAR) ? dateTime.getYear() : DatatypeConstants.FIELD_UNDEFINED,
                    fields.contains(CalendarField.MONTH) ? dateTime.getMonthValue() : DatatypeConstants.FIELD_UNDEFINED,
                    fields.contains(CalendarField.DAY) ? dateTime.getDayOfMonth() : DatatypeConstants.FIELD_UNDEFINED,
                    fields.contains(CalendarField.HOUR) ? dateTime.getHour() : DatatypeConstants.FIELD_UNDEFINED,
                    fields.contains(CalendarField.MINUTE) ? dateTime.getMinute() : DatatypeConstants.FIELD_UNDEFINED,
                    fields.contains(CalendarField.SECOND) ? dateTime.getSecond() : DatatypeConstants.FIELD_UNDEFINED,
                    DatatypeConstants.FIELD_UNDEFINED,
                    withOffset && fields.contains(CalendarField.TIMEZONE) ?
                            dateTime.getOffset().getTotalSeconds() / 60 :
                            DatatypeConstants.FIELD_UNDEFINED);

            if (fields.contains(CalendarField.NANO) && dateTime.getNano() != 0) {
                calendar.setFractionalSecond(BigDecimal.valueOf(dateTime.getNano(), 9).stripTrailingZeros());
            }
        }

        return calendar;
    }

    static OffsetDateTime toOffsetDateTime(XMLGregorianCalendar calendar) {
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
                    ZONE_OFFSET_PROVIDER.apply(dateTime));
        } else {
            return null;
        }
    }

    static List<OffsetDateTime> toOffsetDateTimeList(List<XMLGregorianCalendar> calendars) {
        if (calendars != null) {
            List<OffsetDateTime> dateTimes = new ArrayList<>(calendars.size());
            for (XMLGregorianCalendar calendar : calendars) {
                dateTimes.add(TextHelper.toOffsetDateTime(calendar));
            }

            return dateTimes;
        } else {
            return null;
        }
    }

    static String normalize(String value) {
        int length = value.length();
        if (length != 0) {
            char[] chars = value.toCharArray();
            for (int i = 0; i < length; i++) {
                if (Character.isWhitespace(chars[i])) {
                    chars[i] = ' ';
                }
            }

            value = new String(chars);
        }

        return value;
    }

    static String collapse(String value) {
        int length = value.length();
        if (length != 0) {
            int i = 0;
            while (i < length && Character.isWhitespace(value.charAt(i))) {
                i++;
            }

            if (i != length) {
                StringBuilder collapsed = new StringBuilder(length - i);
                char ch = value.charAt(i);
                collapsed.append(ch);

                boolean isWhiteSpace = false;
                for (i += 1; i < length; i++) {
                    ch = value.charAt(i);
                    if (Character.isWhitespace(ch)) {
                        isWhiteSpace = true;
                    } else {
                        if (isWhiteSpace) {
                            collapsed.append(' ');
                            isWhiteSpace = false;
                        }

                        collapsed.append(ch);
                    }
                }

                value = collapsed.toString();
            } else {
                value = "";
            }
        }

        return value;
    }

    static String[] tokenize(String value) {
        int length = value.length();
        String[] tokens = new String[(length / 2) + 1];
        int noOfTokens = 0;
        int current = -1;
        int next;

        do {
            next = nextWhiteSpace(value, current + 1, length);
            if (next != current + 1) {
                tokens[noOfTokens++] = value.substring(current + 1, next);
            }

            current = next;
        } while (next != length);

        String[] tokenizedContent = new String[noOfTokens];
        System.arraycopy(tokens, 0, tokenizedContent, 0, noOfTokens);
        return tokenizedContent;
    }

    static int nextWhiteSpace(String value, int pos, int length) {
        while (pos < length && !Character.isWhitespace(value.charAt(pos))) {
            pos++;
        }

        return pos;
    }
}
