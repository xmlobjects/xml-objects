/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.xml;

import java.util.EnumSet;

public enum CalendarField {
    YEAR,
    MONTH,
    DAY,
    HOUR,
    MINUTE,
    SECOND,
    NANO,
    TIMEZONE;

    public static final EnumSet<CalendarField> DATE_TIME_FIELDS = EnumSet.allOf(CalendarField.class);
    public static final EnumSet<CalendarField> TIME_FIELDS = EnumSet.of(HOUR, MINUTE, SECOND, NANO, TIMEZONE);
    public static final EnumSet<CalendarField> DATE_FIELDS = EnumSet.of(YEAR, MONTH, DAY, TIMEZONE);
    public static final EnumSet<CalendarField> GYEAR_MONTH_FIELDS = EnumSet.of(YEAR, MONTH, TIMEZONE);
    public static final EnumSet<CalendarField> GMONTH_DAY_FIELDS = EnumSet.of(MONTH, DAY, TIMEZONE);
    public static final EnumSet<CalendarField> GDAY_FIELDS = EnumSet.of(DAY, TIMEZONE);
    public static final EnumSet<CalendarField> GMONTH_FIELDS = EnumSet.of(MONTH, TIMEZONE);
    public static final EnumSet<CalendarField> GYEAR_FIELDS = EnumSet.of(YEAR, TIMEZONE);
}
