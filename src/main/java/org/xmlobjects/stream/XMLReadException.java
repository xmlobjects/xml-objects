/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.stream;

public class XMLReadException extends Exception {

    public XMLReadException() {
        super();
    }

    public XMLReadException(String message) {
        super(message);
    }

    public XMLReadException(Throwable cause) {
        super(cause);
    }

    public XMLReadException(String message, Throwable cause) {
        super(message, cause);
    }
}
