package org.xmlobjects.stream;

import org.w3c.dom.Node;

public class ParseResult<T> {
    private final T object;
    private final Node node;

    private ParseResult(T object, Node node) {
        this.object = object;
        this.node = node;
    }

    public static <T> ParseResult<T> of(T object) {
        return new ParseResult<>(object, null);
    }

    public static <T> ParseResult<T> of(Node node) {
        return new ParseResult<>(null, node);
    }

    public static <T> ParseResult<T> empty() {
        return new ParseResult<>(null, null);
    }

    public boolean isSetObject() {
        return object != null;
    }

    public T getObject() {
        return object;
    }

    public boolean isSetDOMResult() {
        return node != null;
    }

    public Node getDOMResult(){
        return node;
    }
}
