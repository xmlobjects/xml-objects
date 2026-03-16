/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.util.xml;

import java.util.regex.Pattern;

public class XMLPatterns {
    public static final Pattern NAME = Pattern.compile("[_:\\p{L}][-_:.\\p{L}0-9]*");
    public static final Pattern NCNAME = Pattern.compile("[_\\p{L}][-_.\\p{L}0-9]*");
    public static final Pattern LANGUAGE = Pattern.compile("[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*");
}
