/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.stream;

public class XMLWriteException extends Exception {

    public XMLWriteException() {
        super();
    }

    public XMLWriteException(String message) {
        super(message);
    }

    public XMLWriteException(Throwable cause) {
        super(cause);
    }

    public XMLWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}
