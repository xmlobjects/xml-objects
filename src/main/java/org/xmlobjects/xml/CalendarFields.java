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

import java.util.EnumSet;

public enum CalendarFields {
    YEAR,
    MONTH,
    DAY,
    HOUR,
    MINUTE,
    SECOND,
    NANO,
    TIMEZONE;

    public static final EnumSet<CalendarFields> DATE_TIME_FIELDS = EnumSet.allOf(CalendarFields.class);
    public static final EnumSet<CalendarFields> TIME_FIELDS = EnumSet.of(HOUR, MINUTE, SECOND, NANO, TIMEZONE);
    public static final EnumSet<CalendarFields> DATE_FIELDS = EnumSet.of(YEAR, MONTH, DAY, TIMEZONE);
    public static final EnumSet<CalendarFields> GYEAR_MONTH_FIELDS = EnumSet.of(YEAR, MONTH, TIMEZONE);
    public static final EnumSet<CalendarFields> GMONTH_DAY_FIELDS = EnumSet.of(MONTH, DAY, TIMEZONE);
    public static final EnumSet<CalendarFields> GDAY_FIELDS = EnumSet.of(DAY, TIMEZONE);
    public static final EnumSet<CalendarFields> GMONTH_FIELDS = EnumSet.of(MONTH, TIMEZONE);
    public static final EnumSet<CalendarFields> GYEAR_FIELDS = EnumSet.of(YEAR, TIMEZONE);
}
