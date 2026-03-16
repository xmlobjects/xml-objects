/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Claus Nagel <claus.nagel@gmail.com>
 */

package org.xmlobjects.util.copy;

public class CopyException extends RuntimeException {

    public CopyException() {
        super();
    }

    public CopyException(String message) {
        super(message);
    }

    public CopyException(Throwable cause) {
        super(cause);
    }

    public CopyException(String message, Throwable cause) {
        super(message, cause);
    }
}
