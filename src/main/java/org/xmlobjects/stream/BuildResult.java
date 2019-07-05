package org.xmlobjects.stream;

import org.w3c.dom.Node;

public class BuildResult<T> {
    private final T object;
    private final Node node;

    private BuildResult(T object, Node node) {
        this.object = object;
        this.node = node;
    }

    public static <T> BuildResult<T> of(T object) {
        return new BuildResult<>(object, null);
    }

    public static <T> BuildResult<T> of(Node node) {
        return new BuildResult<>(null, node);
    }

    public static <T> BuildResult<T> empty() {
        return new BuildResult<>(null, null);
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
