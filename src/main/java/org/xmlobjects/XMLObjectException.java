package org.xmlobjects;

public class XMLObjectException extends Exception {
    private static final long serialVersionUID = 8533413316860346255L;

    public XMLObjectException() {
        super();
    }

    public XMLObjectException(String message) {
        super(message);
    }

    public XMLObjectException(Throwable cause) {
        super(cause);
    }

    public XMLObjectException(String message, Throwable cause) {
        super(message, cause);
    }
}
