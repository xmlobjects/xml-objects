package org.xmlobjects.xml;

import java.util.Objects;

public class ElementContent {
    private final static ElementContent EMPTY = new ElementContent(null, null);

    private final TextContent textContent;
    private final Element element;

    private ElementContent(TextContent textContent, Element element) {
        this.textContent = textContent;
        this.element = element;
    }

    public static ElementContent of(TextContent textContent) {
        return new ElementContent(Objects.requireNonNull(textContent, "Text content must not be null."), null);
    }

    public static ElementContent of(Element element) {
        return new ElementContent(null, Objects.requireNonNull(element, "XML element must not be null."));
    }

    public static ElementContent empty() {
        return EMPTY;
    }

    public boolean isSetTextContent() {
        return textContent != null;
    }

    public TextContent getTextContent() {
        return textContent;
    }

    public boolean isSetElement() {
        return element != null;
    }

    public Element getElement() {
        return element;
    }
}
