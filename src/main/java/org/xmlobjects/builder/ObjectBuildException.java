/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.builder;

public class ObjectBuildException extends Exception {

    public ObjectBuildException() {
        super();
    }

    public ObjectBuildException(String message) {
        super(message);
    }

    public ObjectBuildException(Throwable cause) {
        super(cause);
    }

    public ObjectBuildException(String message, Throwable cause) {
        super(message, cause);
    }
}
