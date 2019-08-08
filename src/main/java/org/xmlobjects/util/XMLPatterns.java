package org.xmlobjects.util;

import java.util.regex.Pattern;

public class XMLPatterns {
    public static final Pattern NAME = Pattern.compile("[_:\\p{L}][-_:.\\p{L}0-9]*");
    public static final Pattern NCNAME = Pattern.compile("[_\\p{L}][-_.\\p{L}0-9]*");
    public static final Pattern LANGUAGE = Pattern.compile("[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*");
}
