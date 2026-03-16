/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.schema;

public class SchemaHandlerException extends Exception {

    public SchemaHandlerException() {
        super();
    }

    public SchemaHandlerException(String message) {
        super(message);
    }

    public SchemaHandlerException(Throwable cause) {
        super(cause);
    }

    public SchemaHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
