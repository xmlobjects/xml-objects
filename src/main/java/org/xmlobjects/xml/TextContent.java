package org.xmlobjects.xml;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class TextContent {
    private String content;

    private Object value;
    private String trimmed;
    private Set<Class<?>> tested;

    public TextContent(String content) {
        this.content = Objects.requireNonNull(content, "Content must not be null.");
    }

    public TextContent trim() {
        content = content.trim();
        return this;
    }

    public TextContent removeNewLines() {
        content = removeNewLines(content);
        return this;
    }

    public boolean isBoolean() {
        if (value == null && notTested(Boolean.class)) {
            if ("true".equals(trimmedContent())
                    || "1".equals(trimmedContent()))
                value = Boolean.TRUE;
            else if ("false".equals(trimmedContent())
                    || "0".equals(trimmedContent()))
                value = Boolean.FALSE;
        }

        return value instanceof Boolean;
    }

    public Boolean getAsBoolean() {
        return isBoolean() ? (Boolean) value : null;
    }

    public boolean isDouble() {
        if (value == null && notTested(Double.class)) {
            try {
                value = Double.parseDouble(trimmedContent());
            } catch (NumberFormatException e) {
                //
            }
        }

        return value instanceof Double;
    }

    public Double getAsDouble() {
        return isDouble() ? (Double) value : null;
    }

    public boolean isInteger() {
        if (value == null && notTested(Integer.class)) {
            try {
                value = Integer.parseInt(trimmedContent());
            } catch (NumberFormatException e) {
                //
            }
        }

        return value instanceof Integer;
    }

    public Integer getAsInteger() {
        return isInteger() ? (Integer) value : null;
    }

    private String removeNewLines(String value) {
        return value.replaceAll("\\R", "").replaceAll("\\s+", " ");
    }

    private boolean notTested(Class<?> type) {
        if (tested == null)
            tested = new HashSet<>();

        return tested.add(type);
    }

    private String trimmedContent() {
        if (trimmed == null)
            trimmed = removeNewLines(content.trim());

        return trimmed;
    }

    @Override
    public String toString() {
        return content;
    }
}
