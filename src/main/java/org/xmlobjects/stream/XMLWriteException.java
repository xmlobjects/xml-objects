package org.xmlobjects.stream;

public class XMLWriteException extends Exception {
    private static final long serialVersionUID = 6531053530564352599L;

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
