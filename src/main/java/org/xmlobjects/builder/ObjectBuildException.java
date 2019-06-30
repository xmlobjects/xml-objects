package org.xmlobjects.builder;

public class ObjectBuildException extends Exception {
    private static final long serialVersionUID = 6098471883159637787L;

    public ObjectBuildException() {
        super();
    }

    public ObjectBuildException(String message) {
        super(message);
    }

    public ObjectBuildException(Throwable cause) {
        super(cause);
    }

    public ObjectBuildException(String message, Throwable cause) {
        super(message, cause);
    }
}
