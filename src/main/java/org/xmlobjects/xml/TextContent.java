package org.xmlobjects.xml;

import java.util.Objects;

public class TextContent {
    private String content;

    private Object value;
    private String trimmed;

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
        if ("true".equals(trimmedContent())
                || "1".equals(trimmedContent()))
            value = Boolean.TRUE;
        else if ("false".equals(trimmedContent())
                || "0".equals(trimmedContent()))
            value = Boolean.FALSE;

        return value instanceof Boolean;
    }

    public Boolean getAsBoolean() {
        return isBoolean() ? (Boolean) value : null;
    }

    public boolean isDouble() {
        try {
            value = Double.parseDouble(trimmedContent());
        } catch (NumberFormatException e) {
            //
        }

        return value instanceof Double;
    }

    public Double getAsDouble() {
        return isDouble() ? (Double) value : null;
    }

    public boolean isInteger() {
        try {
            value = Integer.parseInt(trimmedContent());
        } catch (NumberFormatException e) {
            //
        }

        return value instanceof Integer;
    }

    public Integer getAsInteger() {
        return isInteger() ? (Integer) value : null;
    }

    private String removeNewLines(String value) {
        return value.replaceAll("\\R", "").replaceAll("\\s+", " ");
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
