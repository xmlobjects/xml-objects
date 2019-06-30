package org.xmlobjects.stream;

public class XMLReadException extends Exception {
    private static final long serialVersionUID = -5238313882968966786L;

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
