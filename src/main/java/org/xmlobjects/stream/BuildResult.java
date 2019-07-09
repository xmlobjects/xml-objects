package org.xmlobjects.stream;

import org.w3c.dom.Element;

public class BuildResult<T> {
    private final static BuildResult<?> EMPTY = new BuildResult<>(null, null);

    private final T object;
    private final Element element;

    private BuildResult(T object, Element element) {
        this.object = object;
        this.element = element;
    }

    public static <T> BuildResult<T> of(T object) {
        return new BuildResult<>(object, null);
    }

    public static <T> BuildResult<T> of(Element element) {
        return new BuildResult<>(null, element);
    }

    @SuppressWarnings("unchecked")
    public static <T> BuildResult<T> empty() {
        return (BuildResult<T>) EMPTY;
    }

    public boolean isSetObject() {
        return object != null;
    }

    public T getObject() {
        return object;
    }

    public boolean isSetDOMElement() {
        return element != null;
    }

    public Element getDOMElement(){
        return element;
    }
}
