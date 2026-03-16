/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects;

public class XMLObjectsException extends Exception {

    public XMLObjectsException() {
        super();
    }

    public XMLObjectsException(String message) {
        super(message);
    }

    public XMLObjectsException(Throwable cause) {
        super(cause);
    }

    public XMLObjectsException(String message, Throwable cause) {
        super(message, cause);
    }
}
