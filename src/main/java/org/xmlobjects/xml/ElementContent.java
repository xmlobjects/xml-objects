package org.xmlobjects.xml;

import java.util.Objects;

public class ElementContent {
    private final static ElementContent EMPTY = new ElementContent(null, null);

    private final String text;
    private final Element element;

    private ElementContent(String text, Element element) {
        this.text = text;
        this.element = element;
    }

    public static ElementContent of(String text) {
        return new ElementContent(Objects.requireNonNull(text, "Text must not be null."), null);
    }

    public static ElementContent of(Element element) {
        return new ElementContent(null, Objects.requireNonNull(element, "XML element must not be null."));
    }

    public static ElementContent empty() {
        return EMPTY;
    }

    public boolean isSetText() {
        return text != null;
    }

    public String getText() {
        return text;
    }

    public boolean isSetElement() {
        return element != null;
    }

    public Element getElement() {
        return element;
    }
}
