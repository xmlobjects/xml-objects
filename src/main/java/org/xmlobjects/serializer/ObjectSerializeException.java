/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.serializer;

public class ObjectSerializeException extends Exception {

    public ObjectSerializeException() {
        super();
    }

    public ObjectSerializeException(String message) {
        super(message);
    }

    public ObjectSerializeException(Throwable cause) {
        super(cause);
    }

    public ObjectSerializeException(String message, Throwable cause) {
        super(message, cause);
    }
}
