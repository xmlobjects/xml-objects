package org.xmlobjects;

public class XMLObjectsException extends Exception {
    private static final long serialVersionUID = 8533413316860346255L;

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
