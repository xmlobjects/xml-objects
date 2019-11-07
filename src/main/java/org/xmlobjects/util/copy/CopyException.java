package org.xmlobjects.util.copy;

public class CopyException extends RuntimeException {
    private static final long serialVersionUID = -4154821246498926284L;

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
