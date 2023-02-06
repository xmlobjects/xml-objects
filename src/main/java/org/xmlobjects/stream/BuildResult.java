/*
 * xml-objects - A simple and lightweight XML-to-object mapping library
 * https://github.com/xmlobjects
 *
 * Copyright 2019-2023 Claus Nagel <claus.nagel@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xmlobjects.stream;

import org.w3c.dom.Element;

import java.util.function.Consumer;

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

    public void ifObject(Consumer<T> action) {
        if (isSetObject())
            action.accept(object);
    }

    public T getObject() {
        return object;
    }

    public boolean isSetDOMElement() {
        return element != null;
    }

    public void ifDOMElement(Consumer<Element> action) {
        if (isSetDOMElement())
            action.accept(element);
    }

    public Element getDOMElement(){
        return element;
    }
}
